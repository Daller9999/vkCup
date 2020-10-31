package com.example.vkcupalbums.Fragments.Documents;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.example.vkcupalbums.Objects.VkDocsData;
import com.example.vkcupalbums.R;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import static java.lang.Thread.sleep;

class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
    private volatile List<VkDocsData> list;
    private LayoutInflater layoutInflater;
    private OnRecyclerClick onRecyclerClick;
    private AlertDialog alertDialogRename;
    private EditText editTextRename;
    private int userId;
    private int currentPos = -1;
    private volatile List<String> httpsPhotos;
    private List<Boolean> needLoad;
    private boolean isLoadRun = true;
    private Handler handler = new Handler();

    void setOnRecyclerClick(OnRecyclerClick onRecyclerClick) {
        this.onRecyclerClick = onRecyclerClick;
    }

    void addList(List<VkDocsData> vkDocsData) {
        this.list.addAll(vkDocsData);
        for (VkDocsData vkDocsData1 : vkDocsData) {
            httpsPhotos.add(vkDocsData1.getHttpPhoto());
            needLoad.add(vkDocsData1.getBitmap() == null);
        }
        loadImages();
        notifyDataSetChanged();
    }

    void setList(List<VkDocsData> vkDocsData) {
        this.list = new ArrayList<>(vkDocsData);
        needLoad = new ArrayList<>();
        for (VkDocsData vkDocsData1 : vkDocsData) {
            httpsPhotos.add(vkDocsData1.getHttpPhoto());
            needLoad.add(vkDocsData1.getBitmap() == null);
        }
        loadImages();
        notifyDataSetChanged();
    }

    RecyclerAdapter(Context context) {
        layoutInflater = LayoutInflater.from(context);
        list = new ArrayList<>();
        httpsPhotos = new ArrayList<>();
        needLoad = new ArrayList<>();

        userId = Integer.valueOf(VKAccessToken.currentToken().userId);


        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        editTextRename = new EditText(context);
        // editTextName.setMaxWidth(125);
        builder.setTitle("Введите имя файла");
        builder.setPositiveButton("Сохранить", (dialogInterface, i) -> {
            String name = editTextRename.getText().toString();
            String etc = list.get(currentPos).getExt();
            if (name.isEmpty())
                name = list.get(currentPos).getTitle();
            else if (name.length() + 1 + etc.length() > 128)
                name = name.substring(0, 128 - 1 - etc.length());
            String res = name + "." + etc;
            renameFile(res);
            // createAlbumApi(name);
            dialogInterface.dismiss();
        });
        builder.setNegativeButton("Отмена", (dialogInterface, i) -> {
            currentPos = -1;
            dialogInterface.dismiss();
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override public void onCancel(DialogInterface dialogInterface) {
                currentPos = -1;
            }
        });
        builder.setView(editTextRename);
        alertDialogRename = builder.create();
    }

    void updateRowText(String text, int position) {
        list.get(position).setTitle(text);
        notifyItemChanged(position);
    }

    void updateImage(Bitmap bitmap, int pos) {
        list.get(pos).setBitmap(bitmap);
        notifyItemChanged(pos);
    }

    void removeRow(int position) {
        list.remove(position);
        notifyItemRemoved(position);
    }

    void clear() {
        list.clear();
        notifyDataSetChanged();
    }

    // String getItemText(int id) { return list.get(id); }

    @Override @NonNull public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.recycler_lits_item, parent, false);
        return new ViewHolder(view);
    }

    @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VkDocsData vkDocsData = list.get(position);
        if (vkDocsData.getBitmap() == null)
            holder.imageView.setImageResource(vkDocsData.getImageResources());
        else
            holder.imageView.setImageBitmap(vkDocsData.getBitmap());
        holder.textViewName.setText(vkDocsData.getTitle());
        holder.textViewSizeAndOther.setText(vkDocsData.getTypeSizeDate());
    }

    @Override public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {
        private TextView textViewName;
        private TextView textViewSizeAndOther;
        private TextView textViewOther;
        private Button buttonMenu;

        private ImageView imageView;
        private ConstraintLayout constraintLayout;

        private PopupMenu popupMenu;

        ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageSrc);

            textViewName = itemView.findViewById(R.id.textViewFileName);
            textViewSizeAndOther = itemView.findViewById(R.id.textViewDataSize);
            textViewOther = itemView.findViewById(R.id.textViewDataAny);

            buttonMenu = itemView.findViewById(R.id.buttonMore);

            constraintLayout = itemView.findViewById(R.id.containerItem);
            constraintLayout.setOnClickListener(this);

            popupMenu = new PopupMenu(itemView.getContext(), buttonMenu);
            popupMenu.inflate(R.menu.recycler_popup_menu_item);
            popupMenu.setOnMenuItemClickListener(this);
            buttonMenu.setOnClickListener((v) -> popupMenu.show());
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            if (currentPos != -1) return false;

            currentPos = getAdapterPosition();
            if (menuItem.getItemId() == R.id.buttonRename) {
                String text = list.get(getAdapterPosition()).getTitle();
                String etc = list.get(getAdapterPosition()).getExt();
                editTextRename.setText(list.get(getAdapterPosition()).getTitle().substring(0, text.length() - etc.length() - 1));
                alertDialogRename.show();
            } else if (menuItem.getItemId() == R.id.buttonDelete) {
                removeFile();
            }
            return false;
        }

        @Override public void onClick(View view) {
            if (onRecyclerClick != null) {
                isLoadRun = false;
                if (view.getId() == R.id.containerItem)
                    onRecyclerClick.onItemClick(list.get(getAdapterPosition()));
            }
        }
    }

    private void renameFile(String text) {
        int fileId = list.get(currentPos).getId();
        VKRequest vkRequest = new VKRequest("docs.edit", VKParameters.from(VKApiConst.OWNER_ID, userId, "doc_id", fileId, "title", text));
        vkRequest.executeWithListener(new VKRequest.VKRequestListener() {
            @Override public void onError(VKError error) {
                super.onError(error);
                if (onRecyclerClick != null)
                    onRecyclerClick.onError("Не удалось переименовать файл, проверьте соединение с интернетом");
                currentPos = -1;

            }

            @Override public void onComplete(VKResponse response) {
                super.onComplete(response);
                if (currentPos != -1) {
                    list.get(currentPos).setTitle(text);
                    notifyItemChanged(currentPos);
                    currentPos = -1;
                }
            }
        });
    }

    private void removeFile() {
        int fileId = list.get(currentPos).getId();
        VKRequest vkRequest = new VKRequest("docs.delete", VKParameters.from(VKApiConst.OWNER_ID, userId, "doc_id", fileId));
        vkRequest.executeWithListener(new VKRequest.VKRequestListener() {
            @Override public void onError(VKError error) {
                super.onError(error);
                if (onRecyclerClick != null)
                    onRecyclerClick.onError("Не удалось переименовать файл, проверьте соединение с интернетом");
                currentPos = -1;
            }

            @Override public void onComplete(VKResponse response) {
                super.onComplete(response);
                if (currentPos != -1) {
                    list.remove(currentPos);
                    httpsPhotos.remove(currentPos);
                    notifyItemRemoved(currentPos);
                    currentPos = -1;
                }
            }
        });

    }

    private void loadImages() {
        Observable<DataLoad> observable = Observable.create((emitter) -> {
            int count = 0;
            for (int i = 0; i < httpsPhotos.size() && isLoadRun; i++) {
                VkDocsData vkDocsData = list.get(i);
                if (needLoad.get(i) && (vkDocsData.getType() == VkDocsData.IMAGE || vkDocsData.getType() == VkDocsData.GIF)) {
                    String http = httpsPhotos.get(i);
                    try {
                        Bitmap bitmap = loadPhoto(http);
                        while (list.size() < count + 1)
                            sleep(50);
                        emitter.onNext(new DataLoad(bitmap, count));
                    } catch (InterruptedException ex) {
                        //
                    }
                }
                count++;
            }
            emitter.onComplete();
        });
        observable = observable.observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.newThread());
        observable.subscribe(dataLoad -> updateImage(dataLoad.bitmap, dataLoad.pos),
                             throwable -> throwable.printStackTrace());
    }

    private class DataLoad {
        private Bitmap bitmap;
        private int pos;

        DataLoad(Bitmap bitmap, int pos) {
            this.bitmap = bitmap;
            this.pos = pos;
        }
    }

    private Bitmap loadPhoto(String http) {
        Bitmap bitmap = null;
        try {
            InputStream in = new java.net.URL(http).openStream();
            bitmap = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e("mesUri", "error to load image : " + e.getMessage());
        }
        return bitmap;
    }

    public void destroy() {
        list.clear();
        onRecyclerClick = null;
        httpsPhotos.clear();
        isLoadRun = false;
    }

    public interface OnRecyclerClick {
        void onItemClick(VkDocsData vkDocsData);
        void onError(String text);
    }
}

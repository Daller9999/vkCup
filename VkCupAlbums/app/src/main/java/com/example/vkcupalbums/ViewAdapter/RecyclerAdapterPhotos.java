package com.example.vkcupalbums.ViewAdapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;

import com.example.vkcupalbums.Objects.PhotoInfo;
import com.example.vkcupalbums.R;
import com.vk.sdk.api.model.VKApiPhoto;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class RecyclerAdapterPhotos extends RecyclerView.Adapter<RecyclerAdapterPhotos.ViewHolder> {
    private volatile List<PhotoInfo[]> list;
    private LayoutInflater layoutInflater;
    private OnRecyclerListener onRecyclerListener;

    private volatile boolean edit = false;

    public List<PhotoInfo> getList() {
        List<PhotoInfo> photoInfos = new ArrayList<>();
        for (PhotoInfo[] photoInfos1 : list)
            for (PhotoInfo photoInfo : photoInfos1)
                photoInfos.add(photoInfo);
        return photoInfos;
    }

    public void setEdit(boolean b) {
        edit = b;
        if (!edit) {
            List<PhotoInfo> albumInfoAll = new ArrayList<>();
            Vector<Integer> removeIds = new Vector<>();
            for (PhotoInfo[] photoInfos : list) {
                for (PhotoInfo photoInfo : photoInfos) {
                    if (photoInfo != null) {
                        if (!photoInfo.isRemove())
                            albumInfoAll.add(photoInfo);
                        else
                            removeIds.addElement(photoInfo.getId());
                    }
                }
            }

            if (!removeIds.isEmpty()) {
                int[] mas = new int[removeIds.size()];
                for (int i = 0; i < removeIds.size(); i++)
                    mas[i] = removeIds.elementAt(i);
                if (onRecyclerListener != null)
                    onRecyclerListener.onRemove(mas);
            }
            setList(albumInfoAll);
        } else
            notifyDataSetChanged();
    }

    private Animation shakeAnimation;

    public void setOnRecyclerListener(OnRecyclerListener onRecyclerListener) {
        this.onRecyclerListener = onRecyclerListener;
    }

    public synchronized void addPhotoInfo(PhotoInfo photoInfo) {
        if (!list.isEmpty()) {
            int pos = list.size() - 1;
            PhotoInfo[] photoInfos = list.get(pos);
            if (photoInfos[0] != null && photoInfos[1] == null) {
                photoInfos[1] = photoInfo;
                list.set(pos, photoInfos);
            } else if (photoInfos[0] != null && photoInfos[2] == null) {
                photoInfos[2] = photoInfo;
                list.set(pos, photoInfos);
            } else
                list.add(new PhotoInfo[]{photoInfo, null, null});
        } else
            list.add(new PhotoInfo[]{photoInfo, null, null});
        notifyDataSetChanged();
    }

    public synchronized void addPhotoInfo(PhotoInfo[] albumInfos) {
        list.add(albumInfos);
        notifyItemInserted(list.size() - 1);
    }

    public RecyclerAdapterPhotos(Context context) {
        layoutInflater = LayoutInflater.from(context);
        shakeAnimation = AnimationUtils.loadAnimation(context, R.anim.shake);
        list = new ArrayList<>();
    }

    public synchronized void setList(List<PhotoInfo> photoInfos) {
        list = new ArrayList<>();
        for (int i = 0; i < photoInfos.size(); i += 3) {
            PhotoInfo[] groupInfosNew = new PhotoInfo[3];
            for (int j = 0; j < 3 && j + i < photoInfos.size(); j++)
                groupInfosNew[j] = photoInfos.get(j + i);
            list.add(groupInfosNew);
        }
        loadPhoto();
    }


    private void loadPhoto() {
        Observable<Integer> observable = Observable.create((emitter) -> {
            Bitmap bitmap;
            int count = 0;
            for (PhotoInfo[] photoInfos : list) {
                for (PhotoInfo photoInfo : photoInfos) {
                    if (photoInfo != null && photoInfo.getBitmap() == null) {
                        bitmap = loadPhoto(photoInfo.getVkApiPhoto());
                        photoInfo.setBitmap(bitmap);
                    }
                }
                emitter.onNext(count);
                count++;
            }
            emitter.onNext(count);
            emitter.onComplete();
        });
        observable = observable.observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.newThread());
        observable.subscribe(count -> notifyItemChanged(count), throwable -> throwable.printStackTrace());
    }

    private Bitmap loadPhoto(VKApiPhoto vkApiPhoto) {
        String[] photos = new String[]{vkApiPhoto.photo_2560, vkApiPhoto.photo_1280,
                vkApiPhoto.photo_807, vkApiPhoto.photo_604,
                vkApiPhoto.photo_130, vkApiPhoto.photo_75};
        String http = photos[0];
        for (String photoHttp : photos)
            if (!photoHttp.isEmpty()) {
                http = photoHttp;
                break;
            }

        Bitmap mIcon11 = null;
        try {
            InputStream in = new java.net.URL(http).openStream();
            mIcon11 = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("mesUri", "error to load image : " + e.getMessage());
        }
        return mIcon11;
    }

    /*GroupInfo[] getItemText(int id) {
        return list.get(id);
    }*/

    @Override @NonNull public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.recycler_list_fragment_photo_item, parent, false);
        return new ViewHolder(view);
    }

    @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PhotoInfo[] photoInfos = list.get(position);
        int visibility;
        boolean enable;

        for (int i = 0; i < photoInfos.length; i++) {
            if (photoInfos[i] != null) {
                if (!photoInfos[i].isRemove()) {
                    holder.viewsDelete[i].setVisibility(View.GONE);
                    holder.imageViews[i].setImageBitmap(photoInfos[i].getBitmap());

                    holder.checks[i].setVisibility(edit ? View.VISIBLE : View.GONE);
                    if (edit)
                        holder.cardViews[i].startAnimation(shakeAnimation);
                    else
                        holder.cardViews[i].clearAnimation();
                } else {
                    holder.viewsDelete[i].setVisibility(View.GONE);
                    holder.cardViews[i].clearAnimation();
                    holder.checks[i].setVisibility(View.GONE);
                }
            } else {
                holder.checks[i].setVisibility(View.GONE);
                holder.cardViews[i].clearAnimation();
                holder.viewsDelete[i].setVisibility(View.GONE);
            }
            visibility = photoInfos[i] == null ? View.INVISIBLE : View.VISIBLE;
            enable = photoInfos[i] != null;
            holder.imageViews[i].setEnabled(enable);
            holder.cardViews[i].setVisibility(visibility);
        }
    }

    @Override public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
        holder.checkAnimation();
    }

    @Override public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
        for (CardView cardView : holder.cardViews)
            cardView.clearAnimation();
    }

    @Override public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private CardView[] cardViews;
        private ImageView[] imageViews;
        private Button[] checks;
        private View[] viewsDelete;
        int[] ids;

        ViewHolder(View itemView) {
            super(itemView);
            cardViews = new CardView[]{itemView.findViewById(R.id.view1), itemView.findViewById(R.id.view2), itemView.findViewById(R.id.view3)};
            imageViews = new ImageView[]{itemView.findViewById(R.id.image1), itemView.findViewById(R.id.image2), itemView.findViewById(R.id.image3)};
            checks = new Button[]{itemView.findViewById(R.id.check1), itemView.findViewById(R.id.check2), itemView.findViewById(R.id.check3)};
            viewsDelete = new View[]{itemView.findViewById(R.id.viewDelete1), itemView.findViewById(R.id.viewDelete2), itemView.findViewById(R.id.viewDelete3)};
            ids = new int[]{R.id.image1, R.id.image2};
            for (int i = 0; i < imageViews.length; i++) {
                imageViews[i].setOnClickListener(this);
                imageViews[i].setOnLongClickListener(this);
                checks[i].setOnClickListener(this);
            }

        }

        @Override public void onClick(View view) {
            int pos = getAdapterPosition();
            if (edit) {
                for (int i = 0; i < checks.length; i++)
                    if (checks[i].getId() == view.getId()) {
                        list.get(pos)[i].setRemove();
                        checkAnimation();
                        viewsDelete[i].setVisibility(View.VISIBLE);
                        return;
                    }
            } else if (onRecyclerListener != null) {
                for (int i = 0; i < imageViews.length; i++)
                    if (imageViews[i].getId() == view.getId()) {
                        onRecyclerListener.onPhotoClick(list.get(pos)[i]);
                        return;
                    }
            }

        }

        @Override public boolean onLongClick(View view) {
            if (onRecyclerListener != null)
                onRecyclerListener.onLongClick();
            return false;
        }

        private void checkAnimation() {
            int pos = getAdapterPosition();
            PhotoInfo[] photoInfos = list.get(pos);
            if (photoInfos != null && photoInfos.length != 0) {
                for (int i = 0; i < photoInfos.length; i++) {
                    if (photoInfos[i] != null) {
                        if (edit && !photoInfos[i].isRemove())
                            cardViews[i].startAnimation(shakeAnimation);
                        else if (photoInfos[i].isRemove()) {
                            cardViews[i].clearAnimation();
                            viewsDelete[i].setVisibility(View.VISIBLE);
                            checks[i].setVisibility(View.GONE);
                        }
                    }
                }
            }
        }
    }


    /*private void openGroupInfo(GroupInfo groupInfo) {
        constraintLayout.setVisibility(View.VISIBLE);
        textViewName.setText(groupInfo.getName());
        textViewDescription.setText(groupInfo.getDescription());
        textViewMembers.setText(groupInfo.getStringMembersAndFriends());
        textViewLastPost.setText(groupInfo.getDateString());
        isOpen = false;
    }*/

}

package com.example.vkcupalbums.ViewAdapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.vkcupalbums.Objects.AlbumInfo;
import com.example.vkcupalbums.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
    private volatile List<AlbumInfo[]> list;
    private LayoutInflater layoutInflater;
    private OnRecyclerListener onRecyclerListener;

    private volatile boolean edit = false;

    public void addAlbumInfo(AlbumInfo albumInfo) {
        if (!list.isEmpty()) {
            int pos = list.size() - 1;
            AlbumInfo[] albumInfos = list.get(pos);
            if (albumInfos[0] != null && albumInfos[1] == null) {
                albumInfos[1] = albumInfo;
                list.set(pos, albumInfos);
            } else
                list.add(new AlbumInfo[]{albumInfo, null});
        } else
            list.add(new AlbumInfo[]{albumInfo, null});
        notifyDataSetChanged();
    }

    public void setEdit(boolean b) {
        edit = b;
        if (!edit) {
            List<AlbumInfo> albumInfoAll = new ArrayList<>();
            Vector<Integer> removeIds = new Vector<>();
            for (AlbumInfo[] albumInfos : list) {
                if (albumInfos[0] != null) {
                    if (!albumInfos[0].isRemove())
                        albumInfoAll.add(albumInfos[0]);
                    else
                        removeIds.addElement(albumInfos[0].getId());
                }

                if (albumInfos[1] != null) {
                    if (!albumInfos[1].isRemove())
                        albumInfoAll.add(albumInfos[1]);
                    else
                        removeIds.addElement(albumInfos[1].getId());
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

    /*public void onRemoveIds(int[] ids) {
        List<GroupInfo> newList = new ArrayList<>();
        Vector<Integer> idv = new Vector<>();
        for (int id : ids)
            idv.addElement(id);
        for (GroupInfo[] info : list) {
            for (GroupInfo groupInfo : info)
                if (groupInfo != null && !idv.contains(groupInfo.getId()))
                    newList.add(groupInfo);
        }
        setList(newList);
    }*/

    public void addAlbumInfo(AlbumInfo[] albumInfos) {
        list.add(albumInfos);
        notifyItemInserted(list.size() - 1);
    }

    public RecyclerAdapter(Context context) {
        layoutInflater = LayoutInflater.from(context);
        shakeAnimation = AnimationUtils.loadAnimation(context, R.anim.shake);
        list = new ArrayList<>();
    }

    public void setImageBitmap(int row, int column, Bitmap bitmap) {
        list.get(row)[column].setBitmapMain(bitmap);
        notifyItemChanged(row);
    }

    private void updateData() {

    }


    public void setList(List<AlbumInfo> groupInfos) {
        list = new ArrayList<>();
        for (int i = 0; i < groupInfos.size(); i += 2) {
            AlbumInfo[] groupInfosNew = new AlbumInfo[2];
            for (int j = 0; j < 2 && j + i < groupInfos.size(); j++)
                groupInfosNew[j] = groupInfos.get(j + i);
            list.add(groupInfosNew);
        }
        notifyDataSetChanged();
    }


    /*GroupInfo[] getItemText(int id) {
        return list.get(id);
    }*/

    @Override @NonNull public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.recycler_list_fragment_item, parent, false);
        return new ViewHolder(view);
    }

    @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AlbumInfo[] albumInfos = list.get(position);
        int visibility;
        boolean enable;

        for (int i = 0; i < albumInfos.length; i++) {
            if (albumInfos[i] != null) {
                if (!albumInfos[i].isRemove()) {
                    holder.viewsDelete[i].setVisibility(View.GONE);
                    if (albumInfos[i].getDescription() != null)
                        holder.textViewsA[i].setText(albumInfos[i].getDescription());
                    if (albumInfos[i].getPhotoCount() != -1)
                        holder.textViewsB[i].setText(albumInfos[i].getPhotoCountString());
                    if (albumInfos[i].getBitmapMain() != null)
                        holder.imageViews[i].setImageBitmap(albumInfos[i].getBitmapMain());
                    else
                        holder.imageViews[i].setImageBitmap(null);

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
                holder.textViewsA[i].setText("");
                holder.textViewsB[i].setText("");
            }
            visibility = albumInfos[i] == null ? View.INVISIBLE : View.VISIBLE;
            enable = albumInfos[i] != null;
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

        private TextView textView1a;
        private TextView textView1b;
        private TextView textView2a;
        private TextView textView2b;
        private TextView[] textViewsA;
        private TextView[] textViewsB;
        private CardView[] cardViews;

        private ImageView imageView1;
        private ImageView imageView2;
        private ImageView[] imageViews;

        private Button check1;
        private Button check2;
        private Button[] checks;

        private View[] viewsDelete;

        int[] ids;

        ViewHolder(View itemView) {
            super(itemView);
            textView1a = itemView.findViewById(R.id.text1a);
            textView1b = itemView.findViewById(R.id.text1b);

            textView2a = itemView.findViewById(R.id.text2a);
            textView2b = itemView.findViewById(R.id.text2b);

            textViewsA = new TextView[]{textView1a, textView2a};
            textViewsB = new TextView[]{textView1b, textView2b};

            cardViews = new CardView[]{itemView.findViewById(R.id.view1), itemView.findViewById(R.id.view2)};
            ids = new int[]{R.id.image1, R.id.image2};

            imageView1 = itemView.findViewById(R.id.image1);
            imageView1.setOnLongClickListener(this);
            imageView1.setOnClickListener(this);
            check1 = itemView.findViewById(R.id.check1);
            check1.setOnClickListener(this);

            imageView2 = itemView.findViewById(R.id.image2);
            imageView2.setOnLongClickListener(this);
            imageView2.setOnClickListener(this);
            check2 = itemView.findViewById(R.id.check2);
            check2.setOnClickListener(this);

            // textViews = new TextView[]{textView1, textView2, textView3};
            imageViews = new ImageView[]{imageView1, imageView2};
            checks = new Button[]{check1, check2};
            viewsDelete = new View[]{itemView.findViewById(R.id.viewDelete1), itemView.findViewById(R.id.viewDelete2)};

        }

        @Override public void onClick(View view) {
            int pos = getAdapterPosition();
            if (edit) {
                if (view.getId() == R.id.check1 || view.getId() == R.id.check2) {
                    int element = view.getId() == R.id.check1 ? 0 : 1;
                    list.get(pos)[element].setRemove();
                    checkAnimation();
                    viewsDelete[element].setVisibility(View.VISIBLE);
                }
            } else if (onRecyclerListener != null) {
                int element = -1;
                if (view.getId() == R.id.image1)
                    element = 0;
                else if (view.getId() == R.id.image2)
                    element = 1;
                if (element != -1)
                    onRecyclerListener.onItemClick(list.get(pos)[element].getId());
            }

        }

        @Override public boolean onLongClick(View view) {
            if (onRecyclerListener != null)
                onRecyclerListener.onLongClick();
            return false;
        }

        private void checkAnimation() {
            int pos = getAdapterPosition();
            AlbumInfo[] albumInfos = list.get(pos);
            if (albumInfos != null && albumInfos.length != 0) {
                for (int i = 0; i < albumInfos.length; i++) {
                    if (albumInfos[i] != null) {
                        if (edit && !albumInfos[i].isRemove())
                            cardViews[i].startAnimation(shakeAnimation);
                        else if (albumInfos[i].isRemove()) {
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

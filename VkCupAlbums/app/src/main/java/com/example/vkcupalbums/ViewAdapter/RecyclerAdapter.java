package com.example.vkcupalbums.ViewAdapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.vkcupalbums.AlbumInfo;
import com.example.vkcupalbums.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
    private volatile List<AlbumInfo[]> list;
    private LayoutInflater layoutInflater;
    private OnRecyclerClick onRecyclerClick;
    private ConstraintLayout constraintLayout;

    private TextView textViewName;
    private TextView textViewMembers;
    private TextView textViewDescription;
    private TextView textViewLastPost;

    private volatile boolean isOpen = true;
    private volatile boolean isSelect = false;

    public void setOnRecyclerClick(OnRecyclerClick onRecyclerClick) {
        this.onRecyclerClick = onRecyclerClick;
    }

    /*public void removeIds(int[] ids) {
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

    public RecyclerAdapter(Context context, List<AlbumInfo> albumInfos) {
        layoutInflater = LayoutInflater.from(context);
        setList(albumInfos);
    }

    private void hideShow() {
        constraintLayout.setVisibility(View.GONE);
        isOpen = true;
    }


    private void setList(List<AlbumInfo> groupInfos) {
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
                if (albumInfos[i].getDescription() != null)
                    holder.textViewsA[i].setText(albumInfos[i].getDescription());
                if (albumInfos[i].getPhotoCount() != -1)
                    holder.textViewsB[i].setText(albumInfos[i].getPhotoCountString());
                if (albumInfos[i].getBitmapMain() != null)
                    holder.imageViews[i].setImageBitmap(albumInfos[i].getBitmapMain());
                /*visibility = albumInfos[i].isSelected() ? View.VISIBLE : View.GONE;
                holder.views[i].setVisibility(visibility);
                holder.checks[i].setVisibility(visibility);*/
            } else {
                holder.views[i].setVisibility(View.GONE);
                holder.checks[i].setVisibility(View.GONE);
            }
            visibility = albumInfos[i] == null ? View.INVISIBLE : View.VISIBLE;
            enable = albumInfos[i] != null;
            // holder.textViews[i].setVisibility(visibility);
            holder.imageViews[i].setEnabled(enable);
            holder.cardViews[i].setVisibility(visibility);
        }
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

        private View view1;
        private View view2;
        private View[] views;

        private View check1;
        private View check2;
        private View[] checks;

        ViewHolder(View itemView) {
            super(itemView);
            textView1a = itemView.findViewById(R.id.text1a);
            textView1b = itemView.findViewById(R.id.text1b);

            textView2a = itemView.findViewById(R.id.text2a);
            textView2b = itemView.findViewById(R.id.text2b);

            textViewsA = new TextView[]{textView1a, textView2a};
            textViewsB = new TextView[]{textView1b, textView2b};

            cardViews = new CardView[]{itemView.findViewById(R.id.view1), itemView.findViewById(R.id.view2)};

            imageView1 = itemView.findViewById(R.id.image1);
            imageView1.setOnLongClickListener(this);
            imageView1.setOnClickListener(this);
            // view1 = itemView.findViewById(R.id.select_image1);
            check1 = itemView.findViewById(R.id.check1);

            imageView2 = itemView.findViewById(R.id.image2);
            imageView2.setOnLongClickListener(this);
            imageView2.setOnClickListener(this);
            // view2 = itemView.findViewById(R.id.select_image2);
            check2 = itemView.findViewById(R.id.check2);

            // textViews = new TextView[]{textView1, textView2, textView3};
            imageViews = new ImageView[]{imageView1, imageView2};
            views = new View[]{view1, view2};
            checks = new View[]{check1, check2};

        }

        @Override public void onClick(View view) {
            /*if (!isOpen) return;
            boolean select;
            int visibility;
            if (view.getId() == R.id.image1 && list.get(getAdapterPosition())[0] != null) {
                select = !list.get(getAdapterPosition())[0].isSelected();
                list.get(getAdapterPosition())[0].setSelected(select);
                visibility = select ? View.VISIBLE : View.GONE;
                view1.setVisibility(visibility);
                check1.setVisibility(visibility);
            } else if (view.getId() == R.id.image2 && list.get(getAdapterPosition())[1] != null) {
                select = !list.get(getAdapterPosition())[1].isSelected();
                list.get(getAdapterPosition())[1].setSelected(select);
                visibility = select ? View.VISIBLE : View.GONE;
                view2.setVisibility(visibility);
                check2.setVisibility(visibility);
            } else if (view.getId() == R.id.image3 && list.get(getAdapterPosition())[2] != null) {
                select = !list.get(getAdapterPosition())[2].isSelected();
                list.get(getAdapterPosition())[2].setSelected(select);
                visibility = select ? View.VISIBLE : View.GONE;
                view3.setVisibility(visibility);
                check3.setVisibility(visibility);
            }
            List<Integer> delete = new ArrayList<>();
            for (GroupInfo[] groupInfos : list)
                for (GroupInfo groupInfo : groupInfos)
                    if (groupInfo != null && groupInfo.isSelected()) // todo fix
                        delete.add(groupInfo.getId());
            if (onRecyclerClick != null) {
                if (!delete.isEmpty()) {
                    isSelect = true;
                    int[] mas = new int[delete.size()];
                    for (int i = 0; i < delete.size(); i++)
                        mas[i] = delete.get(i);
                    onRecyclerClick.onItemClick(mas);
                } else {
                    onRecyclerClick.onItemClick(new int[]{});
                    isSelect = false;
                }
            }*/
        }

        @Override public boolean onLongClick(View view) {
            /*if (!isOpen && !isSelect) return false;
            GroupInfo groupInfo = null;
            if (view.getId() == R.id.image1) {
                // clicked1 = true;
                groupInfo = list.get(getAdapterPosition())[0];
            } else if (view.getId() == R.id.image2) {
                // clicked2 = true;
                groupInfo = list.get(getAdapterPosition())[1];
            } else if (view.getId() == R.id.image3) {
                // clicked3 = true;
                groupInfo = list.get(getAdapterPosition())[2];
            }
            if (groupInfo != null)
                openGroupInfo(groupInfo);*/
            return false;
        }
    }

    public interface OnRecyclerClick {
        void onItemClick(int[] id);
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

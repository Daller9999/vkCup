package com.sunplacestudio.vkgroupscup.ViewAdapter;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.sunplacestudio.vkgroupscup.GroupInfo;
import com.sunplacestudio.vkgroupscup.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
    private volatile List<GroupInfo[]> list;
    private LayoutInflater layoutInflater;
    private OnRecyclerClick onRecyclerClick;
    private ConstraintLayout constraintLayout;

    private TextView textViewName;
    private TextView textViewMembers;
    private TextView textViewDescription;
    private TextView textViewLastPost;

    private volatile boolean isOpen = true;

    public void setOnRecyclerClick(OnRecyclerClick onRecyclerClick) {
        this.onRecyclerClick = onRecyclerClick;
    }

    public void removeIds(int[] ids) {
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
    }

    public RecyclerAdapter(Context context, final ConstraintLayout constraintLayout) {
        layoutInflater = LayoutInflater.from(context);
        this.constraintLayout = constraintLayout;

        textViewName = constraintLayout.findViewById(R.id.groupName);
        textViewMembers = constraintLayout.findViewById(R.id.membersText);
        textViewDescription = constraintLayout.findViewById(R.id.notesText);
        textViewLastPost = constraintLayout.findViewById(R.id.postText);

        Button buttonClose = constraintLayout.findViewById(R.id.buttonClose);
        buttonClose.setOnClickListener((v) -> hideShow());

        Button buttonOpenGroup = constraintLayout.findViewById(R.id.buttonOpenGroup);
        buttonOpenGroup.setOnClickListener((v) -> {
            hideShow();
            // дальше вызываем следующий код по открытию группы
        });

        list = new ArrayList<>();

    }

    private void hideShow() {
        constraintLayout.setVisibility(View.GONE);
        isOpen = true;
    }


    public void addData(GroupInfo[] groupInfos) {
        list.add(groupInfos);
        notifyItemInserted(list.size() - 1);
    }

    private void setList(List<GroupInfo> groupInfos) {
        list = new ArrayList<>();
        for (int i = 0; i < groupInfos.size(); i += 3) {
            GroupInfo[] groupInfosNew = new GroupInfo[3];
            for (int j = 0; j < 3 && j + i < groupInfos.size(); j++)
                groupInfosNew[j] = groupInfos.get(j + i);
            list.add(groupInfosNew);
        }
        notifyDataSetChanged();
    }


    GroupInfo[] getItemText(int id) {
        return list.get(id);
    }

    @Override @NonNull public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.recycler_list_fragment_item, parent, false);
        return new ViewHolder(view);
    }

    @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GroupInfo[] groupInfo = list.get(position);
        if (groupInfo[0] != null) {
            if (groupInfo[0].getName() != null)
                holder.textView1.setText(groupInfo[0].getName());
            if (groupInfo[0].getBitmap() != null)
                holder.imageView1.setImageBitmap(groupInfo[0].getBitmap());
            holder.view1.setVisibility(groupInfo[0].isSelected() ? View.VISIBLE : View.GONE);
        }

        if (groupInfo[1] != null) {
            if (groupInfo[1].getName() != null)
                holder.textView2.setText(groupInfo[1].getName());
            if (groupInfo[1].getBitmap() != null)
                holder.imageView2.setImageBitmap(groupInfo[1].getBitmap());
            holder.view2.setVisibility(groupInfo[1].isSelected() ? View.VISIBLE : View.GONE);
        }

        if (groupInfo[2] != null) {
            if (groupInfo[2].getName() != null)
                holder.textView3.setText(groupInfo[2].getName());
            if (groupInfo[2].getBitmap() != null)
                holder.imageView3.setImageBitmap(groupInfo[2].getBitmap());
            holder.view3.setVisibility(groupInfo[2].isSelected() ? View.VISIBLE : View.GONE);
        }
    }

    @Override public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private TextView textView1;
        private TextView textView2;
        private TextView textView3;

        private ImageView imageView1;
        private ImageView imageView2;
        private ImageView imageView3;

        private View view1;
        private View view2;
        private View view3;

        ViewHolder(View itemView) {
            super(itemView);
            textView1 = itemView.findViewById(R.id.text1);
            textView2 = itemView.findViewById(R.id.text2);
            textView3 = itemView.findViewById(R.id.text3);

            imageView1 = itemView.findViewById(R.id.image1);
            imageView1.setOnLongClickListener(this);
            imageView1.setOnClickListener(this);
            view1 = itemView.findViewById(R.id.select_image1);

            imageView2 = itemView.findViewById(R.id.image2);
            imageView2.setOnLongClickListener(this);
            imageView2.setOnClickListener(this);
            view2 = itemView.findViewById(R.id.select_image2);

            imageView3 = itemView.findViewById(R.id.image3);
            imageView3.setOnLongClickListener(this);
            imageView3.setOnClickListener(this);
            view3 = itemView.findViewById(R.id.select_image3);

        }

        @Override public void onClick(View view) {
            if (!isOpen) return;
            boolean select;
            if (view.getId() == R.id.image1 && list.get(getAdapterPosition())[0] != null) {
                select = !list.get(getAdapterPosition())[0].isSelected();
                list.get(getAdapterPosition())[0].setSelected(select);
                view1.setVisibility(select ? View.VISIBLE : View.GONE);
            } else if (view.getId() == R.id.image2 && list.get(getAdapterPosition())[1] != null) {
                select = !list.get(getAdapterPosition())[1].isSelected();
                list.get(getAdapterPosition())[1].setSelected(select);
                view2.setVisibility(select ? View.VISIBLE : View.GONE);
            } else if (view.getId() == R.id.image3 && list.get(getAdapterPosition())[2] != null) {
                select = !list.get(getAdapterPosition())[2].isSelected();
                list.get(getAdapterPosition())[2].setSelected(select);
                view3.setVisibility(select ? View.VISIBLE : View.GONE);
            }
            List<Integer> delete = new ArrayList<>();
            for (GroupInfo[] groupInfos : list)
                for (GroupInfo groupInfo : groupInfos)
                    if (groupInfo != null && groupInfo.isSelected()) // todo fix
                        delete.add(groupInfo.getId());
            if (onRecyclerClick != null) {
                if (!delete.isEmpty()) {
                    int[] mas = new int[delete.size()];
                    for (int i = 0; i < delete.size(); i++)
                        mas[i] = delete.get(i);
                    onRecyclerClick.onItemClick(mas);
                } else
                    onRecyclerClick.onItemClick(new int[]{});
            }
        }

        @Override public boolean onLongClick(View view) {
            if (!isOpen) return false;
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
                openGroupInfo(groupInfo);
            return false;
        }
    }

    public interface OnRecyclerClick {
        void onItemClick(int[] id);
    }

    private void openGroupInfo(GroupInfo groupInfo) {
        constraintLayout.setVisibility(View.VISIBLE);
        textViewName.setText(groupInfo.getName());
        textViewDescription.setText(groupInfo.getDescription());
        textViewMembers.setText(groupInfo.getStringMembersAndFriends());
        textViewLastPost.setText(groupInfo.getDateString());
        isOpen = false;
    }

}

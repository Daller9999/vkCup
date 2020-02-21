package com.example.vkcupfiles;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.vk.sdk.api.VKDefaultParser;

import java.util.ArrayList;
import java.util.List;

class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
    private List<VkDocsData> list;
    private LayoutInflater layoutInflater;
    private OnRecyclerClick onRecyclerClick;

    void setOnRecyclerClick(OnRecyclerClick onRecyclerClick) {
        this.onRecyclerClick = onRecyclerClick;
    }

    RecyclerAdapter(Context context, List<VkDocsData> list) {
        layoutInflater = LayoutInflater.from(context);
        this.list = list;
    }

    void setList(List<VkDocsData> vkDocsData) {
        this.list = vkDocsData;
        notifyDataSetChanged();
    }

    RecyclerAdapter(Context context) {
        layoutInflater = LayoutInflater.from(context);
        list = new ArrayList<>();
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

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView textViewName;
        private TextView textViewSizeAndOther;
        private TextView textViewOther;

        private ImageView imageView;

        ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageSrc);

            textViewName = itemView.findViewById(R.id.textViewFileName);
            textViewSizeAndOther = itemView.findViewById(R.id.textViewDataSize);
            textViewOther = itemView.findViewById(R.id.textViewDataAny);
        }

        @Override public void onClick(View view) {
            if (onRecyclerClick != null) {
                /*if (view.getId() == R.id.textViewGA2)
                    onRecyclerClick.onItemClick(view, getAdapterPosition());
                else if (view.getId() == R.id.textViewDelete)
                    onRecyclerClick.onItemDeleteClick(getAdapterPosition());*/
            }
        }
    }

    public interface OnRecyclerClick {
        void onItemClick(View view, int position);
        void onItemDeleteClick(int position);
    }
}

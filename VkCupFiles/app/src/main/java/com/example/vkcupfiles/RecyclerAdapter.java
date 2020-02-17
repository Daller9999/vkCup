package com.example.vkcupfiles;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
    private List<String> list;
    private LayoutInflater layoutInflater;
    private OnRecyclerClick onRecyclerClick;

    void setOnRecyclerClick(OnRecyclerClick onRecyclerClick) {
        this.onRecyclerClick = onRecyclerClick;
    }

    RecyclerAdapter(Context context, List<String> list) {
        layoutInflater = LayoutInflater.from(context);
        this.list = list;
    }

    RecyclerAdapter(Context context) {
        layoutInflater = LayoutInflater.from(context);
        list = new ArrayList<>();
    }

    void addRow(String text) {
        list.add(text);
        notifyItemInserted(list.indexOf(text));
    }

    void addRowNotActive(String text) {
        list.add(text);
        notifyItemInserted(list.indexOf(text));
    }

    void addRow(String text, int position) {
        list.add(position, text);
        notifyItemInserted(position);
    }

    void updateRow(String text, int position) {
        list.set(position, text);
        notifyItemChanged(position);
    }

    void updateRowNonActive(String text, int position) {
        list.set(position, text);
        notifyItemChanged(position);
    }

    void removeRow(int position) {
        list.remove(position);
        notifyItemRemoved(position);
    }

    void clear() {
        list.clear();
        notifyDataSetChanged();
    }

    String getItemText(int id) {
        return list.get(id);
    }

    @Override @NonNull public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.recycler_lits_item, parent, false);
        return new ViewHolder(view);
    }

    @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String text = list.get(position);
        holder.textView.setText(text);
    }

    @Override public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView textView;

        ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textViewMain);
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

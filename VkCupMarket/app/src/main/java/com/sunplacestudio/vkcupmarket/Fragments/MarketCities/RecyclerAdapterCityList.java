package com.sunplacestudio.vkcupmarket.Fragments.MarketCities;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sunplacestudio.vkcupmarket.Markets.CityInfo;
import com.sunplacestudio.vkcupmarket.R;

import java.util.ArrayList;
import java.util.List;

public class RecyclerAdapterCityList extends RecyclerView.Adapter<RecyclerAdapterCityList.ViewHolder> {
    private volatile List<CityInfo> list;
    private LayoutInflater layoutInflater;
    private int checkedPos = -1;
    private CityInfo selectedCity;

    public RecyclerAdapterCityList(Context context) {
        layoutInflater = LayoutInflater.from(context);
        this.list = new ArrayList<>();
    }

    void setList(List<CityInfo> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @Override @NonNull public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.market_city_item, parent, false);
        return new ViewHolder(view);
    }

    @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CityInfo cityInfo = list.get(position);
        holder.textViewCityName.setText(cityInfo.getName());
        holder.viewChecked.setVisibility(position == checkedPos ? View.VISIBLE : View.GONE);
    }

    @Override public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView textViewCityName;
        private View viewChecked;
        private ConstraintLayout constraintLayout;

        ViewHolder(View itemView) {
            super(itemView);
            textViewCityName = itemView.findViewById(R.id.textViewCityName);
            viewChecked = itemView.findViewById(R.id.viewSelected);
            constraintLayout = itemView.findViewById(R.id.constraintItemCity);
            constraintLayout.setOnClickListener(this);
        }

        @Override public void onClick(View view) {
            if (getAdapterPosition() == checkedPos) {
                checkedPos = -1;
                selectedCity = null;
            } else {
                checkedPos = getAdapterPosition();
                selectedCity = list.get(checkedPos);
            }
            notifyDataSetChanged();
        }
    }

    public CityInfo getSelectedCity() { return selectedCity; }

    public interface OnRecyclerCityListener {
        void onClose(CityInfo cityInfo);
    }

}

package com.sunplacestudio.vkcupmarket.Fragments.MarketCities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sunplacestudio.vkcupmarket.Markets.MarketInfo;
import com.sunplacestudio.vkcupmarket.R;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.os.Handler;

import static java.lang.Thread.sleep;

public class RecyclerAdapterCitiesMarkets extends RecyclerView.Adapter<RecyclerAdapterCitiesMarkets.ViewHolder> {
    private volatile List<MarketInfo> list;
    private volatile List<MarketInfo> copy;
    private volatile List<String> https;
    private volatile boolean[] httpsNeed;
    private LayoutInflater layoutInflater;
    private ExecutorService executorService = Executors.newCachedThreadPool();
    private volatile boolean run = false;
    private volatile boolean newData = false;
    private OnMarketListener onMarketListener;

    public void setOnMarketListener(OnMarketListener onMarketListener) {
        this.onMarketListener = onMarketListener;
    }

    public RecyclerAdapterCitiesMarkets(Context context) {
        layoutInflater = LayoutInflater.from(context);
        list = new ArrayList<>();
    }

    void setList(List<MarketInfo> marketInfoList) {
        if (run) {
            run = false;
            newData = true;
            copy = new ArrayList<>(marketInfoList);
        } else {
            list = new ArrayList<>(marketInfoList);
            notifyDataSetChanged();
            setHttps();
            loadImages();
        }
    }

    private void setHttps() {
        https = new ArrayList<>();
        httpsNeed = new boolean[list.size()];
        for (int i = 0; i < list.size(); i++) {
            httpsNeed[i] = list.get(i).getBitmap() == null;
            https.add(list.get(i).getPhotoUrl());
        }
    }

    /*GroupInfo[] getItemText(int id) {
        return list.get(id);
    }*/

    @Override @NonNull public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.market_info_item_recycler, parent, false);
        return new ViewHolder(view);
    }

    @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MarketInfo marketInfo = list.get(position);
        holder.imageView.setImageBitmap(marketInfo.getBitmap());
        holder.textViewName.setText(marketInfo.getName());
        holder.textViewType.setText(marketInfo.getType());
    }

    @Override public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private TextView textViewName;
        private TextView textViewType;
        private ImageView imageView;

        private ConstraintLayout constraintLayout;

        ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            textViewName = itemView.findViewById(R.id.textViewGroupName);
            textViewType = itemView.findViewById(R.id.textViewGroupType);
            constraintLayout = itemView.findViewById(R.id.constraintListMarkets);
            constraintLayout = itemView.findViewById(R.id.constraintItemMarket);
            constraintLayout.setOnClickListener(this);
        }

        @Override public void onClick(View view) {
            if (onMarketListener != null) {
                onMarketListener.onClickMarket(list.get(getAdapterPosition()));
            }
        }
    }

    private Handler handler = new Handler();
    private void loadImages() {
        run = true;
        executorService.execute(() -> {
            for (int i = 0; i < https.size() && run; i++) {
                if (httpsNeed[i]) {
                    String http = https.get(i);
                    Bitmap bitmap = loadPhoto(http);
                    final int pos = i;
                    handler.post(() -> updateImage(bitmap, pos));
                }
            }
            overLoad();
        });
    }

    private void overLoad() {
        if (newData) {
            handler.post(() -> {
                list = new ArrayList<>(copy);
                notifyDataSetChanged();
                setHttps();
                copy.clear();
                loadImages();
                newData = false;
            });
        } else
            run = false;
    }

    private void updateImage(Bitmap bitmap, int pos) {
        if (pos >= list.size()) return;
        list.get(pos).setBitmap(bitmap);
        notifyItemChanged(pos);
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

    public interface OnMarketListener {
        void onClickMarket(MarketInfo marketInfo);
    }

}

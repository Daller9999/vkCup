package com.sunplacestudio.vkcupmarket.Fragments.Goods;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sunplacestudio.vkcupmarket.Markets.ProductInfo;
import com.sunplacestudio.vkcupmarket.R;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RecyclerAdapterGoods extends RecyclerView.Adapter<RecyclerAdapterGoods.ViewHolder> {
    private volatile List<ProductInfo[]> list;
    private volatile List<String[]> https;
    private volatile List<boolean[]> httpsNeed;
    private LayoutInflater layoutInflater;
    private OnProductListener productListener;
    private ExecutorService executorService = Executors.newCachedThreadPool();

    public void setProductListener(OnProductListener productListener) {
        this.productListener = productListener;
    }

    public RecyclerAdapterGoods(Context context) {
        layoutInflater = LayoutInflater.from(context);
        list = new ArrayList<>();
        https = new ArrayList<>();
        httpsNeed = new ArrayList<>();
    }

    public List<ProductInfo> getListAll() {
        List<ProductInfo> list = new ArrayList<>();
        for (ProductInfo[] productInfos : this.list)
            for (ProductInfo productInfo : productInfos)
                list.add(productInfo);
        return list;
    }

    void addList(List<ProductInfo> productInfos) {
        ProductInfo productInfo;
        for (int i = 0; i < productInfos.size(); i += 2) {
            ProductInfo[] groupInfosNew = new ProductInfo[2];
            String[] strings = new String[2];
            boolean[] booleans = new boolean[2];
            for (int j = 0; j < 2 && j + i < productInfos.size(); j++) {
                productInfo = productInfos.get(j + i);
                if (productInfo != null) {
                    groupInfosNew[j] = productInfo;
                    strings[j] = productInfo.getHttpBitmap();
                    booleans[j] = productInfo.getBitmap() == null;
                }
            }
            httpsNeed.add(booleans);
            https.add(strings);
            list.add(groupInfosNew);

        }
        if (!run)
            loadImages();
        notifyDataSetChanged();
    }


    @Override @NonNull public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.recycler_goods_item, parent, false);
        return new ViewHolder(view);
    }

    @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProductInfo[] productInfo = list.get(position);
        for (int i = 0; i < productInfo.length; i++) {
            if (productInfo[i] != null) {
                holder.imageViews[i].setImageBitmap(productInfo[i].getBitmap());
                holder.textViewsCost[i].setText(productInfo[i].getCost());
                holder.textViewsName[i].setText(productInfo[i].getName());
            } else {
                holder.imageView1.setImageBitmap(null);
                holder.textViewCost1.setText("");
                holder.textViewName1.setText("");
            }
        }
    }

    @Override public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView textViewName1;
        private TextView textViewName2;
        private TextView textViewCost1;
        private TextView textViewCost2;

        private ImageView imageView1;
        private ImageView imageView2;

        private ImageView[] imageViews;
        private TextView[] textViewsName;
        private TextView[] textViewsCost;

        ViewHolder(View itemView) {
            super(itemView);
            textViewName1 = itemView.findViewById(R.id.textViewGoodsName1);
            textViewCost1 = itemView.findViewById(R.id.textViewGoodsCost1);
            imageView1 = itemView.findViewById(R.id.imageView1);
            imageView1.setOnClickListener(this);

            textViewName2 = itemView.findViewById(R.id.textViewGoodsName2);
            textViewCost2 = itemView.findViewById(R.id.textViewGoodsCost2);
            imageView2 = itemView.findViewById(R.id.imageView2);
            imageView2.setOnClickListener(this);

            textViewsName = new TextView[]{textViewName1, textViewName2};
            textViewsCost = new TextView[]{textViewCost1, textViewCost2};
            imageViews = new ImageView[]{imageView1, imageView2};
        }

        @Override public void onClick(View view) {
            if (productListener == null) return;
            ProductInfo[] productInfos = list.get(getAdapterPosition());
            if (view.getId() == R.id.imageView1)
                productListener.onProductClicked(productInfos[0]);
            else if (view.getId() == R.id.imageView2)
                productListener.onProductClicked(productInfos[1]);
        }
    }


    private boolean run = false;
    private Handler handler = new Handler();
    private void loadImages() {
        run = true;
        executorService.execute(() -> {
            for (int i = 0; i < https.size() && run; i++) {
                String[] http = https.get(i);
                for (int j = 0; j < http.length; j++) {
                    String http0 = http[j];
                    if (http0 != null && httpsNeed.get(i)[j]) {
                        Bitmap bitmap = loadPhoto(http0);
                        final int pos = i;
                        final int column = j;
                        handler.post(() -> updateImage(bitmap, pos, column));
                    }
                }
            }
            run = false;
        });
    }

    private void updateImage(Bitmap bitmap, int pos, int column) {
        if (pos >= list.size()) return;
        list.get(pos)[column].setBitmap(bitmap);
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

    public interface OnProductListener {
        void onProductClicked(ProductInfo productInfo);
    }

}

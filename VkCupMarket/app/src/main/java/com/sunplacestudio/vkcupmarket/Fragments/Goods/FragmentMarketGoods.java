package com.sunplacestudio.vkcupmarket.Fragments.Goods;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sunplacestudio.vkcupmarket.MainActivity;
import com.sunplacestudio.vkcupmarket.Markets.MarketInfo;
import com.sunplacestudio.vkcupmarket.Markets.ProductInfo;
import com.sunplacestudio.vkcupmarket.R;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.Thread.MAX_PRIORITY;
import static java.lang.Thread.sleep;

public class FragmentMarketGoods extends Fragment {

    private MarketInfo marketInfo;
    private RecyclerAdapterGoods recyclerAdapterGoods;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private List<ProductInfo> productInfos = new ArrayList<>();

    @Override public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_goods_list, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewGoods);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        recyclerAdapterGoods = new RecyclerAdapterGoods(view.getContext());
        recyclerView.setAdapter(recyclerAdapterGoods);
        recyclerAdapterGoods.setProductListener((productInfo) -> {
            if (getActivity() != null)
                ((MainActivity) getActivity()).loadFragmentProductInfo(productInfo);
        });

        TextView textViewName = view.findViewById(R.id.textViewMarketName);
        String text = "Товары сообщества " + marketInfo.getName();
        textViewName.setText(text);

        Button buttonBack = view.findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener((v) -> {
            if (getActivity() != null)
                ((MainActivity) getActivity()).popBackStack();
        });
        if (productInfos.isEmpty())
            loadAllGoods();
        else
            recyclerAdapterGoods.addList(productInfos);
        return view;
    }

    public void setMarketInfo(MarketInfo marketInfo) {
        this.marketInfo = marketInfo;
    }

    private void loadAllGoods() {
        executorService.execute(new LoadAllData());
    }

    private class LoadAllData implements Runnable {
        private boolean wait = false;
        private int offset = 0;
        private int count = 1;

        @Override public void run() {
            while (count > 0) {
                VKRequest vkRequest = new VKRequest("market.get",
                        VKParameters.from(VKApiConst.OWNER_ID, -marketInfo.getId(), "count", 200, "offset", offset));
                wait = true;
                vkRequest.executeWithListener(vkRequestListener);
                try {
                    while (wait)
                        sleep(50);
                } catch (InterruptedException ex) {
                    //
                }
                offset += 200;
            }
        }

        VKRequest.VKRequestListener vkRequestListener = new VKRequest.VKRequestListener() {
            @Override public void onComplete(VKResponse response) {
                super.onComplete(response);
                try {
                    JSONArray jsonArray = response.json.getJSONObject("response").getJSONArray("items");
                    count = jsonArray.length();
                    productInfos = new ArrayList<>();
                    ProductInfo productInfo;
                    String name, cost, http, description;
                    int id;
                    JSONObject jsonObject;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        jsonObject = jsonArray.getJSONObject(i);
                        name = jsonObject.getString("title");
                        cost = jsonObject.getJSONObject("price").getString("text").replace("rub", "₽");
                        id = jsonObject.getInt("id");
                        http = jsonObject.getString("thumb_photo");
                        description = jsonObject.getString("description");
                        productInfo = new ProductInfo(name, id, http, cost, description);
                        productInfos.add(productInfo);
                    }
                    recyclerAdapterGoods.addList(productInfos);
                    wait = false;
                } catch (JSONException ex) {
                    //
                }
            }

            @Override public void onError(VKError error) {
                super.onError(error);
                Toast.makeText(getContext(), "Проблема при загрузке данных", Toast.LENGTH_SHORT).show();
            }
        };
    }
}

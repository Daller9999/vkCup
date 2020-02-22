package com.sunplacestudio.vkcupmarket.Fragments.MarketCities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sunplacestudio.vkcupmarket.Markets.CityInfo;
import com.sunplacestudio.vkcupmarket.MainActivity;
import com.sunplacestudio.vkcupmarket.Markets.MarketInfo;
import com.sunplacestudio.vkcupmarket.R;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class FragmentMarket extends Fragment {
    private ConstraintLayout constraintLayoutCityList;
    private ConstraintLayout constraintLayoutMarketList;
    private RecyclerAdapterCityList recyclerAdapterCityList;
    private RecyclerAdapterCitiesMarkets recyclerAdapterCitiesMarkets;

    private HashMap<Integer, List<MarketInfo>> hashMap = new HashMap<>();
    private List<MarketInfo> marketInfoList = new ArrayList<>();
    private Vector<Integer> citiesIds = new Vector<>();
    private List<CityInfo> cityInfoList;
    private Button buttonCloseCityList;

    private long load;
    private CityInfo cityInfo = null;

    @Override public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_main_cities_market, container, false);

        constraintLayoutCityList = view.findViewById(R.id.constraintListCities);
        constraintLayoutCityList.setVisibility(View.VISIBLE);

        constraintLayoutMarketList = view.findViewById(R.id.constraintListMarkets);
        constraintLayoutMarketList.setVisibility(View.GONE);

        RecyclerView recyclerViewListCities = view.findViewById(R.id.recyclerViewCitiesList);
        recyclerViewListCities.setLayoutManager(new LinearLayoutManager(view.getContext()));
        recyclerAdapterCityList = new RecyclerAdapterCityList(view.getContext());
        recyclerViewListCities.setAdapter(recyclerAdapterCityList);

        RecyclerView recyclerViewMarketList = view.findViewById(R.id.recyclerViewMarketList);
        recyclerViewMarketList.setLayoutManager(new LinearLayoutManager(view.getContext()));
        recyclerAdapterCitiesMarkets = new RecyclerAdapterCitiesMarkets(view.getContext());
        recyclerAdapterCitiesMarkets.setOnMarketListener((marketInfo) -> {
            if (getActivity() != null)
                ((MainActivity) getActivity()).loadFragmentMarketGoods(marketInfo);
        });
        recyclerViewMarketList.setAdapter(recyclerAdapterCitiesMarkets);

        TextView textViewCityCurrent = view.findViewById(R.id.textViewMarketCities);

        buttonCloseCityList = view.findViewById(R.id.buttonCloseCityList);
        buttonCloseCityList.setOnClickListener((v) -> {
            CityInfo cityInfo = recyclerAdapterCityList.getSelectedCity();
            if (cityInfo != null) {
                this.cityInfo = cityInfo;
                String text = "Магазины в " + cityInfo.getName();
                textViewCityCurrent.setText(text);
                List<MarketInfo> infos = hashMap.get(recyclerAdapterCityList.getSelectedCity().getId());
                if (infos != null)
                    recyclerAdapterCitiesMarkets.setList(infos);
            }
            setMarketVisible();
        });

        Button buttonOpenCityList = view.findViewById(R.id.buttonDropDown);
        buttonOpenCityList.setOnClickListener((v) -> {
            constraintLayoutCityList.setVisibility(View.VISIBLE);
            constraintLayoutMarketList.setVisibility(View.GONE);
        });


        load = System.currentTimeMillis();
        if (citiesIds.isEmpty())
            loadCities();
        else {
            recyclerAdapterCityList.setList(cityInfoList);
            recyclerAdapterCitiesMarkets.setList(hashMap.get(cityInfo.getId()));
            textViewCityCurrent.setText("Магазины в " + cityInfo.getName());
            setMarketVisible();
        }
        return view;
    }

    private void setMarketVisible() {
        constraintLayoutCityList.setVisibility(View.GONE);
        constraintLayoutMarketList.setVisibility(View.VISIBLE);
    }

    private void loadCities() {
        VKRequest vkRequest = new VKRequest("database.getCities", VKParameters.from("country_id", 1, "count", 1000));
        vkRequest.executeWithListener(new VKRequest.VKRequestListener() {
            @Override public void onComplete(VKResponse response) {
                super.onComplete(response);
                try {
                    JSONObject jsonObject = (JSONObject) response.json.get("response");
                    JSONArray jsonArray = jsonObject.getJSONArray("items");
                    cityInfoList = new ArrayList<>();
                    String name;
                    int id;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = (JSONObject) jsonArray.get(i);
                        name = object.getString("title");
                        id = object.getInt("id");
                        citiesIds.addElement(id);
                        cityInfoList.add(new CityInfo(name, id));
                    }
                    recyclerAdapterCityList.setList(cityInfoList);
                } catch (JSONException ex) {
                    //
                }
                loadAllMarkets();
            }

            @Override public void onError(VKError error) {
                super.onError(error);
                showErrorMessage("Произошла ошибка при загрузке списка городов, проверьте соединение с интернетом");
            }
        });
    }

    private void showErrorMessage(String text) {
        Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
    }

    private void loadAllMarkets() {
        VKRequest vkRequest = new VKRequest("groups.getCatalog", VKParameters.from("category_id", 11));
        vkRequest.executeWithListener(new VKRequest.VKRequestListener() {
            @Override public void onComplete(VKResponse response) {
                super.onComplete(response);
                try {
                    JSONObject jsonObject = (JSONObject) response.json.get("response");
                    JSONArray jsonArray = jsonObject.getJSONArray("items");
                    marketInfoList = new ArrayList<>();
                    String name, http;
                    int id, type;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = (JSONObject) jsonArray.get(i);
                        name = object.getString("name");
                        id = object.getInt("id");
                        http = object.getString("photo_100");
                        type = object.getInt("is_closed");
                        marketInfoList.add(new MarketInfo(name, id, http, type));
                    }
                    loadInfoAboutMarkets();
                } catch (JSONException ex) {
                    //
                }
            }

            @Override public void onError(VKError error) {
                super.onError(error);
                showErrorMessage("Произошла ошибка при загрузке данных, проверьте соединение с интернетом");
            }
        });
    }

    private boolean run = true;

    private void loadInfoAboutMarkets() {
        run = true;
        new LoadThread().start();
    }

    private class LoadThread extends Thread {

        private volatile boolean wait = true;
        private int counter = 0;

        @Override public void run() {
            List<MarketInfo> marketInfoList = new ArrayList<>(FragmentMarket.this.marketInfoList);
            StringBuilder stringBuilder;
            for (counter = 0; counter < marketInfoList.size() && run; counter += 500) {
                stringBuilder = new StringBuilder();
                for (int j = 0; j + counter < marketInfoList.size() && j < 500 && run; j++) {
                    stringBuilder.append(String.valueOf(marketInfoList.get(counter + j).getId()));
                    if (j + 1 < 500 && j + counter + 1 < marketInfoList.size())
                        stringBuilder.append(",");
                }
                String ids = stringBuilder.toString();
                VKRequest vkRequest = new VKRequest("groups.getById", VKParameters.from("group_ids", ids, "fields", "city"));
                wait = true;
                vkRequest.executeWithListener(vkRequestListener);
                try { while (wait) sleep(100); } catch (InterruptedException ex) {}
            }
            Log.e("mesUri", "all load is : " + (System.currentTimeMillis() - load));
        }

        private VKRequest.VKRequestListener vkRequestListener = new VKRequest.VKRequestListener() {
            @Override public void onComplete(VKResponse response) {
                super.onComplete(response);
                    JSONArray jsonArray = null;
                    try {
                        jsonArray = response.json.getJSONArray("response");
                    } catch (JSONException ex) {
                        //
                    }
                    String name;
                    int id = -1;
                    JSONObject jsonObject;
                    if (jsonArray == null) return;

                    for (int i = 0; i < jsonArray.length(); i++) {
                        name = null;
                        try {
                            jsonObject = (JSONObject) ((JSONObject) jsonArray.get(i)).get("city");
                            name = jsonObject.getString("title");
                            id = jsonObject.getInt("id");
                        } catch (JSONException ex) {
                            // int u = 4;
                        }
                        if (name != null && citiesIds.contains(id)) {
                            marketInfoList.get(counter + i).setCity(name, id);
                            if (hashMap.get(id) == null)
                                hashMap.put(id, new ArrayList<>());
                            hashMap.get(id).add(marketInfoList.get(counter + i));
                        }
                    }
                    wait = false;
            }

            @Override public void onError(VKError error) {
                super.onError(error);
                showErrorMessage("Произошла ошибка при загрузке данных, проверьте соединение с интернетом");
            }
        };
    }

    @Override public void onDestroy() {
        super.onDestroy();
        run = false;
    }
}

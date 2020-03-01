package com.sunplacestudio.vkcupmarket;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.sunplacestudio.vkcupmarket.Fragments.FragmentProductInfo;
import com.sunplacestudio.vkcupmarket.Fragments.Goods.FragmentMarketGoods;
import com.sunplacestudio.vkcupmarket.Fragments.MarketCities.FragmentMarket;
import com.sunplacestudio.vkcupmarket.Markets.MarketInfo;
import com.sunplacestudio.vkcupmarket.Markets.ProductInfo;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
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

import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity {

    private int currentFragment;
    private int FRAGMENT_MAIN = 0;
    private int FRAGMENT_GOODS = 1;
    private int FRAGMENT_PRODUCT = 2;
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // String[] fingerprints = VKUtil.getCertificateFingerprint(this, this.getPackageName());

        String[] permissions = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
        };
        if (Build.VERSION.SDK_INT >= 23)
            requestPermissions(permissions, 1);

        String[] otherPermissions = {VKScope.GROUPS, "market"};
        if (!VKSdk.isLoggedIn())
            VKSdk.login(this, otherPermissions);
        else {
            // new LoadAllFaves().start();
            loadFragmentMarketCityList();
        }

        Log.e("mesUri", "api is : " + VKSdk.getApiVersion());
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {

            @Override public void onResult(VKAccessToken res) {
                loadFragmentMarketCityList();
            }

            @Override public void onError(VKError error) {
                Toast.makeText(getApplicationContext(), "Ошибка при входе в ВК, попробуйте перезайти в приложение", Toast.LENGTH_SHORT).show();
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void loadFragmentMarketCityList() {
        currentFragment = FRAGMENT_MAIN;
        getSupportFragmentManager().beginTransaction().replace(R.id.container, new FragmentMarket()).addToBackStack(FragmentMarket.class.getName()).commit();
    }

    public void loadFragmentMarketGoods(MarketInfo marketInfo) {
        currentFragment = FRAGMENT_GOODS;
        FragmentMarketGoods fragmentMarket = new FragmentMarketGoods();
        fragmentMarket.setMarketInfo(marketInfo);
        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragmentMarket).addToBackStack(FragmentMarketGoods.class.getName()).commit();
    }

    public void loadFragmentProductInfo(int id, ProductInfo productInfo) {
        currentFragment = FRAGMENT_PRODUCT;
        FragmentProductInfo fragmentProductInfo = new FragmentProductInfo();
        fragmentProductInfo.setProductInfo(id, productInfo);
        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragmentProductInfo).addToBackStack(FragmentProductInfo.class.getName()).commit();
    }

    public void popBackStack() {
        if (currentFragment == FRAGMENT_PRODUCT)
            currentFragment = FRAGMENT_GOODS;
        else if (currentFragment == FRAGMENT_GOODS)
            currentFragment = FRAGMENT_MAIN;
        getSupportFragmentManager().popBackStack();
    }

    @Override public void onBackPressed() {
        if (currentFragment != FRAGMENT_MAIN)
            popBackStack();
        else {
            moveTaskToBack(true);
            finish();
        }
    }
}
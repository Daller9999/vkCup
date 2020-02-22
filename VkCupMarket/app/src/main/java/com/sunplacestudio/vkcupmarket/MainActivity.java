package com.sunplacestudio.vkcupmarket;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
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
import com.vk.sdk.api.VKError;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
            loadFragmentMarketCityList();
        }

        Log.e("mesUri", "api is : " + VKSdk.getApiVersion());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
        getSupportFragmentManager().beginTransaction().replace(R.id.container, new FragmentMarket()).addToBackStack(FragmentMarket.class.getName()).commit();
    }

    public void loadFragmentMarketGoods(MarketInfo marketInfo) {
        FragmentMarketGoods fragmentMarket = new FragmentMarketGoods();
        fragmentMarket.setMarketInfo(marketInfo);
        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragmentMarket).addToBackStack(FragmentMarketGoods.class.getName()).commit();
    }

    public void loadFragmentProductInfo(ProductInfo productInfo) {
        FragmentProductInfo fragmentProductInfo = new FragmentProductInfo();
        fragmentProductInfo.setProductInfo(productInfo);
        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragmentProductInfo).addToBackStack(FragmentProductInfo.class.getName()).commit();
    }

    public void popBackStack() {
        getSupportFragmentManager().popBackStack();
    }
}

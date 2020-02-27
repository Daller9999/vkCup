package com.sunplacestudio.vkcupmarket.Fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sunplacestudio.vkcupmarket.MainActivity;
import com.sunplacestudio.vkcupmarket.Markets.ProductInfo;
import com.sunplacestudio.vkcupmarket.R;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import org.json.JSONException;

import java.io.InputStream;
import java.util.List;

import static android.support.v7.content.res.AppCompatResources.getDrawable;


public class FragmentProductInfo extends Fragment {

    private ProductInfo productInfo;
    private Drawable drawableSelect;
    private Drawable drawableUnSelect;
    private int colorUn = Color.parseColor("#3F8AE0");
    private int colorSelect = Color.WHITE;
    private boolean added = false;
    private final String add = "Добавить в избранное";
    private final String noAdd = "Удалить из избранного";


    private ImageView imageView;
    private Handler handler = new Handler();
    private int count = 0;
    private int marketId;

    @Override public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_product_info_view, container, false);

        drawableSelect = getDrawable(view.getContext(), R.drawable.button_select);
        drawableUnSelect = getDrawable(view.getContext(), R.drawable.button_not_selected);

        /*if (getActivity() != null) {
            List<Integer> faves = ((MainActivity) getActivity()).getFavesId();
            added = faves.contains(productInfo.getId());
        }*/

        Button buttonSelect = view.findViewById(R.id.buttonAddProduct);
        buttonSelect.setOnClickListener((v) -> {
            added = !added;
            buttonSelect.setTextColor(added ? colorUn : colorSelect);
            buttonSelect.setBackground(added ? drawableUnSelect : drawableSelect);
            buttonSelect.setText(added ? noAdd : add);
            makeFave(added);
        });

        buttonSelect.setTextColor(added ? colorUn : colorSelect);
        buttonSelect.setBackground(added ? drawableUnSelect : drawableSelect);
        buttonSelect.setText(added ? noAdd : add);

        TextView textViewName1 = view.findViewById(R.id.textViewProductName);
        TextView textViewNam2 = view.findViewById(R.id.textViewProductName2);
        textViewNam2.setText(productInfo.getName());
        textViewName1.setText(productInfo.getName());

        imageView = view.findViewById(R.id.imageView);
        if (productInfo.getBitmap() == null)
            loadImage();
        else
            imageView.setImageBitmap(productInfo.getBitmap());

        TextView textViewCost = view.findViewById(R.id.textViewProductCost);
        textViewCost.setText(productInfo.getCost());

        TextView textViewDescription = view.findViewById(R.id.textViewDescription);
        textViewDescription.setText(productInfo.getDescription());

        Button buttonBack = view.findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener((v) -> {
            if (getActivity() != null)
                ((MainActivity) getActivity()).popBackStack();
        });

        return view;
    }

    private void makeFave(boolean b) {
        String coomand, mes1, mes2;
        if (b) {
            coomand = "fave.addProduct";
            mes1 = "Товар успешно добавлен в закладки";
            mes2 = "Произошла ошибка при добавлении товара в закладки";
        } else {
            coomand = "fave.removeProduct";
            mes1 = "Товар успешно удалён из закладок";
            mes2 = "Не удалось удалить товар из закладом";
        }
        VKRequest vkRequest = new VKRequest(coomand, VKParameters.from("owner_id", -marketId, "id",  productInfo.getId()));
        vkRequest.executeWithListener(new VKRequest.VKRequestListener() {
            @Override public void onComplete(VKResponse response) {
                super.onComplete(response);
                Toast.makeText(getContext(), mes1, Toast.LENGTH_SHORT).show();
            }

            @Override public void onError(VKError error) {
                super.onError(error);
                Toast.makeText(getContext(), mes2, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setProductInfo(int marketId, ProductInfo productInfo) {
        this.productInfo = productInfo;
        this.marketId = marketId;
    }

    private void loadImage() {
        new Thread(() -> {
            Bitmap bitmap = null;
            try {
                InputStream in = new java.net.URL(productInfo.getHttpBitmap()).openStream();
                bitmap = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("mesUri", "error to load image : " + e.getMessage());
            }
            final Bitmap bitmap1 = bitmap;
            handler.post(() -> imageView.setImageBitmap(bitmap1));
        }).start();
    }
}

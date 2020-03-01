package com.sunplacestudio.vkcupmaps;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;
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
import com.vk.sdk.api.model.VKApiCommunityArray;
import com.vk.sdk.api.model.VKApiCommunityFull;
import com.vk.sdk.api.model.VKApiPlace;
import com.vk.sdk.api.model.VKApiPost;
import com.vk.sdk.api.model.VKPostArray;
import com.vk.sdk.util.VKUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.Thread.MAX_PRIORITY;
import static java.lang.Thread.dumpStack;
import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity {

    private GoogleMap googleMap;
    private GoogleApiClient googleApiClient;
    private static final String LOG_TAG = "mesUri";
    private long lastTimeRequest = 0;
    private boolean overVkRequest = true;
    private List<MarkerInfo> markerInfoList = new ArrayList<>();
    private List<MarkerInfo> markerInfosGroups = new ArrayList<>();
    private ExecutorService executorService = Executors.newCachedThreadPool();
    private Handler handler = new Handler();


    private ConstraintLayout constraintLayoutPhoto;
    private ImageView imageView;
    private Button buttonBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String[] fingerprints = VKUtil.getCertificateFingerprint(this, this.getPackageName());

        String[] permissions = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
        };
        if (Build.VERSION.SDK_INT >= 23)
            requestPermissions(permissions, 1);

        String[] otherPermissions = {VKScope.GROUPS, VKScope.FRIENDS};
        if (!VKSdk.isLoggedIn())
            VKSdk.login(this, otherPermissions);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync((googleMap) -> initMap(googleMap));

        constraintLayoutPhoto = findViewById(R.id.layout_photo);
        imageView = findViewById(R.id.imageView);
        constraintLayoutPhoto.setVisibility(View.GONE);
        buttonBack = findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener((v) -> constraintLayoutPhoto.setVisibility(View.GONE));

        int colorGray = Color.parseColor("#99A2AD");
        int colorBlack = Color.BLACK;
        TextView textViewPhoto = findViewById(R.id.textViewPhoto);
        textViewPhoto.setTextColor(colorBlack);
        TextView textViewEvent = findViewById(R.id.textViewEvent);
        textViewEvent.setTextColor(colorGray);
        TextView textViewGroups = findViewById(R.id.textViewGroups);
        textViewGroups.setTextColor(colorGray);

        View textViewPhotoLine = findViewById(R.id.textViewPhotoLine);
        View textViewEventLine = findViewById(R.id.textViewEventLine);
        textViewEventLine.setVisibility(View.GONE);
        View textViewGroupsLine = findViewById(R.id.textViewGroupsLine);
        textViewGroupsLine.setVisibility(View.GONE);

        View.OnClickListener onClickListenerLine = (v) -> {
            textViewGroupsLine.setVisibility(View.GONE);
            textViewGroups.setTextColor(colorGray);
            textViewEventLine.setVisibility(View.GONE);
            textViewEvent.setTextColor(colorGray);
            textViewPhotoLine.setVisibility(View.GONE);
            textViewPhoto.setTextColor(colorGray);

            if (v.getId() == R.id.textViewEvent) {
                textViewEventLine.setVisibility(View.VISIBLE);
                textViewEvent.setTextColor(colorBlack);
            } else if (v.getId() == R.id.textViewPhoto) {
                textViewPhotoLine.setVisibility(View.VISIBLE);
                textViewPhoto.setTextColor(colorBlack);
            } else if (v.getId() == R.id.textViewGroups) {
                textViewGroupsLine.setVisibility(View.VISIBLE);
                textViewGroups.setTextColor(colorBlack);
            }
        };
        textViewPhoto.setOnClickListener(onClickListenerLine);
        textViewGroups.setOnClickListener(onClickListenerLine);
        textViewEvent.setOnClickListener(onClickListenerLine);
    }


    private void initMap(GoogleMap googleMap) {
        this.googleMap = googleMap;
        this.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(55.752828, 37.623191) , 14.0f) );
        this.googleMap.setOnCameraMoveListener(onCameraMoveListener);
        this.googleMap.setOnMarkerClickListener(onMarkerClickListener);
    }

    private GoogleMap.OnMarkerClickListener onMarkerClickListener = (marker) -> {
        for (MarkerInfo markerInfo : markerInfoList)
            if (markerInfo.getLatLng().equals(marker.getPosition()))
                showImage(markerInfo.getBitmapView());
                // Log.i(LOG_TAG, "found marker " + marker.getPosition().toString());
        return false;
    };

    private void showImage(Bitmap bitmap) {
        constraintLayoutPhoto.setVisibility(View.VISIBLE);
        imageView.setImageBitmap(bitmap);
    }

    private double getMapVisibleRadius() {
        VisibleRegion visibleRegion = googleMap.getProjection().getVisibleRegion();

        float[] distanceWidth = new float[1];
        float[] distanceHeight = new float[1];

        LatLng farRight = visibleRegion.farRight;
        LatLng farLeft = visibleRegion.farLeft;
        LatLng nearRight = visibleRegion.nearRight;
        LatLng nearLeft = visibleRegion.nearLeft;

        Location.distanceBetween(
                (farLeft.latitude + nearLeft.latitude) / 2,
                farLeft.longitude,
                (farRight.latitude + nearRight.latitude) / 2,
                farRight.longitude,
                distanceWidth
        );

        Location.distanceBetween(
                farRight.latitude,
                (farRight.longitude + farLeft.longitude) / 2,
                nearRight.latitude,
                (nearRight.longitude + nearLeft.longitude) / 2,
                distanceHeight
        );

        return Math.sqrt(Math.pow(distanceWidth[0], 2) + Math.pow(distanceHeight[0], 2)) / 2;
    }

    private GoogleMap.OnCameraMoveListener onCameraMoveListener = () -> {
        if (System.currentTimeMillis() - lastTimeRequest > 1500 && overVkRequest) {
            LatLng latLng = googleMap.getCameraPosition().target;
            double mapLatitude = latLng.latitude;
            double mapLongitude = latLng.longitude;
            int radius = roundRadius(getMapVisibleRadius());
            overVkRequest = false;
            createPhotoVkMapRequest(mapLatitude, mapLongitude, radius);
            lastTimeRequest = System.currentTimeMillis();
            Log.i(LOG_TAG, "pos = " + latLng.toString() + " ; radius is " + radius);
        }
    };

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {

            @Override public void onResult(VKAccessToken res) {
                // Пользователь успешно авторизовался
            }

            @Override public void onError(VKError error) {
                Toast.makeText(getApplicationContext(), "Ошибка при входе в ВК, попробуйте перезайти в приложение", Toast.LENGTH_SHORT).show();
                // Произошла ошибка авторизации (например, пользователь запретил авторизацию)
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void createPhotoVkMapRequest(double latitude, double longitude, int radius) {
        VKParameters vkParameters = VKParameters.from("q", "*", "lat", latitude, "long", longitude, "count", 50, "radius", radius);
        VKRequest vkRequest = new VKRequest("photos.search", vkParameters);
        vkRequest.executeWithListener(new VKRequest.VKRequestListener() {
            @Override public void onComplete(VKResponse response) {
                super.onComplete(response);
                MarkerInfo markerInfo;
                JSONObject jsonObject;
                JSONArray jsonArraySizes;
                try {
                    JSONArray jsonArray = response.json.getJSONObject("response").getJSONArray("items");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        jsonObject = jsonArray.getJSONObject(i);

                        jsonArraySizes = jsonObject.getJSONArray("sizes");
                        String http = jsonArraySizes.getJSONObject(0).getString("url");
                        String httpView = jsonArraySizes.getJSONObject(jsonArraySizes.length() - 2).getString("url");

                        String title = jsonObject.getString("text");
                        int type = 0;
                        int id = jsonObject.getInt("id");
                        double latitude = jsonObject.getDouble("lat");
                        double longitude = jsonObject.getDouble("long");

                        markerInfo = new MarkerInfo(title, http, httpView, type, id, latitude, longitude);
                        markerInfoList.add(markerInfo);
                    }
                } catch (JSONException ex) {
                    //
                }
                updateGoogleMapsMarkets(latitude, longitude, radius);
            }

            @Override public void onError(VKError error) {
                super.onError(error);
                overVkRequest = true;
                Toast.makeText(MainActivity.this, "Произошла ошибка при загрузке данных, проверьте интернет соединение", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateGoogleMapsMarkets(double latitude, double longitude, int radius) {
        double doubleRadius = radius * 1.5;
        double startLat = latitude * 111 * 1000 - doubleRadius;
        double endLat = latitude * 111 * 1000 + doubleRadius;
        double startLong = longitude * 111 * 1000 - doubleRadius;
        double endLong = longitude * 111 * 1000 + doubleRadius;

        googleMap.clear();
        if (markerInfoList.size() >= 1000)
            markerInfoList = markerInfoList.subList(500, 1000);
        executorService.execute(() -> {
            int count = 0;
            List<LatLng> list = new ArrayList<>();
            for (MarkerInfo markerInfo : markerInfoList) {
                if (startLat <= markerInfo.getLatMeters() && markerInfo.getLatMeters() <= endLat &&
                    startLong <= markerInfo.getLongMeters() && markerInfo.getLongMeters() <= endLong &&
                    checkDistance(list, markerInfo.getLatLng(), radius)) {

                    if (markerInfo.getBitmap() == null) {
                        markerInfo.setBitmap(loadPhoto(markerInfo.getHttpPhoto()));
                        markerInfo.setBitmapView(loadPhoto(markerInfo.getHttpView()));
                    }
                    Bitmap bitmap = markerInfo.getBitmap();

                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(markerInfo.getLatLng())
                            .title(markerInfo.getTitle())
                            .icon(BitmapDescriptorFactory.fromBitmap(bitmap));
                    handler.post(() -> googleMap.addMarker(markerOptions));
                    list.add(markerInfo.getLatLng());
                    count++;
                    if (count == 11)
                        break;
                }
            }
            overVkRequest = true;
        });
    }

    private boolean checkDistance(List<LatLng> latLngs, LatLng latLng, double radius) {
        double delta = radius / 100;
        for (LatLng latLngCheck : latLngs)
            if (calculateMeters(latLngCheck, latLng) < delta)
                return false;
        return true;
    }

    private double calculateMeters(LatLng latLng1, LatLng latLng2) {
        float[] res = new float[1];
        Location.distanceBetween(latLng1.latitude, latLng1.longitude, latLng2.latitude, latLng2.longitude, res);
        return res[0];
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

    private int roundRadius(double radius) {
        int[] radiuses = {10, 100, 800, 6000, 50000};
        for (int i = 1; i < radiuses.length; i++)
            if (radiuses[i - 1] <= radius && radius <= radiuses[i])
                return radiuses[i];
        return radiuses[radiuses.length - 1];
    }

    private class ThreadLoad extends Thread {

        private boolean wait;
        private int count = 3;
        private int offset = 0;

        private void waitForLoad() {
            try {
                while (wait)
                    sleep(250);
            } catch (InterruptedException ex) {
                //
            }
        }

        @Override public void run() {
            VKRequest vkRequest;
            String userID = VKAccessToken.currentToken().userId;
            while (count > 0) {
                vkRequest = VKApi.groups().get(
                        VKParameters.from(
                                VKApiConst.USER_ID, userID,
                                VKApiConst.EXTENDED, 1,
                                VKApiConst.FILTERS, "groups,events",
                                VKApiConst.FIELDS, "description,place", // с запросом на место какая - то фигня, он то приходит, то нет
                                VKApiConst.COUNT, 1000,
                                "offset", offset));
                wait = true;
                vkRequest.executeWithListener(vkRequestListenerGetGroups);
                waitForLoad();
                if (count == 0) return;
                offset += 1000;
            }
            handler.post(() -> {
                MainActivity.this.googleMap.setOnCameraMoveListener(onCameraMoveListener);
                MainActivity.this.googleMap.setOnMarkerClickListener(onMarkerClickListener);
            });
        }

        private VKRequest.VKRequestListener vkRequestListenerGetGroups = new VKRequest.VKRequestListener() {
            @Override public void onComplete(VKResponse response) {
                super.onComplete(response);
                VKApiCommunityArray vkApiCommunityFulls = (VKApiCommunityArray) response.parsedModel;

                Object[] mas = vkApiCommunityFulls.toArray();
                count = mas.length;
                VKApiCommunityFull vkApiCommunityFull;
                for (int i = 0; i < mas.length; i++) {
                    vkApiCommunityFull = (VKApiCommunityFull) mas[i];
                    if (vkApiCommunityFull.place != null) {
                        VKApiPlace vkApiPlace = vkApiCommunityFull.place;
                        String name = vkApiCommunityFull.name;
                        String photo = vkApiCommunityFull.photo_100;
                        String text = vkApiCommunityFull.description;
                        MarkerInfo markerInfo = new MarkerInfo(text, photo, photo, MarkerInfo.TYPE_GROUP, vkApiCommunityFull.id, vkApiPlace.latitude, vkApiPlace.longitude);
                        markerInfo.setAddress(vkApiPlace.address);
                        markerInfosGroups.add(markerInfo);
                    }

                }
                wait = false;
            }

            @Override
            public void onError(VKError error) {
                super.onError(error);
                wait = false;
            }
        };
    }
}

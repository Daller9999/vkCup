package com.sunplacestudio.vkgroupscup;

import android.Manifest;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sunplacestudio.vkgroupscup.ViewAdapter.RecyclerAdapter;
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
import com.vk.sdk.api.model.VKApiPost;
import com.vk.sdk.api.model.VKApiUserFull;
import com.vk.sdk.api.model.VKList;
import com.vk.sdk.api.model.VKPostArray;
import com.vk.sdk.api.model.VkAudioArray;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.lang.Thread.sleep;

// String[] fingerprints = VKUtil.getCertificateFingerprint(this, this.getPackageName());

public class MainActivity extends AppCompatActivity {

    private int userId = -1;
    private List<GroupInfo> groups = new ArrayList<>();
    private RecyclerView recyclerView;
    private ConstraintLayout constraintLayoutInfo;
    private RecyclerAdapter recyclerAdapter;
    private int[] leaveIds;
    private static final int waitForSend = 300;
    private int scrollY = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // VKSdk.initialize(this.getApplicationContext());

        String[] permissions = {VKScope.GROUPS};

        if (!VKSdk.isLoggedIn())
            VKSdk.login(this, permissions);
        else
            loadGroups();

        constraintLayoutInfo = findViewById(R.id.constrainLayout);
        constraintLayoutInfo.setVisibility(View.GONE);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        TextView textViewLeaveCount = findViewById(R.id.textViewLeave);
        textViewLeaveCount.setVisibility(View.GONE);
        Button buttonLeave = findViewById(R.id.buttonLeave);
        buttonLeave.setVisibility(View.GONE);
        buttonLeave.setOnClickListener((v) -> {
            if (leaveIds == null) return;
            scrollY -= leaveIds.length / 3 * 500;
            recyclerAdapter.removeIds(leaveIds);
            buttonLeave.setVisibility(View.GONE);
            textViewLeaveCount.setVisibility(View.GONE);
            // if (leaveIds != null && leaveIds.length !=0) // если раскомментировать эти строки, то заработает отписка от групп,
            //    new ThreadLeave(leaveIds).start();        // но я не хочу отписываться, поэтому закомментировал их.
        });

        View textViewGroups = findViewById(R.id.whiteView);
        TextView textViewText = findViewById(R.id.mainText);
        TextView textViewHelp = findViewById(R.id.secondText);

        recyclerAdapter = new RecyclerAdapter(getApplicationContext(), constraintLayoutInfo);
        recyclerView.setAdapter(recyclerAdapter);
        recyclerAdapter.setOnRecyclerClick((ids) -> {
            if (ids.length != 0) {
                buttonLeave.setVisibility(View.VISIBLE);
                textViewLeaveCount.setVisibility(View.VISIBLE);
                textViewLeaveCount.setText(String.valueOf(ids.length));
                leaveIds = ids;
            } else {
                leaveIds = null;
                buttonLeave.setVisibility(View.GONE);
                textViewLeaveCount.setVisibility(View.GONE);
            }
        });
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                scrollY += dy;
                if (scrollY > 1000) {
                    textViewGroups.setVisibility(View.VISIBLE);
                    textViewText.setVisibility(View.GONE);
                    textViewHelp.setVisibility(View.GONE);
                } else if (scrollY < 500) {
                    textViewGroups.setVisibility(View.GONE);
                    textViewText.setVisibility(View.VISIBLE);
                    textViewHelp.setVisibility(View.VISIBLE);
                }
            }
        });
    }


    /**
     * Поток для отписки групп с таймаутом
     */
    private class ThreadLeave extends Thread {
        private boolean leave = false;
        private int[] ids;

        public ThreadLeave(int[] ids) {
            this.ids = ids.clone();
        }

        @Override public void run() {
            try {
                for (int id : ids) {
                    leave = false;
                    sleep(waitForSend);
                    leaveGroup(id);
                    while (!leave)
                        sleep(waitForSend);
                }
            } catch (InterruptedException ex) {
                Log.e("mesUri", "some error in leave run");
            }
        }

        private void leaveGroup(int id) {
            VKApi.groups().leave(id).executeWithListener(new VKRequest.VKRequestListener() {
                @Override public void onError(VKError error) {
                    super.onError(error);
                    leave(id);
                }

                @Override public void onComplete(VKResponse response) {
                    super.onComplete(response);
                    leave = true;
                }
            });
        }

        private void leave(int id) {
            CountDownTimer countDownTimer = new CountDownTimer(300, 100) {
                @Override public void onTick(long l) {}
                @Override public void onFinish() {
                    leaveGroup(id);
                }
            };
            countDownTimer.start();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {

            @Override public void onResult(VKAccessToken res) {
                loadGroups();
            }

            @Override public void onError(VKError error) {
                Toast.makeText(getApplicationContext(), "Ошибка при входе в ВК, попробуйте перезайти в приложение", Toast.LENGTH_SHORT).show();
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void loadGroups() {
        userId = Integer.valueOf(VKAccessToken.currentToken().userId);
        new ThreadLoad().start();
    }

    // грузим по 3 группы в recycler view, именно для этого нужен счётчик
    private int infoCounter = 0;
    private int groupCounter = 0;
    // Массив с основной информацией по всем группам
    private GroupInfo[] groupInfos;
    private Handler handler = new Handler();


    private class ThreadLoad extends Thread {

        private boolean wait;
        private int count = 3;
        private int offset = 0;
        private static final int timeOut = 250;

        private void waitForLoad() {
            try {
                while (wait)
                    sleep(50);
            } catch (InterruptedException ex) {
                //
            }
        }

        @Override public void run() {
            VKRequest vkRequest;
            try {
                while (count > 0) {
                    vkRequest = VKApi.groups().get(
                            VKParameters.from(
                                    VKApiConst.USER_ID, userId,
                                    VKApiConst.EXTENDED, 1,
                                    VKApiConst.FIELDS, "description,members_count",
                                    VKApiConst.COUNT, 3,
                                    "offset", offset));
                    wait = true;
                    vkRequest.executeWithListener(vkRequestListenerGetGroups);
                    waitForLoad();

                    sleep(timeOut);
                    groupInfos = new GroupInfo[3];
                    infoCounter = 0;
                    if (count == 0) return;

                    for (int i = 0; i < 3 && groupCounter < groups.size(); i++) {
                        vkRequest = VKApi.wall().
                                get(VKParameters.from(VKApiConst.OWNER_ID, -groups.get(groupCounter).getId(), VKApiConst.COUNT, 1, VKApiConst.EXTENDED, 1));
                        wait = true;
                        vkRequest.executeWithListener(vkRequestListenerWall);
                        waitForLoad();
                        sleep(timeOut);

                        vkRequest = VKApi.groups().
                                getMembers(VKParameters.from(VKApiConst.GROUP_ID, groups.get(groupCounter).getId(), VKApiConst.OFFSET, 0, "filter", "friends"));
                        wait = true;
                        vkRequest.executeWithListener(vkRequestListenerFriends);
                        waitForLoad();
                        sleep(timeOut);

                        groupInfos[infoCounter] = groups.get(groupCounter);
                        infoCounter++;
                        groupCounter++;
                    }
                    handler.post(() -> recyclerAdapter.addData(groupInfos));
                    offset += 3;
                }
            } catch (InterruptedException ex) {
                //
            }
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
                    groups.add(new GroupInfo(vkApiCommunityFull.id, vkApiCommunityFull.name, vkApiCommunityFull.photo_200, vkApiCommunityFull.description, vkApiCommunityFull.members_count));
                }
                wait = false;
            }

            @Override
            public void onError(VKError error) {
                super.onError(error);
                wait = false;
            }
        };

        private VKRequest.VKRequestListener vkRequestListenerWall = new VKRequest.VKRequestListener() {
            @Override public void onError(VKError error) {
                super.onError(error);
                wait = false;
            }

            @Override public void onComplete(VKResponse response) {
                super.onComplete(response);
                VKPostArray vkApiPosts = (VKPostArray) response.parsedModel;
                Object[] objects = vkApiPosts.toArray();
                if (objects.length != 0) {
                    VKApiPost post = (VKApiPost) objects[0];
                    groups.get(groupCounter).setLastPostDate(new Date(post.date * 1000L));
                }
                wait = false;
            }
        };

        private VKRequest.VKRequestListener vkRequestListenerFriends = new VKRequest.VKRequestListener() {
            @Override public void onError(VKError error) {
                super.onError(error);
                wait = false;
            }

            @Override public void onComplete(VKResponse response) {
                super.onComplete(response);
                try {
                    JSONObject ob = (JSONObject) response.json.get("response");
                    int count = Integer.valueOf(ob.get("count").toString());
                    groups.get(groupCounter).setFriends(count);
                } catch (JSONException ex) {
                    Log.e("mesUri","error to convert json");
                }
                wait = false;
            }
        };
    }
}

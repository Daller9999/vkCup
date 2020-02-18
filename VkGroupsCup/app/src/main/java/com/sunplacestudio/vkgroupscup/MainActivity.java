package com.sunplacestudio.vkgroupscup;

import android.Manifest;
import android.content.Intent;
import android.os.CountDownTimer;
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

import java.util.Date;

import static java.lang.Thread.sleep;

// String[] fingerprints = VKUtil.getCertificateFingerprint(this, this.getPackageName());

public class MainActivity extends AppCompatActivity {

    private int userId = -1;
    private GroupInfo[] groups;
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
            getUserGroups();

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

    private void getUserGroups() {
        VKApi.users().get().executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                VKList<VKApiUserFull> vkApiUserFulls = (VKList<VKApiUserFull>) response.parsedModel;
                VKApiUserFull vkApiUserFull = (VKApiUserFull) vkApiUserFulls.toArray()[0];
                userId = vkApiUserFull.id;
                loadGroups(userId);
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
                getUserGroups();
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

    /**
     * Загрузка групп пользователя
     * @param userId ИД пользователя
     */
    private void loadGroups(int userId) {
        VKApi.groups().
                get(VKParameters.from(VKApiConst.USER_ID, userId, VKApiConst.EXTENDED, 1, VKApiConst.FIELDS, "description,members_count", VKApiConst.COUNT, 1000)).
                executeWithListener(new VKRequest.VKRequestListener() {
                    @Override
                    public void onError(VKError error) {
                        super.onError(error);
                        Log.e("mesUri", "error group");
                    }

                    @Override public void onComplete(VKResponse response) {
                        super.onComplete(response);
                        VKApiCommunityArray vkApiCommunityFulls = (VKApiCommunityArray) response.parsedModel;

                        Object[] mas = vkApiCommunityFulls.toArray();
                        groups = new GroupInfo[mas.length];
                        VKApiCommunityFull vkApiCommunityFull;
                        for (int i = 0; i < mas.length; i++) {
                            vkApiCommunityFull = (VKApiCommunityFull) mas[i];
                            groups[i] = new GroupInfo(vkApiCommunityFull.id, vkApiCommunityFull.name, vkApiCommunityFull.photo_200, vkApiCommunityFull.description, vkApiCommunityFull.members_count);
                        }
                        groupInfos = new GroupInfo[3];
                        loadWallData();
                    }
                });
    }

    // грузим по 3 группы в recycler view, именно для этого нужен счётчик
    private int infoCounter = 0;
    private int groupCounter = 0;
    // Массив с основной информацией по всем группам
    private GroupInfo[] groupInfos;


    // Грузим информацию о дате последненго поста
    private void loadWallData() {
        VKApi.wall().
                get(VKParameters.from(VKApiConst.OWNER_ID, -Math.abs(groups[groupCounter].getId()), VKApiConst.COUNT, 1, VKApiConst.EXTENDED, 1)).
                executeWithListener(new VKRequest.VKRequestListener() {
                    @Override public void onError(VKError error) {
                        super.onError(error);
                        Log.i("mesUri", "error to load : " + groupCounter + " ; " + error.toString());
                        if (groupCounter < groups.length)
                            sendNewDataWall();
                        else {
                            groupCounter = 0;
                            Log.i("mesUri", "load over");
                        }
                    }

                    @Override public void onComplete(VKResponse response) {
                        super.onComplete(response);
                        VKPostArray vkApiPosts = (VKPostArray) response.parsedModel;
                        Object[] objects = vkApiPosts.toArray();
                        if (objects.length != 0) {
                            VKApiPost post = (VKApiPost) objects[0];

                            groups[groupCounter].setLastPostDate(new Date(post.date * 1000));
                            groupInfos[infoCounter] = groups[groupCounter];
                            infoCounter++;
                        }

                        groupCounter++;
                        if (groupCounter <= groups.length) {
                            sendNewDataFriends();
                        }
                    }
                });
    }

    // Тайм аут на отправку
    private void sendNewDataWall() {
        CountDownTimer countDownTimer = new CountDownTimer(waitForSend, 50) {
            @Override public void onTick(long l) {}
            @Override public void onFinish() {
                loadWallData();
            }
        };
        countDownTimer.start();
    }

    private void sendNewDataFriends() {
        CountDownTimer countDownTimer = new CountDownTimer(waitForSend, 50) {
            @Override public void onTick(long l) {}
            @Override public void onFinish() {
                loadFriend();
            }
        };
        countDownTimer.start();
    }

    private void loadFriend() {
        VKApi.groups().
                getMembers(VKParameters.from(VKApiConst.GROUP_ID, groups[groupCounter - 1].getId(), VKApiConst.OFFSET, 1, "filter", "friends")).
                executeWithListener(new VKRequest.VKRequestListener() {
                    @Override
                    public void onError(VKError error) {
                        super.onError(error);
                        if (groupCounter - 1 < groups.length)
                            sendNewDataFriends();
                        else {
                            groupCounter = 0;
                            Log.i("mesUri", "load over");
                        }
                    }

                    @Override
                    public void onComplete(VKResponse response) {
                        super.onComplete(response);
                        try {
                            JSONObject ob = (JSONObject) response.json.get("response");
                            int count = Integer.valueOf(ob.get("count").toString());
                            groups[groupCounter - 1].setFriends(count);
                            if (infoCounter == 3) {
                                infoCounter = 0;
                                recyclerAdapter.addData(groupInfos);
                                groupInfos = new GroupInfo[3];
                            }
                        } catch (JSONException ex) {
                            Log.e("mesUri","error to convert json");
                        }
                        if (groupCounter != groups.length)
                            sendNewDataWall();
                        else {
                            // groupInfos[infoCounter] = groups[groupCounter - 1];
                            recyclerAdapter.addData(groupInfos);
                        }
                    }
                });
    }
}

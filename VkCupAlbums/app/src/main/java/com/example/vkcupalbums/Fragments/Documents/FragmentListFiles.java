package com.example.vkcupalbums.Fragments.Documents;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.vkcupalbums.DataLoader.DataLoader;
import com.example.vkcupalbums.MainActivity;
import com.example.vkcupalbums.Objects.VkDocsData;
import com.example.vkcupalbums.R;
import com.vk.sdk.VKAccessToken;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FragmentListFiles extends Fragment {

    private RecyclerAdapter recyclerAdapter;
    private List<VkDocsData> vkDocsDataList = new ArrayList<>();


    @Override public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_files_list, container, false);

        recyclerAdapter = new RecyclerAdapter(view.getContext());
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        recyclerView.setAdapter(recyclerAdapter);
        recyclerAdapter.setOnRecyclerClick(new RecyclerAdapter.OnRecyclerClick() {
            @Override public void onItemClick(VkDocsData vkDocsData) {
                    FragmentShowData fragmentShowData = new FragmentShowData();
                    fragmentShowData.setData(vkDocsData);
                    getFragmentManager()
                            .beginTransaction()
                            .setCustomAnimations(R.anim.to_left_in, R.anim.to_left_out, R.anim.to_right_in, R.anim.to_right_out)
                            .replace(R.id.container_documents, fragmentShowData)
                            .addToBackStack(FragmentShowData.class.getName()).commit();
            }

            @Override public void onError(String text) {
                Toast.makeText(view.getContext(), text, Toast.LENGTH_SHORT).show();
            }
        });

        if (vkDocsDataList.isEmpty())
            DataLoader.getInstance().loadDocuments(vkDocsDataList1 -> {
                this.vkDocsDataList = vkDocsDataList1;
                recyclerAdapter.addList(vkDocsDataList1);
            });
        else
            recyclerAdapter.setList(vkDocsDataList);

        return view;
    }


    @Override public void onDestroy() {
        super.onDestroy();
        recyclerAdapter.destroy();
    }
}

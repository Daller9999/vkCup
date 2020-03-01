package com.sunplacestudio.vkcupvideoqrcode.Fragment;


import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.sunplacestudio.vkcupvideoqrcode.MainActivity;
import com.sunplacestudio.vkcupvideoqrcode.R;

public class FragmentPermissions extends Fragment {

    @Nullable
    @Override public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_permissions, container, false);
        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean ok = true;
        for (int i = 0; i < permissions.length; i++)
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                ok = false;
                break;
            }
        ok = permissions.length != 0;
        if (ok)
            ((MainActivity) getActivity()).loadCameraFragment();

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}

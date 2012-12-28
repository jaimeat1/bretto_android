package com.provauto.bretto.wcontrol.fragments;

import com.provauto.bretto.wcontrol.R;
import com.provauto.bretto.wcontrol.R.layout;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SystemFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	return inflater.inflate(R.layout.system_layout, container, false);
    }
}

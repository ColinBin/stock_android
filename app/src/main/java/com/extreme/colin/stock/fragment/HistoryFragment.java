package com.extreme.colin.stock.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.extreme.colin.stock.MyOperations;
import com.extreme.colin.stock.R;

/**
 * Created by colin on 12/11/2017.
 */

public class HistoryFragment extends Fragment{
    private static final String TAG = "HistoryFragment";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);



        return view;
    }

}

package com.extreme.colin.stock;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.toolbox.Volley;

/**
 * Created by colin on 12/11/2017.
 */

public class CurrentFragment extends Fragment{

    private Button currentButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_current, container, false);

        currentButton = view.findViewById(R.id.current_button);
        currentButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Hello I am current", Toast.LENGTH_SHORT).show();


            }
        });

        return view;
    }

}

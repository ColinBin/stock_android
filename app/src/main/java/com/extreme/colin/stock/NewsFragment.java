package com.extreme.colin.stock;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

/**
 * Created by colin on 12/11/2017.
 */

public class NewsFragment extends Fragment{

    private static final String TAG = "NewsFragment";

    private Button newsButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news, container, false);

        newsButton = view.findViewById(R.id.news_button);
        newsButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Hello I am news", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

}

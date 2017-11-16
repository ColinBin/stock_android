package com.extreme.colin.stock.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.extreme.colin.stock.MyOperations;
import com.extreme.colin.stock.fragment.CurrentFragment;
import com.extreme.colin.stock.fragment.HistoryFragment;
import com.extreme.colin.stock.fragment.NewsFragment;
import com.extreme.colin.stock.R;
import com.extreme.colin.stock.adaptor.SectionsPagerAdapter;

public class DetailActivity extends AppCompatActivity {

    private static final String TAG = "DetainActivity";

    private ViewPager mViewPager;
    private String symbolInput;

    RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        queue = Volley.newRequestQueue(this);

        Intent intent = getIntent();
        symbolInput = intent.getStringExtra("symbol");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(symbolInput);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // when the back arrow is clicked, return to search activity and terminate
        toolbar.setNavigationOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                terminateThis();
            }
        });

        mViewPager = findViewById(R.id.container);
        setupViewPager(mViewPager);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
    }

    private void terminateThis() {
        Intent intent = new Intent(this, SearchActivity.class);
        startActivity(intent);
        this.finish();
    }

    private void setupViewPager(ViewPager viewPager) {
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new CurrentFragment(), "Current");
        adapter.addFragment(new HistoryFragment(), "Historical");
        adapter.addFragment(new NewsFragment(), "News");
        viewPager.setAdapter(adapter);
    }

    public void addRequest(Request request) {
        queue.add(request);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        terminateThis();
    }
}

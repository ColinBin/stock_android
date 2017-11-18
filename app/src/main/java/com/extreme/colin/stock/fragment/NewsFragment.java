package com.extreme.colin.stock.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.extreme.colin.stock.MyOperations;
import com.extreme.colin.stock.R;
import com.extreme.colin.stock.activity.DetailActivity;
import com.extreme.colin.stock.adaptor.NewsAdaptor;
import com.extreme.colin.stock.models.News;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.extreme.colin.stock.MyOperations.parseNewsList;

/**
 * Created by colin on 12/11/2017.
 */

public class NewsFragment extends Fragment implements AdapterView.OnItemClickListener {

    private static final String TAG = "NewsFragment";

    private String symbolInput;

    private ListView newsListView;
    private ProgressBar newsProgressBar;
    private TextView newsErrorMsg;

    private List<News> newsList = new ArrayList<>();

    private NewsAdaptor newsAdaptor;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news, container, false);
        newsListView = view.findViewById(R.id.news_list);
        newsListView.setOnItemClickListener(this);
        newsErrorMsg = view.findViewById(R.id.news_error_msg);
        newsProgressBar = view.findViewById(R.id.news_progress_bar);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        // only show progress bar
        setUIState(MyOperations.IN_PROGRESS);
        symbolInput = getActivity().getIntent().getStringExtra("symbol");

        // request data from backend
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("symbol", symbolInput);
        params.put("type", "news");
        String url = MyOperations.makeUrl(params);

        JsonObjectRequest newsRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            if(response.getInt("status_code") == 200) {
                                JSONArray newsArray = response.getJSONArray("data");
                                newsList = MyOperations.parseNewsList(newsArray);
                                // adapter needs an activity as an adapter
                                newsAdaptor = new NewsAdaptor(getActivity(), R.layout.news_item, newsList);
                                newsListView.setAdapter(newsAdaptor);
                                setUIState(MyOperations.SUCCESS);
                            } else {
                                setUIState(MyOperations.ERROR);
                            }
                        } catch (Exception exp) {
                            Log.e(TAG, exp.toString());
                            // only show error msg
                            setUIState(MyOperations.ERROR);
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //only show error msg
                        setUIState(MyOperations.ERROR);
                    }

                });
        ((DetailActivity)getActivity()).addRequest(newsRequest);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        News selectedNews = (News)adapterView.getItemAtPosition(i);
        // open browser with the links associated with this news
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(selectedNews.getUrl()));
        startActivity(browserIntent);
    }

    private void setUIState(int state) {
        MyOperations.setUIState(newsListView, newsProgressBar, newsErrorMsg, state);
    }

}

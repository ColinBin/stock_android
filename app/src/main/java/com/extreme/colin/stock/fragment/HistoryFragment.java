package com.extreme.colin.stock.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Button;
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

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.HashMap;

/**
 * Created by colin on 12/11/2017.
 */

public class HistoryFragment extends Fragment{
    private static final String TAG = "HistoryFragment";

    private WebView historyWebView;
    private TextView historyErrorMsg;
    private ProgressBar historyProgressBar;

    private String symbolInput;

    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case 1:
                    setHistoryUIState(MyOperations.SUCCESS);
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        historyWebView = view.findViewById(R.id.history_web_view);
        historyProgressBar = view.findViewById(R.id.history_progress_bar);
        historyErrorMsg = view.findViewById(R.id.history_error_msg);

        historyWebView.getSettings().setJavaScriptEnabled(true);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        symbolInput = getActivity().getIntent().getStringExtra("symbol");

        setHistoryUIState(MyOperations.IN_PROGRESS);

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("symbol", symbolInput);
        params.put("type", "stock_history");
        String url = MyOperations.makeUrl(params);
        JsonObjectRequest historyRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getInt("status_code") == 200 && !response.isNull("data")) {
                                JSONObject historyData = response.getJSONObject("data");

                                historyWebView.addJavascriptInterface(new javascriptObject(historyData), "injectedObject");
                                historyWebView.loadUrl("file:///android_asset/history.html");
                            } else {
                                setHistoryUIState(MyOperations.ERROR);
                            }
                        } catch (Exception exp) {
                            Log.e(TAG, exp.toString());
                            // only show error msg
                            setHistoryUIState(MyOperations.ERROR);
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //only show error msg
                        setHistoryUIState(MyOperations.ERROR);
                    }

                });
        historyRequest.setTag(MyOperations.HISTORY_REQUEST);
        ((DetailActivity) getActivity()).addRequest(historyRequest);
    }

    class javascriptObject {
        JSONObject jsonObject;

        private javascriptObject(JSONObject object) {
            this.jsonObject = object;
        }
        @JavascriptInterface
        public String getStrData() {
            return jsonObject.toString();
        }

        @JavascriptInterface
        public void readyToDisplay() {
            // when the chart is rendered, set UI state
            Message msg = new Message();
            msg.what = 1;
            mHandler.sendMessage(msg);
        }
        @JavascriptInterface
        public void notifyAndroid(String msg) {
            Log.d(TAG, "notifyAndroid: " + msg);
        }

    }

    private void setHistoryUIState(int state) {
        MyOperations.setUIState(historyWebView, historyProgressBar, historyErrorMsg, state);
    }


}

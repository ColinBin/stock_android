package com.extreme.colin.stock.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.extreme.colin.stock.MyOperations;
import com.extreme.colin.stock.R;
import com.extreme.colin.stock.activity.DetailActivity;
import com.extreme.colin.stock.adaptor.NewsAdaptor;
import com.extreme.colin.stock.models.Favorite;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.List;

/**
 * Created by colin on 12/11/2017.
 */

public class CurrentFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "CurrentFragment";

    private ImageButton facebookButton;
    private ImageButton favoriteButton;
    private TableLayout detailTable;
    private TextView changeIndicatorButton;
    private Spinner indicatorSpinner;
    private ProgressBar detailProgressBar;
    private ProgressBar indicatorProgressBar;
    private TextView detailErrorMsg;
    private TextView indicatorErrorMsg;

    private TextView detailSymbol;
    private TextView detailPrice;
    private TextView detailChange;
    private TextView detailTime;
    private TextView detailOpen;
    private TextView detailClose;
    private TextView detailCloseOrPrev;
    private TextView detailRange;
    private TextView detailVolume;
    private ImageView detailArrow;

    private String symbolInput;

    private int detailState;
    private int indicatorState;

    private Favorite currentStock;

    boolean isCurrentFavorite = false;

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    JSONArray favoriteJSONArray;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_current, container, false);

        // components of the detail table
        detailSymbol = view.findViewById(R.id.detail_symbol);
        detailPrice = view.findViewById(R.id.detail_price);
        detailChange = view.findViewById(R.id.detail_change);
        detailTime = view.findViewById(R.id.detail_timestamp);
        detailOpen = view.findViewById(R.id.detail_open);
        detailClose = view.findViewById(R.id.detail_close);
        detailCloseOrPrev = view.findViewById(R.id.detail_close_prev);
        detailRange = view.findViewById(R.id.detail_range);
        detailVolume = view.findViewById(R.id.detail_volume);
        detailArrow = view.findViewById(R.id.detail_arrow);

        facebookButton = view.findViewById(R.id.facebook_button);
        favoriteButton = view.findViewById(R.id.favorite_button);

        changeIndicatorButton = view.findViewById(R.id.change_indicator_button);
        indicatorSpinner = view.findViewById(R.id.indicator_spinner);

        detailProgressBar = view.findViewById(R.id.detail_progress_bar);
        indicatorProgressBar = view.findViewById(R.id.indicator_progress_bar);
        detailErrorMsg = view.findViewById(R.id.detail_error_msg);
        indicatorErrorMsg = view.findViewById(R.id.indicator_error_msg);

        detailTable = view.findViewById(R.id.detail_table);

        facebookButton.setOnClickListener(this);
        favoriteButton.setOnClickListener(this);
        changeIndicatorButton.setOnClickListener(this);


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        symbolInput = getActivity().getIntent().getStringExtra("symbol");

        // read local storage and set favorite state
        sharedPref = getActivity().getSharedPreferences("stock_information", Context.MODE_PRIVATE);


        try{
            favoriteJSONArray = new JSONArray(sharedPref.getString("favorite_list", "[]"));
            for(int i = 0; i < favoriteJSONArray.length(); ++i) {
                if(symbolInput.equals(favoriteJSONArray.getJSONObject(i).getString("symbol"))) {
                    isCurrentFavorite = true;
                }
            }
        } catch(Exception exp) {
            Log.e(TAG, exp.toString());
            favoriteJSONArray = new JSONArray();
            isCurrentFavorite = false;
        }

        // TODO change star pic after getting data from back end
        setFavoriteButtonImage();


        detailState = MyOperations.INPROGRESS;
        indicatorState = MyOperations.INPROGRESS;

        setDetailUIState(MyOperations.INPROGRESS);
        setIndicatorUIState(MyOperations.INPROGRESS);

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("symbol", symbolInput);
        params.put("type", "stock_detail");
        String url = MyOperations.makeUrl(params);

        JsonObjectRequest detailRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getInt("status_code") == 200) {
                                JSONObject detailData = response.getJSONObject("data");
                                displayDetailResult(detailData);
                                setDetailUIState(MyOperations.SUCCESS);
                                detailState = MyOperations.SUCCESS;
                            } else {
                                setDetailUIState(MyOperations.ERROR);
                                detailState = MyOperations.ERROR;
                            }
                        } catch (Exception exp) {
                            Log.e(TAG, exp.toString());
                            // only show error msg
                            setDetailUIState(MyOperations.ERROR);
                            detailState = MyOperations.ERROR;
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //only show error msg
                        setDetailUIState(MyOperations.ERROR);
                        detailState = MyOperations.ERROR;
                    }

                });
        ((DetailActivity) getActivity()).addRequest(detailRequest);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.facebook_button:
                break;
            case R.id.favorite_button:
                // only valid when detail are successfully retrieved
                if (detailState == MyOperations.SUCCESS) {
                    // toggle favorite state
                    toggleFavorite();
                } else if (detailState == MyOperations.ERROR) {
                    MyOperations.makeToast(getActivity(), "Current Stock is not successfully loaded.");
                } else {
                    MyOperations.makeToast(getActivity(), "Current Stock has not successfully loaded.");
                }
                break;
            case R.id.change_indicator_button:
                break;
            default:
                break;
        }
    }

    private void displayDetailResult(JSONObject detailData) {
        try {
            String changeStr = detailData.getString("change");
            String changePercentStr = detailData.getString("change_percent");
            String changeAndPercent =  changeStr + " (" + changePercentStr + ")";
            String symbol = detailData.getString("symbol");
            String lastPriceStr = detailData.getString("last_price");
            String timestamp = detailData.getString("timestamp");
            String openStr = detailData.getString("open");
            String rangeStr = detailData.getString("range");
            String volumeStr = detailData.getString("volume");

            detailSymbol.setText(symbol);
            detailPrice.setText(lastPriceStr);
            detailChange.setText(changeAndPercent);
            detailTime.setText(timestamp);
            detailOpen.setText(openStr);
            detailRange.setText(rangeStr);
            detailVolume.setText(volumeStr);
            if (detailData.getBoolean("isTrading")) {
                String prevCloseLiteral = "Prev Close";
                detailCloseOrPrev.setText(prevCloseLiteral);
                detailClose.setText(detailData.getString("prev_close"));
            } else {
                String closeLiteral = "Close";
                detailCloseOrPrev.setText(closeLiteral);
                detailClose.setText(detailData.getString("close"));
            }


            double changeValue = Double.parseDouble(changeStr);
            if (changeValue > 0) {
                detailArrow.setImageResource(R.drawable.up);
            } else {
                detailArrow.setImageResource(R.drawable.down);
            }
            // parse json data to Favorite object
            currentStock = MyOperations.getFavoriteFromJSONObject(detailData);

        } catch (Exception exception) {
            setDetailUIState(MyOperations.ERROR);
            detailState = MyOperations.ERROR;
        }

    }

    private void toggleFavorite() {
        try {

            if(isCurrentFavorite) {
                // TODO optimize to avoid search every time
                for(int i = 0; i < favoriteJSONArray.length(); ++i) {
                    if(symbolInput.equals(favoriteJSONArray.getJSONObject(i).getString("symbol"))) {
                        favoriteJSONArray.remove(i);
                        break;
                    }
                }

            } else {
                favoriteJSONArray.put(currentStock.toJson());

            }
            // update local storage
            editor = sharedPref.edit();
            editor.putString("favorite_list", favoriteJSONArray.toString());
            editor.apply();
            isCurrentFavorite = !isCurrentFavorite;
            setFavoriteButtonImage();
        }catch (Exception exp) {
            Log.e(TAG, exp.toString());
        }

    }


    private void setDetailUIState(int state) {
        MyOperations.setUIState(detailTable, detailProgressBar, detailErrorMsg, state);
    }

    private void setIndicatorUIState(int state) {

    }

    private void setFavoriteButtonImage() {
        if(isCurrentFavorite) {
            favoriteButton.setImageResource(R.drawable.filled);
        } else {
            favoriteButton.setImageResource(R.drawable.empty);
        }
    }
}

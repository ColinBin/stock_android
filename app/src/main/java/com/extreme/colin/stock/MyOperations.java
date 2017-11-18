package com.extreme.colin.stock;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.extreme.colin.stock.models.Favorite;
import com.extreme.colin.stock.models.Hint;
import com.extreme.colin.stock.models.News;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.android.volley.VolleyLog.TAG;

/**
 * Created by colin on 13/11/2017.
 */

public class MyOperations {

    private static final String baseUrl = "http://stock-homework9.us-east-1.elasticbeanstalk.com/";

    public static final int IN_PROGRESS = -1;

    public static final int ERROR = 0;

    public static final int SUCCESS = 1;

    public static final int INTERVAL = 5000;

    public static final int DETAIL_REQUEST = 111;

    public static final int AUTOCOMPLETE_REQUEST = 222;


    public static void makeToast(Activity activity, String msg) {
        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
    }

    public static String makeUrl(HashMap<String, String> params) {
        StringBuilder formattedUrl = new StringBuilder(baseUrl + "?");
        for (HashMap.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            formattedUrl.append(key + "=" + value + "&");
        }
        formattedUrl.deleteCharAt(formattedUrl.length() - 1);
        return formattedUrl.toString();
    }

    public static double stripFormatDouble(String doubleStr) {
        return Double.parseDouble(doubleStr.replaceAll(",", ""));
    }

    public static void setUIState(View component, ProgressBar progressBar, TextView errorMsg, int state) {
        if (state == -1) {
            progressBar.setVisibility(View.VISIBLE);
            component.setVisibility(View.INVISIBLE);
            errorMsg.setVisibility(View.INVISIBLE);
        } else if (state == 0) {
            errorMsg.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
            component.setVisibility(View.INVISIBLE);
        } else {
            component.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
            errorMsg.setVisibility(View.INVISIBLE);
        }
    }

    public static List<News> parseNewsList(JSONArray newsArray) {
        List<News> newsList = new ArrayList<>();
        try {
            for (int i = 0; i < newsArray.length(); ++i) {
                News current = new News(newsArray.getJSONObject(i));
                newsList.add(current);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newsList;
    }

    public static List<Favorite> parseFavoriteList(JSONArray favoriteArray) {
        List<Favorite> favoriteList = new ArrayList<>();
        try {
            for (int i = 0; i < favoriteArray.length(); ++i) {
                Favorite current = new Favorite(favoriteArray.getJSONObject(i));
                favoriteList.add(current);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return favoriteList;
    }
    public static List<Hint> parseHintList(JSONArray hintArray) {
        List<Hint> hintList = new ArrayList<>();
        try {
            for (int i = 0; i < hintArray.length(); ++i) {
                Hint current = new Hint(hintArray.getJSONObject(i));
                hintList.add(current);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hintList;
    }

    public static Favorite getFavoriteFromJSONObject(JSONObject jsonObject) {
        Favorite favorite;
        try {
            String lastPriceStr = jsonObject.getString("last_price");
            String changeStr = jsonObject.getString("change");
            String changePercentStr = jsonObject.getString("change_percent");
            favorite = new Favorite(
                    jsonObject.getString("symbol"),
                    stripFormatDouble(lastPriceStr),
                    lastPriceStr,
                    Double.parseDouble(changeStr),
                    Double.parseDouble(jsonObject.getString("change_percent_num")),
                    changeStr + " (" + changePercentStr + ")"
            );
            return favorite;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

}

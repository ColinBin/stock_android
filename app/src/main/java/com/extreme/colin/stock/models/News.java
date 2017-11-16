package com.extreme.colin.stock.models;

import android.util.Log;

import org.json.JSONObject;

import static com.android.volley.VolleyLog.TAG;

/**
 * Created by colin on 13/11/2017.
 */

public class News {

    private String title;
    private String author;
    private String date;
    private String url;

    public News(String newsTitle, String newsAuthor, String newsDate, String newsURL) {
        title = newsTitle;
        author = newsAuthor;
        date = newsDate;
        url = newsURL;
    }

    public News(JSONObject jsonObject) {
        try {
            title = jsonObject.getString("title");
            author = jsonObject.getString("author");
            date = jsonObject.getString("date");
            url = jsonObject.getString("link");
        }catch (Exception exception) {
            Log.e(TAG, exception.toString());
        }
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getDate() {
        return date;
    }

    public String getUrl() {
        return url;
    }
}

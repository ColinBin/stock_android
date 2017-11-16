package com.extreme.colin.stock.models;

import android.util.Log;

import org.json.JSONObject;

import java.io.Serializable;

import static com.android.volley.VolleyLog.TAG;

/**
 * Created by colin on 13/11/2017.
 */

public class Favorite implements Serializable{
    private String symbol;
    private double price;
    private String priceStr;
    private double change;
    private double changePercent;
    private String changeAndPercent;



    private long addedTimestamp;

    public Favorite(String symbol, double price, String priceStr, double change, double changePercent, String changeAndPercent) {
        this.symbol = symbol;
        this.price = price;
        this.priceStr = priceStr;
        this.change = change;
        this.changePercent = changePercent;
        this.changeAndPercent = changeAndPercent;
        addedTimestamp = System.currentTimeMillis() / 1000L;
    }

    public Favorite(JSONObject jsonObject) {
        try {
            symbol = jsonObject.getString("symbol");
            price = jsonObject.getDouble("price");
            priceStr = jsonObject.getString("priceStr");
            change = jsonObject.getDouble("change");
            changePercent = jsonObject.getDouble("changePercent");
            changeAndPercent = jsonObject.getString("changeAndPercent");
            addedTimestamp = jsonObject.getLong("addedTimestamp");
        }catch (Exception exception) {
            Log.e(TAG, exception.toString());
        }
    }

    public JSONObject toJson() {
        JSONObject self = new JSONObject();
        try {
            self.put("symbol", symbol);
            self.put("price", price);
            self.put("priceStr", priceStr);
            self.put("change", change);
            self.put("changePercent", changePercent);
            self.put("changeAndPercent", changeAndPercent);
            self.put("addedTimestamp", addedTimestamp);
        } catch (Exception exception) {
            Log.e(TAG, exception.toString());
        }
        return self;
    }

    public String getSymbol() {
        return symbol;
    }

    public double getPrice() {
        return price;
    }

    public String getPriceStr() {
        return priceStr;
    }

    public double getChange() {
        return change;
    }

    public double getChangePercent() {
        return changePercent;
    }

    public String getChangeAndPercent() {
        return changeAndPercent;
    }

    public long getAddedTimestamp() {
        return addedTimestamp;
    }
}

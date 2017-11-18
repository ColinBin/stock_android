package com.extreme.colin.stock.models;

import org.json.JSONObject;

/**
 * Created by colin on 17/11/2017.
 */

public class Hint {
    private String symbol;
    private String fullDescription;

    public Hint(String symbol, String fullDescription) {
        this.symbol = symbol;
        this.fullDescription = fullDescription;
    }

    public Hint(JSONObject jsonObject) {
        try {
            this.symbol = jsonObject.getString("symbol");
            this.fullDescription = jsonObject.getString("full_description");
        }catch (Exception e) {
            e.printStackTrace();
        }
    }


    public String getSymbol() {
        return symbol;
    }

    public String getFullDescription() {
        return fullDescription;
    }
}

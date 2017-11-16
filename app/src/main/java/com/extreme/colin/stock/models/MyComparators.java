package com.extreme.colin.stock.models;

import java.util.Comparator;

/**
 * Created by colin on 15/11/2017.
 */

public class MyComparators  {
    public static Comparator<Favorite> compareSymbol=new Comparator<Favorite>() {
        @Override
        public int compare(Favorite f1, Favorite f2) {
            return f1.getSymbol().compareTo(f2.getSymbol());
        }
    };
    public static Comparator<Favorite> comparePrice=new Comparator<Favorite>() {
        @Override
        public int compare(Favorite f1, Favorite f2) {
            return Double.compare(f1.getPrice(), f2.getPrice());
        }
    };
    public static Comparator<Favorite> compareChange=new Comparator<Favorite>() {
        @Override
        public int compare(Favorite f1, Favorite f2) {
            return Double.compare(f1.getChange(), f2.getChange());
        }
    };
    public static Comparator<Favorite> compareChangePercent=new Comparator<Favorite>() {
        @Override
        public int compare(Favorite f1, Favorite f2) {
            return Double.compare(f1.getChangePercent(), f2.getChangePercent());
        }
    };

}

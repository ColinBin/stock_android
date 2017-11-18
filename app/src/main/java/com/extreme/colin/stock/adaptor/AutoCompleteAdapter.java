package com.extreme.colin.stock.adaptor;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStructure;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.extreme.colin.stock.R;
import com.extreme.colin.stock.models.Favorite;
import com.extreme.colin.stock.models.Hint;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by colin on 13/11/2017.
 */

public class AutoCompleteAdapter extends BaseAdapter implements Filterable{
    private List<Hint> data;

    private Context mContext;

    private int resourceID;

    public AutoCompleteAdapter(@NonNull Context context) {
        mContext = context;
        data = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Nullable
    @Override
    public Hint getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

//    @NonNull
//    @Override
//    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
//        Hint hint = getItem(position);
//        View view = LayoutInflater.from(getContext()).inflate(resourceID, null);
//        ((TextView)view.findViewById(R.id.hint_description)).setText(hint.getFullDescription());
//        return view;
//    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.hint_item, parent, false);
        }
        ((TextView) convertView.findViewById(R.id.hint_description)).setText(getItem(position).getFullDescription());
        return convertView;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        Filter myFilter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if(constraint != null) {
                    // A class that queries a web API, parses the data and returns an ArrayList<Style>

                    try {
                        data.add(new Hint("AAPL", "AAPL DES"));
                        data.add(new Hint("MSFT", "MSFT DES"));
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                    }
                    // Now assign the values and count to the FilterResults object
                    filterResults.values = data;
                    filterResults.count = data.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if(results != null && results.count > 0) {

                    notifyDataSetChanged();
                }
                else {
                    notifyDataSetInvalidated();
                }
            }
        };
        return myFilter;
    }
}

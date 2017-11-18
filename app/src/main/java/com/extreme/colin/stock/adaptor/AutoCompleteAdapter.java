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

public class AutoCompleteAdapter extends ArrayAdapter<Hint>{
    private List<Hint> hints;
    private int resourceID;
    public AutoCompleteAdapter(@NonNull Context context, int resource, @NonNull List<Hint> objects) {
        super(context, resource, objects);
        resourceID = resource;
        hints = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Hint currentHint = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resourceID, null);
        ((TextView)view.findViewById(R.id.hint_description)).setText(currentHint.getFullDescription());
        return view;
    }

    @Override
    public Filter getFilter() {
        Filter myFilter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if(constraint != null) {
                    filterResults.values = hints;
                    filterResults.count = hints.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence contraint, FilterResults results) {
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

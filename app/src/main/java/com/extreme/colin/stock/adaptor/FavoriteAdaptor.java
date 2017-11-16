package com.extreme.colin.stock.adaptor;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.extreme.colin.stock.R;
import com.extreme.colin.stock.models.Favorite;

import org.w3c.dom.Text;

import java.util.List;

/**
 * Created by colin on 13/11/2017.
 */

public class FavoriteAdaptor extends ArrayAdapter<Favorite> {

    private int resourceID;
    public FavoriteAdaptor(@NonNull Context context, int resource, @NonNull List<Favorite> objects) {
        super(context, resource, objects);
        resourceID = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Favorite currentFavorite = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resourceID, null);
        ((TextView)view.findViewById(R.id.favorite_symbol)).setText(currentFavorite.getSymbol());
        ((TextView)view.findViewById(R.id.favorite_price)).setText(currentFavorite.getPriceStr());

        TextView changeView = view.findViewById(R.id.favorite_change);
        changeView.setText(currentFavorite.getChangeAndPercent());
        if(currentFavorite.getChange() >= 0) {
            changeView.setTextColor(Color.GREEN);
        } else {
            changeView.setTextColor(Color.RED);
        }
        return view;
    }
}

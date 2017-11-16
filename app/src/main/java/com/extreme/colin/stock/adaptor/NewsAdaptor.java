package com.extreme.colin.stock.adaptor;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.extreme.colin.stock.R;
import com.extreme.colin.stock.models.News;

import java.util.List;

/**
 * Created by colin on 13/11/2017.
 */

public class NewsAdaptor extends ArrayAdapter<News> {

    private int resourceID;

    public NewsAdaptor(@NonNull Context context, int resource, @NonNull List<News> objects) {
        super(context, resource, objects);
        resourceID = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        News currentNews = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resourceID, null);

        ((TextView)view.findViewById(R.id.news_title)).setText(currentNews.getTitle());
        ((TextView)view.findViewById(R.id.news_author)).setText("Author: " + currentNews.getAuthor());
        ((TextView)view.findViewById(R.id.news_date)).setText("Date: " + currentNews.getDate());

        return view;
    }
}

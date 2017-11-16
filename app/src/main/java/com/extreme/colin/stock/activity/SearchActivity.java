package com.extreme.colin.stock.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.FaceDetector;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.extreme.colin.stock.MyOperations;
import com.extreme.colin.stock.R;
import com.extreme.colin.stock.adaptor.FavoriteAdaptor;
import com.extreme.colin.stock.models.Favorite;
import com.extreme.colin.stock.models.MyComparators;
import com.extreme.colin.stock.models.News;

import org.json.JSONArray;
import org.json.JSONException;
import org.w3c.dom.Text;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SearchActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener {

    private static final String TAG = "SearchActivity";

    private AutoCompleteTextView inputView;

    private TextView searchButton;
    private TextView clearButton;
    private ImageButton refreshButton;
    private Switch autoRefreshSwitch;
    private ListView favoriteListView;
    private Spinner sortBySpinner;
    private int sortByCurrentSelectedPosition = 1;
    private Spinner orderSpinner;
    private int orderCurrentSelectedPosition = 1;
    private ProgressBar searchProgressBar;

    private String symbolInput;

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    JSONArray favoriteJSONArray;

    List<Favorite> favoriteList;
    FavoriteAdaptor favoriteAdaptor;

    RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        queue = Volley.newRequestQueue(this);

        inputView = findViewById(R.id.search_text);
        searchButton = findViewById(R.id.search_button);
        clearButton = findViewById(R.id.clear_button);
        refreshButton = findViewById(R.id.refresh_button);
        autoRefreshSwitch = findViewById(R.id.auto_refresh_switch);
        favoriteListView = findViewById(R.id.favorite_list);
        sortBySpinner = findViewById(R.id.sort_by_spinner);
        orderSpinner = findViewById(R.id.order_spinner);
        searchProgressBar = findViewById(R.id.search_progress_bar);

        searchButton.setOnClickListener(this);
        clearButton.setOnClickListener(this);
        refreshButton.setOnClickListener(this);

        favoriteListView.setOnItemClickListener(this);
        registerForContextMenu(favoriteListView);

        autoRefreshSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                // when checked, fire auto refresh
                // when unchecked, cancel requests
            }
        });

        // logic for two spinners
        String[] sortByStrList = new String[]{
                "Sort by",
                "Default",
                "Symbol",
                "Price",
                "Change",
                "Change (%)"
        };

        final List<String> sortByList = new ArrayList<>(Arrays.asList(sortByStrList));
        ArrayAdapter<String> sortByAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, sortByList) {
            @Override
            public boolean isEnabled(int position){
                return !(position == 0 || position == sortByCurrentSelectedPosition);
            }
            @Override
            public View getDropDownView(int position, View convertView,
                                        ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if(position == 0 || position == sortByCurrentSelectedPosition) {
                    // Set the disable item text color
                    tv.setTextColor(Color.GRAY);
                } else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
//        ArrayAdapter<CharSequence> sortByAdapter = ArrayAdapter.createFromResource(this, R.array.sort_by_array, android.R.layout.simple_spinner_item);
        sortByAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortBySpinner.setAdapter(sortByAdapter);
        sortBySpinner.setOnItemSelectedListener(this);

        String[] orderStrList = new String[]{
                "Order",
                "Ascending",
                "Descending",
        };
        final List<String> orderList = new ArrayList<>(Arrays.asList(orderStrList));
        ArrayAdapter<String> orderAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, orderList) {
            @Override
            public boolean isEnabled(int position){
                return !(position == 0 || position == orderCurrentSelectedPosition);
            }
            @Override
            public View getDropDownView(int position, View convertView,
                                        ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if(position == 0 || position == orderCurrentSelectedPosition) {
                    // Set the disable item text color
                    tv.setTextColor(Color.GRAY);
                } else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
        orderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        orderSpinner.setAdapter(orderAdapter);
        orderSpinner.setOnItemSelectedListener(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        sharedPref = getSharedPreferences("stock_information", Context.MODE_PRIVATE);
        // load local favorite list and render the favorite list view
        renderFavoriteListView();
        searchProgressBar.setVisibility(View.INVISIBLE);

    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.search_button:
                symbolInput = inputView.getText().toString().trim();
                // check whether input is valid
                if(TextUtils.isEmpty(symbolInput)) {
                    MyOperations.makeToast(this, "Please enter a stock name or symbol");
                    return;
                }
                StartSearchEndSelf(symbolInput);
                break;
            case R.id.clear_button:
                // reset input to empty
                inputView.setText("");
                break;
            case R.id.refresh_button:
                // call back end for favorite symbols data

                break;
            default:
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Favorite selectedFavorite = (Favorite) adapterView.getItemAtPosition(i);
        // open browser with the links associated with this news
        StartSearchEndSelf(selectedFavorite.getSymbol());
    }

    private void StartSearchEndSelf(String symbol) {
        // switch to detail activity and terminate
        // pass symbolInput to detail activity
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("symbol", symbol.toUpperCase());
        startActivity(intent);
        // finish current activity
        this.finish();
    }

    private void renderFavoriteListView() {
        try {
            favoriteJSONArray = new JSONArray(sharedPref.getString("favorite_list", "[]"));
            favoriteList = MyOperations.parseFavoriteList(favoriteJSONArray);
            Collections.sort(favoriteList, MyComparators.compareAddedTimestamp);
            favoriteAdaptor = new FavoriteAdaptor(this, R.layout.favorite_item, favoriteList);
            favoriteListView.setAdapter(favoriteAdaptor);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId()==R.id.favorite_list) {
            menu.setHeaderTitle("Remove from Favorites?");
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.long_press_menu, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.option_no:
                return false;
            case R.id.option_yes:
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                int selectedPosition = info.position;
                // remove from local storage
                // update list view UI
                favoriteList.remove(selectedPosition);
                updateArrayAndWriteToLocal();
                favoriteAdaptor.notifyDataSetChanged();
                return true;
            default:
                return false;
        }
    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

        if(adapterView.getId() == R.id.sort_by_spinner) {
            sortByCurrentSelectedPosition = i;
        } else if(adapterView.getId() == R.id.order_spinner) {
            orderCurrentSelectedPosition = i;
        }
        switch(sortByCurrentSelectedPosition) {
            case 1:
                //default
                Collections.sort(favoriteList, MyComparators.compareAddedTimestamp);
                break;
            case 2:
                // symbol
                Collections.sort(favoriteList, MyComparators.compareSymbol);
                break;
            case 3:
                // price
                Collections.sort(favoriteList, MyComparators.comparePrice);
                break;
            case 4:
                // change
                Collections.sort(favoriteList, MyComparators.compareChange);
                break;
            case 5:
                // change percent
                Collections.sort(favoriteList, MyComparators.compareChangePercent);
                break;
            default:
                break;

        }
        // if not default and descending is selected
        if(orderCurrentSelectedPosition == 2 && sortByCurrentSelectedPosition != 1 && sortByCurrentSelectedPosition != 0) {
            Collections.reverse(favoriteList);
        }
        favoriteAdaptor.notifyDataSetChanged();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }


    private void updateArrayAndWriteToLocal() {
        // construct a new array based on list
        favoriteJSONArray = new JSONArray();
        for(int i = 0; i < favoriteList.size(); ++i) {
            favoriteJSONArray.put(favoriteList.get(i).toJson());
        }
        // write to local storage
        editor = sharedPref.edit();
        editor.putString("favorite_list", favoriteJSONArray.toString());
        editor.apply();
    }

    private void updateFavorite(Favorite favorite) {
        // find the symbol being updated and update the list
        String symbol = favorite.getSymbol();
        for(int i = 0; i < favoriteList.size(); ++i) {
            if(favoriteList.get(i).getSymbol().equals(symbol)) {
                favoriteList.set(i, favorite);
                break;
            }
        }
        // update UI
        favoriteAdaptor.notifyDataSetChanged();
        // update local storage
        updateArrayAndWriteToLocal();
    }

}

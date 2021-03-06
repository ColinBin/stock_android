package com.extreme.colin.stock.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.extreme.colin.stock.MyOperations;
import com.extreme.colin.stock.R;
import com.extreme.colin.stock.adaptor.AutoCompleteAdapter;
import com.extreme.colin.stock.adaptor.FavoriteAdaptor;
import com.extreme.colin.stock.models.Favorite;
import com.extreme.colin.stock.models.Hint;
import com.extreme.colin.stock.models.MyComparators;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class SearchActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener, TextWatcher {

    private static final String TAG = "SearchActivity";
    // UI component
    private AutoCompleteTextView autoCompleteInputView;
    private TextView searchButton;
    private TextView clearButton;
    private ImageButton refreshButton;
    private Switch autoRefreshSwitch;
    private ListView favoriteListView;
    private Spinner sortBySpinner;
    private Spinner orderSpinner;
    private ProgressBar searchProgressBar;

    private int sortByCurrentSelectedPosition = 1;
    private int orderCurrentSelectedPosition = 1;

    private String symbolInput;

    // for local storage
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    JSONArray favoriteJSONArray;
    List<Favorite> favoriteList;
    FavoriteAdaptor favoriteAdaptor;

    // Volley request queue
    RequestQueue queue;

    // handler for repeated refresh
    private Handler mHandler;

    // the count of request for detail data, when each refresh fires, it gets incremented
    // when back end responds, it gets decremented, when 0, not show progress bar and update list
    private int dueCount = 0;

    // Autocomplete
    private AutoCompleteAdapter autoCompleteAdapter;
    List<Hint> hints;
    private boolean isJustSelected = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        queue = Volley.newRequestQueue(this);

        autoCompleteInputView = findViewById(R.id.search_text);
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

        // configure autocomplete
        hints = new ArrayList<>();
        autoCompleteAdapter = new AutoCompleteAdapter(this, R.layout.hint_item, hints);
        autoCompleteInputView.setAdapter(autoCompleteAdapter);
        autoCompleteInputView.setThreshold(1);
        autoCompleteInputView.addTextChangedListener(this);
        autoCompleteInputView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                isJustSelected = true;

                Hint curr = (Hint) adapterView.getItemAtPosition(i);
                autoCompleteInputView.setText(curr.getFullDescription());
                autoCompleteInputView.setSelection(autoCompleteInputView.getText().length());
            }
        });

        autoRefreshSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                // when checked, fire auto refresh
                if(b) {
                    if(favoriteList.size() == 0)
                        return;
                    mHandler.postDelayed(mFavoriteRefresher, MyOperations.INTERVAL);
                } else {
                    queue.cancelAll(MyOperations.DETAIL_REQUEST);
                    dueCount = 0;
                    searchProgressBar.setVisibility(View.INVISIBLE);
                    stopRefreshing();
                }
            }
        });

        // configure spinners
        configureSpinners();
        sortBySpinner.setOnItemSelectedListener(this);
        orderSpinner.setOnItemSelectedListener(this);
    }

    private void configureSpinners() {
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
    }


    Runnable mFavoriteRefresher = new Runnable() {
        @Override
        public void run() {
            try {
                queue.cancelAll(MyOperations.DETAIL_REQUEST);
                dueCount = 0;
                searchProgressBar.setVisibility(View.VISIBLE);
                refreshFavorite();
            } finally {

                mHandler.postDelayed(mFavoriteRefresher, MyOperations.INTERVAL);
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        sharedPref = getSharedPreferences("stock_information", Context.MODE_PRIVATE);
        mHandler = new Handler();
        // load local favorite list and render the favorite list view
        renderFavoriteListView();
        searchProgressBar.setVisibility(View.INVISIBLE);

    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.search_button:
                symbolInput = autoCompleteInputView.getText().toString().trim();
                // check whether input is valid
                if(TextUtils.isEmpty(symbolInput)) {
                    MyOperations.makeToast(this, "Please enter a stock name or symbol");
                    return;
                }
                if(symbolInput.contains(" ")) {
                    symbolInput = symbolInput.substring(0, symbolInput.indexOf(" "));
                }
                StartSearchEndSelf(symbolInput);
                break;
            case R.id.clear_button:
                // reset input to empty
                autoCompleteInputView.setText("");
                break;
            case R.id.refresh_button:
                if(favoriteList.size() == 0)
                    return;
                // call back end for favorite symbols data
                searchProgressBar.setVisibility(View.VISIBLE);
                refreshFavorite();
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
            // menu.setHeaderTitle("Remove from Favorites?");
            LayoutInflater headerInflater = (LayoutInflater) getApplicationContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            ViewGroup header = (ViewGroup) headerInflater.inflate(
                    R.layout.menu_header, null);
            menu.setHeaderView(header);

            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.long_press_menu, menu);

        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.option_no:
                MyOperations.makeToast(this, "Selected no.");
                return false;
            case R.id.option_yes:
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                int selectedPosition = info.position;
                // remove from local storage
                // update list view UI
                favoriteList.remove(selectedPosition);
                updateArrayAndWriteToLocal();
                favoriteAdaptor.notifyDataSetChanged();
                MyOperations.makeToast(this, "Selected yes.");
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

    private void refreshFavorite() {

        int favoriteCount = favoriteList.size();
        dueCount += favoriteCount;
        for(int i = 0; i < favoriteCount; ++i) {
            String symbol = favoriteList.get(i).getSymbol();
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("symbol", symbol);
            params.put("type", "stock_detail");
            String url = MyOperations.makeUrl(params);

            JsonObjectRequest detailRequest = new JsonObjectRequest
                    (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                // TODO make sure dueCount is accessed by one request once
                                --dueCount;
                                if (response.getInt("status_code") == 200) {

                                    if(!response.isNull("data")) {
                                        JSONObject detailData = response.getJSONObject("data");
                                        updateFavoriteInList(MyOperations.getFavoriteFromJSONObject(detailData));
                                    }

                                }
                                // if failed to fetch data, do nothing
                            } catch (Exception e) {
                                e.printStackTrace();

                            } finally {
                                // if no pending requests, hide progress bar and update list
                                // and write to local
                                if(dueCount == 0) {
                                    // hide progress bar
                                    searchProgressBar.setVisibility(View.INVISIBLE);
                                    favoriteAdaptor.notifyDataSetChanged();
                                    // update local storage
                                    updateArrayAndWriteToLocal();
                                }
                            }

                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            --dueCount;
                            if(dueCount == 0) {
                                // hide progress bar
                                searchProgressBar.setVisibility(View.INVISIBLE);
                                favoriteAdaptor.notifyDataSetChanged();
                                // update local storage
                                updateArrayAndWriteToLocal();
                            }
                            Log.d(TAG, error.toString());
                        }

                    });
            detailRequest.setTag(MyOperations.DETAIL_REQUEST);
            queue.add(detailRequest);
        }

    }

    private void stopRefreshing() {
        mHandler.removeCallbacks(mFavoriteRefresher);
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

    private void updateFavoriteInList(Favorite favorite) {
        // find the symbol being updated and update the list
        String symbol = favorite.getSymbol();
        for(int i = 0; i < favoriteList.size(); ++i) {
            if(favoriteList.get(i).getSymbol().equals(symbol)) {
                favoriteList.set(i, favorite);
                break;
            }
        }
    }


    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        String currentInput = editable.toString().trim();
        if(!currentInput.equals("") && !currentInput.contains("Hint@")) {
            if(isJustSelected) {
                isJustSelected = false;
                return;
            }

            searchProgressBar.setVisibility(View.VISIBLE);
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("symbol", currentInput);
            params.put("type", "autocomplete");
            String url = MyOperations.makeUrl(params);
            JsonObjectRequest autoCompleteRequest = new JsonObjectRequest
                    (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                if(!response.isNull("status_code") && response.getInt("status_code") == 200 && !response.isNull("data")) {
                                    JSONArray autoCompleteData = response.getJSONArray("data");
                                    hints.clear();
                                    hints.addAll(MyOperations.parseHintList(autoCompleteData));
                                    autoCompleteAdapter.notifyDataSetChanged();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();

                            } finally {
                                 // TODO check whether the queue is empty before hide the progress bar
                                searchProgressBar.setVisibility(View.INVISIBLE);
                            }
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            searchProgressBar.setVisibility(View.INVISIBLE);
                            Log.e(TAG, "onErrorResponse: " + error.toString());
                        }

                    });
            autoCompleteRequest.setTag(MyOperations.AUTOCOMPLETE_REQUEST);
            queue.add(autoCompleteRequest);
        }
    }
}

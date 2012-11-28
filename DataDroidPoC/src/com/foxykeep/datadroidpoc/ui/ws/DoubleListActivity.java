/**
 * 2012 Foxykeep (http://datadroid.foxykeep.com)
 * <p>
 * Licensed under the Beerware License : <br />
 * As long as you retain this notice you can do whatever you want with this stuff. If we meet some
 * day, and you think this stuff is worth it, you can buy me a beer in return
 */

package com.foxykeep.datadroidpoc.ui.ws;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.foxykeep.datadroid.requestmanager.Request;
import com.foxykeep.datadroid.requestmanager.RequestManager.RequestListener;
import com.foxykeep.datadroidpoc.R;
import com.foxykeep.datadroidpoc.data.model.City;
import com.foxykeep.datadroidpoc.data.requestmanager.PoCRequestFactory;
import com.foxykeep.datadroidpoc.dialogs.ConnectionErrorDialogFragment;
import com.foxykeep.datadroidpoc.dialogs.ConnectionErrorDialogFragment.ConnectionErrorDialogListener;
import com.foxykeep.datadroidpoc.dialogs.ProgressDialogFragment;
import com.foxykeep.datadroidpoc.dialogs.ProgressDialogFragment.ProgressDialogFragmentBuilder;
import com.foxykeep.datadroidpoc.ui.DataDroidActivity;

import java.util.ArrayList;

public final class DoubleListActivity extends DataDroidActivity implements RequestListener,
        OnClickListener, ConnectionErrorDialogListener {

    private static final String SAVED_STATE_CITY_LIST_LEFT = "savedStateCityListLeft";
    private static final String SAVED_STATE_CITY_LIST_RIGHT = "savedStateCityListRight";

    private ListView mListViewLeft;
    private CityListAdapter mListAdapterLeft;
    private ListView mListViewRight;
    private CityListAdapter mListAdapterRight;

    private LayoutInflater mInflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.double_list);

        bindViews();

        mInflater = getLayoutInflater();
    }

    @Override
    protected void onResume() {
        super.onResume();
        for (int i = 0, length = mRequestList.size(); i < length; i++) {
            Request request = mRequestList.get(i);
            int requestType = request.getRequestType();

            if (mRequestManager.isRequestInProgress(request)) {
                if (requestType == PoCRequestFactory.REQUEST_TYPE_CITY_LIST) {
                    setProgressBarIndeterminateVisibility(true);
                }
                mRequestManager.addRequestListener(this, request);
            } else {
                if (requestType == PoCRequestFactory.REQUEST_TYPE_CITY_LIST_2) {
                    ProgressDialogFragment.dismiss(this);
                }
                mRequestManager.callListenerWithCachedData(this, request);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!mRequestList.isEmpty()) {
            mRequestManager.removeRequestListener(this);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        ArrayList<City> cityListLeft = new ArrayList<City>();
        for (int i = 0, n = mListAdapterLeft.getCount(); i < n; i++) {
            cityListLeft.add(mListAdapterLeft.getItem(i));
        }

        outState.putParcelableArrayList(SAVED_STATE_CITY_LIST_LEFT, cityListLeft);

        ArrayList<City> cityListRight = new ArrayList<City>();
        for (int i = 0, n = mListAdapterRight.getCount(); i < n; i++) {
            cityListRight.add(mListAdapterRight.getItem(i));
        }

        outState.putParcelableArrayList(SAVED_STATE_CITY_LIST_RIGHT, cityListRight);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mListAdapterLeft.setNotifyOnChange(false);
        mListAdapterRight.setNotifyOnChange(false);

        ArrayList<City> cityItemListLeft = savedInstanceState
                .getParcelableArrayList(SAVED_STATE_CITY_LIST_LEFT);
        ArrayList<City> cityItemListRight = savedInstanceState
                .getParcelableArrayList(SAVED_STATE_CITY_LIST_RIGHT);

        for (int i = 0, length = cityItemListLeft.size(); i < length; i++) {
            mListAdapterLeft.add(cityItemListLeft.get(i));
        }
        for (int i = 0, length = cityItemListRight.size(); i < length; i++) {
            mListAdapterRight.add(cityItemListRight.get(i));
        }

        mListAdapterLeft.notifyDataSetChanged();
        mListAdapterRight.notifyDataSetChanged();
    }

    private void bindViews() {
        ((Button) findViewById(R.id.b_load)).setOnClickListener(this);
        ((Button) findViewById(R.id.b_clear_memory)).setOnClickListener(this);

        mListViewLeft = (ListView) findViewById(R.id.lv_left);
        mListAdapterLeft = new CityListAdapter(this);
        mListViewLeft.setAdapter(mListAdapterLeft);
        mListViewLeft.setEmptyView(findViewById(R.id.tv_empty_left));

        mListViewRight = (ListView) findViewById(R.id.lv_right);
        mListAdapterRight = new CityListAdapter(this);
        mListViewRight.setAdapter(mListAdapterRight);
        mListViewRight.setEmptyView(findViewById(R.id.tv_empty_right));
    }

    private void callCityListWS() {
        mListAdapterLeft.clear();
        setProgressBarIndeterminateVisibility(true);
        Request request = PoCRequestFactory.createGetCityListRequest();
        mRequestManager.execute(request, this);
        mRequestList.add(request);
    }

    private void callCityList2WS() {
        mListAdapterRight.clear();
        new ProgressDialogFragmentBuilder(this)
                .setMessage(R.string.progress_dialog_message)
                .setCancelable(true)
                .show();
        Request request = PoCRequestFactory.createGetCityList2Request();
        mRequestManager.execute(request, this);
        mRequestList.add(request);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.b_load:
                callCityListWS();
                callCityList2WS();
                break;
            case R.id.b_clear_memory:
                mListAdapterLeft.clear();
                mListAdapterRight.clear();
                break;
        }
    }

    @Override
    public void onRequestFinished(Request request, Bundle resultData) {
        if (mRequestList.contains(request)) {
            int requestType = request.getRequestType();

            switch (requestType) {
                case PoCRequestFactory.REQUEST_TYPE_CITY_LIST:
                    setProgressBarIndeterminateVisibility(false);
                    break;
                case PoCRequestFactory.REQUEST_TYPE_CITY_LIST_2:
                    ProgressDialogFragment.dismiss(this);
                    break;
            }
            mRequestList.remove(request);

            ArrayList<City> cityList = resultData
                    .getParcelableArrayList(PoCRequestFactory.BUNDLE_EXTRA_CITY_LIST);

            switch (requestType) {
                case PoCRequestFactory.REQUEST_TYPE_CITY_LIST:
                    mListAdapterLeft.setNotifyOnChange(false);
                    for (int i = 0, length = cityList.size(); i < length; i++) {
                        mListAdapterLeft.add(cityList.get(i));
                    }
                    mListAdapterLeft.notifyDataSetChanged();
                    break;
                case PoCRequestFactory.REQUEST_TYPE_CITY_LIST_2:
                    mListAdapterLeft.setNotifyOnChange(false);
                    for (int i = 0, length = cityList.size(); i < length; i++) {
                        mListAdapterLeft.add(cityList.get(i));
                    }
                    mListAdapterLeft.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onRequestConnectionError(Request request) {
        if (mRequestList.contains(request)) {
            switch (request.getRequestType()) {
                case PoCRequestFactory.REQUEST_TYPE_CITY_LIST:
                    setProgressBarIndeterminateVisibility(false);
                    break;
                case PoCRequestFactory.REQUEST_TYPE_CITY_LIST_2:
                    ProgressDialogFragment.dismiss(this);
                    break;
            }
            mRequestList.remove(request);

            ConnectionErrorDialogFragment.show(this, request, this);
        }
    }

    @Override
    public void onRequestDataError(Request request) {
        if (mRequestList.contains(request)) {
            switch (request.getRequestType()) {
                case PoCRequestFactory.REQUEST_TYPE_CITY_LIST:
                    setProgressBarIndeterminateVisibility(false);
                    break;
                case PoCRequestFactory.REQUEST_TYPE_CITY_LIST_2:
                    ProgressDialogFragment.dismiss(this);
                    break;
            }
            mRequestList.remove(request);

            showBadDataErrorDialog();
        }
    }

    @Override
    public void connectionErrorDialogCancel(Request request) {
    }

    @Override
    public void connectionErrorDialogRetry(Request request) {
        switch (request.getRequestType()) {
            case PoCRequestFactory.REQUEST_TYPE_CITY_LIST:
                callCityListWS();
                break;
            case PoCRequestFactory.REQUEST_TYPE_CITY_LIST_2:
                callCityList2WS();
                break;
        }
    }

    class ViewHolder {
        private TextView mTextViewName;
        private TextView mTextViewPostalCode;
        private TextView mTextViewState;
        private TextView mTextViewCountry;

        public ViewHolder(View view) {
            mTextViewName = (TextView) view.findViewById(R.id.tv_name);
            mTextViewPostalCode = (TextView) view.findViewById(R.id.tv_postal_code);
            mTextViewState = (TextView) view.findViewById(R.id.tv_state);
            mTextViewCountry = (TextView) view.findViewById(R.id.tv_country);
        }

        public void populateViews(City city) {
            mTextViewName.setText(city.name);
            mTextViewPostalCode.setText(city.postalCode);
            mTextViewState.setText(city.state);
            mTextViewCountry.setText(city.country);
        }
    }

    class CityListAdapter extends ArrayAdapter<City> {

        public CityListAdapter(Context context) {
            super(context, 0);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.city_list_item, null);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.populateViews(getItem(position));

            return convertView;
        }
    }
}
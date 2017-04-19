package com.akyuu.simpleweather;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.akyuu.simpleweather.util.HttpUtil;
import com.akyuu.simpleweather.util.ToastUtil;
import com.akyuu.simpleweather.util.Utility;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends BackKeyFragment {

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    ProgressDialog mProgressDialog;

    @BindView(R.id.title_text) TextView mTitleTextView;
    @BindView(R.id.back_button) Button mBackButton;
    @BindView(R.id.list_view) ListView mListView;
    @BindView(R.id.search_view) SearchView mSearchView;

    private ArrayAdapter<String> mAdapter;
    private List<String> mDataList = new ArrayList<>();

    private List<Province> mProvinceList;
    private List<City> mCityList;
    private List<County> mCountyList;

    private Province mSelectedProvince;
    private City mSelectedCity;

    private int mCurrentLevel;

    public ChooseAreaFragment() {}

    public static ChooseAreaFragment newInstance() {
        return new ChooseAreaFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        ButterKnife.bind(this, view);

        mAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_list_item_1, mDataList);
        mListView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mListView.setOnItemClickListener((adapterView, view, position, id) -> {
            if (mCurrentLevel == LEVEL_PROVINCE) {
                mSelectedProvince = mProvinceList.stream()
                        .filter(province -> province.name.equals(mDataList.get(position)))
                        .findFirst().get();
                queryCities();
            } else if (mCurrentLevel == LEVEL_CITY) {
                mSelectedCity = mCityList.stream()
                        .filter(city -> city.name.equals(mDataList.get(position)))
                        .findFirst().get();
                queryCounties();
            } else if (mCurrentLevel == LEVEL_COUNTY) {
                String weatherId = mCountyList.stream()
                        .filter(county -> county.name.equals(mDataList.get(position)))
                        .findFirst().get().weatherId;
                if (getActivity() instanceof MainActivity) {
                    Intent intent = WeatherActivity.newIntent(getContext(), weatherId);
                    startActivity(intent);
                    getActivity().finish();
                } else if (getActivity() instanceof WeatherActivity) {
                    WeatherActivity activity = ((WeatherActivity) getActivity());
                    activity.mDrawerLayout.closeDrawers();
                    activity.mSwipeRefreshLayout.setRefreshing(true);
                    activity.requestWeather(weatherId);
                }

            }
        });

        mBackButton.setOnClickListener(view -> onBackPressed());

        mSearchView.setSubmitButtonEnabled(false);
        mSearchView.onActionViewExpanded();
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(final String newText) {
                if (newText.equals("")) {
                    return true;
                }
                switch (mCurrentLevel) {
                    case LEVEL_PROVINCE:
                        mDataList.clear();
                        mProvinceList.stream()
                                .filter(province -> province.name.contains(newText))
                                .forEach(province -> mDataList.add(province.name));
                        mAdapter.notifyDataSetChanged();
                        break;
                    case LEVEL_CITY:
                        mDataList.clear();
                        mCityList.stream()
                                .filter(city -> city.name.contains(newText))
                                .forEach(city -> mDataList.add(city.name));
                        mAdapter.notifyDataSetChanged();
                        break;
                    case LEVEL_COUNTY:
                        mDataList.clear();
                        mCountyList.stream()
                                .filter(county -> county.name.contains(newText))
                                .forEach(county -> mDataList.add(county.name));
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

        queryProvinces();
        mSearchView.clearFocus();
    }

    private void queryProvinces() {
        mTitleTextView.setText("中国");
        mBackButton.setVisibility(View.GONE);
        mProvinceList = SQLite.select().from(Province.class).queryList();
        if (mProvinceList.size() > 0) {
            mDataList.clear();
            for (Province province : mProvinceList) {
                mDataList.add(province.name);
            }
            mSearchView.setQuery("", false);
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            mCurrentLevel = LEVEL_PROVINCE;
        } else {
            String address = "http://guolin.tech/api/china";
            queryFromServer(address, "province");
        }
    }

    private void queryCities() {
        mTitleTextView.setText(mSelectedProvince.name);
        mBackButton.setVisibility(View.VISIBLE);
        mCityList = SQLite.select().from(City.class)
                .where(City_Table.provinceId.eq(mSelectedProvince.code))
                .queryList();
        if (mCityList.size() > 0) {
            mDataList.clear();
            for (City city : mCityList) {
                mDataList.add(city.name);
            }
            mSearchView.setQuery("", false);
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            mCurrentLevel = LEVEL_CITY;
        } else {
            String address = "http://guolin.tech/api/china/"
                    + Long.toString(mSelectedProvince.code);
            queryFromServer(address, "city");
        }
    }

    private void queryCounties() {
        mTitleTextView.setText(mSelectedCity.name);
        mBackButton.setVisibility(View.VISIBLE);
        mCountyList = SQLite.select().from(County.class)
                .where(County_Table.cityId.eq(mSelectedCity.code))
                .queryList();
        if (mCountyList.size() > 0) {
            mDataList.clear();
            for (County county : mCountyList) {
                mDataList.add(county.name);
            }
            mSearchView.setQuery("", false);
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            mCurrentLevel = LEVEL_COUNTY;
        } else {
            String address = "http://guolin.tech/api/china/"
                    + Long.toString(mSelectedProvince.code) + "/"
                    + Long.toString(mSelectedCity.code);
            queryFromServer(address, "county");
        }
    }

    protected void queryFromServer(String address, final String type) {
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        ToastUtil.show("加载失败", Toast.LENGTH_SHORT);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                if ("province".equals(type)) {
                    result = Utility.handleProvinceResponse(responseText);
                } else if ("city".equals(type)) {
                    result = Utility.handleCityResponse(responseText, mSelectedProvince.code);
                } else if ("county".equals(type)) {
                    result = Utility.handleCountyResponse(responseText, mSelectedCity.code);
                }
                if (result) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)) {
                                queryProvinces();
                            } else if ("city".equals(type)) {
                                queryCities();
                            } else if ("county".equals(type)) {
                                queryCounties();
                            }
                        }
                    });
                }
            }
        });
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setMessage("正在加载...");
            mProgressDialog.setCanceledOnTouchOutside(false);
        }
        mProgressDialog.show();
    }

    private void closeProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        if (mCurrentLevel == LEVEL_COUNTY) {
            queryCities();
        } else if (mCurrentLevel == LEVEL_CITY) {
            queryProvinces();
        }
    }
}

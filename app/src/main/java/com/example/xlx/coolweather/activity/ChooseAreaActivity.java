package com.example.xlx.coolweather.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.xlx.coolweather.R;
import com.example.xlx.coolweather.db.CoolWeatherDB;
import com.example.xlx.coolweather.model.City;
import com.example.xlx.coolweather.model.County;
import com.example.xlx.coolweather.model.Province;
import com.example.xlx.coolweather.util.HttpCallbackListener;
import com.example.xlx.coolweather.util.HttpUtil;
import com.example.xlx.coolweather.util.Utility;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/6/3.
 */
public class ChooseAreaActivity extends Activity {
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    private ProgressDialog progressDialog;

    private TextView titleText;

    private ListView listView;

    private ArrayAdapter<String> adapter;

    private CoolWeatherDB coolWeatherDB;

    private List<String> dataList = new ArrayList<String>();

//    省列表
    private List<Province> provinceList;

//    市列表
    private List<City> cityList;

//    县列表
    private List<County> countyList;

//    选中的省
    private Province selectedProvince;

//    选中的市
    private City selectedCity;

//    选中的级别
    private int currentLevel;

//    判断是否来自weatherActivity
    private boolean isFromWeatherActivity;

    private static final String TAG = "ChooseAreaActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        isFromWeatherActivity = getIntent().getBooleanExtra("from_weather_activity", false);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(prefs.getBoolean("city_selected", false) && !isFromWeatherActivity){
            Intent intent = new Intent(this, WeatherActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        Log.d(TAG,"onCreate4");

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.choose_area);

//        用于显示标题信息：全国/省/市
        listView = (ListView)findViewById(R.id.list_view);

//        用于显示省/市/县列表
        titleText = (TextView)findViewById(R.id.title_text);

//        把数组中的数据传递给listView，具体讲述见android 第一行代码中的128页。
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);

        coolWeatherDB = CoolWeatherDB.getInstance(this);

//        为ListView注册监听器
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int index, long arg3) {
                Log.d(TAG,"onItemClick the current level is： " + currentLevel);
                if(currentLevel == LEVEL_PROVINCE){
                    Log.d(TAG,"onCreate setOnItemClickListener ready queryCities");
                    selectedProvince = provinceList.get(index);
                    queryCities();
                }
                else if(currentLevel == LEVEL_CITY){
                    Log.d(TAG,"onCreate setOnItemClickListener ready queryCounties");
                    selectedCity = cityList.get(index);
                    queryCounties();
                    Log.d(TAG,"queryCounties selectedCity: " + selectedCity);
                }
                else if(currentLevel == LEVEL_COUNTY){
                    Log.d(TAG,"currentLevel is " + currentLevel + "startActivity(intent);");
                    String countyCode = countyList.get(index).getCountyCode();
                    Intent intent = new Intent(ChooseAreaActivity.this,
                            WeatherActivity.class);
                    intent.putExtra("county_code", countyCode);
                    startActivity(intent);
                    finish();
                }

            }
        });
        Log.d(TAG," queryProvinces() in on create ");
        queryProvinces();

    }

    private void queryProvinces() {
        Log.d(TAG,"queryProvinces");

        provinceList = coolWeatherDB.loadProvinces();
        Log.d(TAG,"queryProvinces provinceList: " + provinceList);
        if(provinceList.size()>0){

            Log.d(TAG,"queryProvinces size>0");

            dataList.clear();
            for(Province province : provinceList){
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText("中国");
            currentLevel = LEVEL_PROVINCE;
            Log.d(TAG,"complete query Province!");
        }else{
            Log.d(TAG,"queryProvinces size<=0");
            queryFromServer(null, "province");
        }
    }

    private void queryCities() {
        Log.d(TAG,"queryCities: queryCities");
        cityList = coolWeatherDB.loadCities(selectedProvince.getId());
        Log.d(TAG,"queryCities cityList: " + cityList);
        if(cityList.size() > 0){
            Log.d(TAG,"queryCities:" + "cityList.size() > 0");
            dataList.clear();
            for(City city:cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectedProvince.getProvinceName());
            currentLevel = LEVEL_CITY;
            Log.d(TAG,"complete query Cities!");
        }else{
            Log.d(TAG,"queryCities" + "cityList.size() <= 0");
            queryFromServer(selectedProvince.getProvinceCode(),"city");
        }

//        queryFromServer(selectedProvince.getProvinceCode(),"city");
    }

    private void queryCounties() {
//        Log.d(TAG,"queryCounties");
        countyList = coolWeatherDB.loadCounties(selectedCity.getId());
//        Log.d(TAG,"queryCities countyList: " + countyList);
        if(countyList.size() > 0){
//            Log.d(TAG,"queryCounties" + "countyList.size() > 0");
            dataList.clear();
            for(County county : countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectedCity.getCityName());
            currentLevel = LEVEL_COUNTY;
            Log.d(TAG,"queryCounties complete!");
        }else{
            Log.d(TAG,"queryCounties" + "countyList.size() <= 0");
            queryFromServer(selectedCity.getCityCode(),"county");
        }
    }

    private void queryFromServer(final String code, final String type) {
        String address;
//        Log.d(TAG, "queryFromServer " + type);
        if(!TextUtils.isEmpty(code)){
//            Log.d(TAG, "queryFromServer " + type + " !TextUtils.isEmpty(code)");
            address = "http://www.weather.com.cn/data/list3/city" + code + ".xml";
//            Log.d(TAG,"queryFromServer address :" + address);
        }else{
//            Log.d(TAG, "queryFromServer " + type + " TextUtils.isEmpty(code)");
            address = "http://www.weather.com.cn/data/list3/city.xml";
        }
        Log.d(TAG, "queryFromServer " + "showProgressDialog();");
        showProgressDialog();
//        向服务器发送请求，响应的数据会会回调到onFinish()方法中
        HttpUtil.sendHttpRequest(address,new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                Log.d(TAG, "queryFromServer " + "sendHttpRequest " + response);
                boolean result = false;
                if("province".equals(type)){
                    //处理和解析服务器返回的数据
                    result = Utility.handleProvinceResponse(coolWeatherDB, response);
                }else if("city".equals(type)){
                    result = Utility.handleCitiesResponse(coolWeatherDB, response,
                            selectedProvince.getId());
                }else if("county".equals(type)){
                    result = Utility.handleCountiesResponse(coolWeatherDB, response,
                            selectedCity.getId());
                }
                if(result){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if("province".equals(type)){
                                queryProvinces();
                            }else if("city".equals(type)){
                                queryCities();
                            }else if("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(ChooseAreaActivity.this,"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void closeProgressDialog() {
        if(progressDialog == null){
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    private void showProgressDialog() {
        if(progressDialog != null){
            progressDialog.dismiss();
        }
    }

//    back键的行为
    @Override
    public void onBackPressed(){
        if(currentLevel ==  LEVEL_COUNTY){
            queryCities();
        }else if(currentLevel == LEVEL_CITY){
            queryProvinces();
        }else{
            if(isFromWeatherActivity){
                Intent intent = new Intent(this, WeatherActivity.class);
                startActivity(intent);
            }
            finish();
        }
    }


//    private void queryFromServer(final String code, final String type) {
//    private void queryFromSever(Object o, String province) {
//    }


}

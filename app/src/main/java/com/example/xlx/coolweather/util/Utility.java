package com.example.xlx.coolweather.util;

import android.text.TextUtils;

import com.example.xlx.coolweather.db.CoolWeatherDB;
import com.example.xlx.coolweather.model.City;
import com.example.xlx.coolweather.model.County;
import com.example.xlx.coolweather.model.Province;

/**
 * Created by Administrator on 2016/6/3.
 */
public class Utility {

    public synchronized static boolean handleProvinceResponse(CoolWeatherDB coolWeatherDB,
                                                              String response){
        if(!TextUtils.isEmpty(response)){
            String[] allProvince = response.split(",");
            if(allProvince != null && allProvince.length > 0){
                for( String p : allProvince){
                    String[] array = p.split("\\|");
                    Province province = new Province();
                    province.setProvinceCode(array[0]);
                    province.setProvinceName(array[1]);

                    coolWeatherDB.saveProvince(province);
                }return true;
            }
        }return false;

    }

    public static boolean handleCitiesResponse(CoolWeatherDB coolWeatherDB,
                                               String response, int provinceId){
        if(!TextUtils.isEmpty(response)){
            String[] allCities = response.split(",");
            if(allCities != null && allCities.length > 0){
                for(String c : allCities){
                    String[] array = c.split("\\|");
                    City city = new City();
                    city.setCityCode(array[0]);
                    city.setCityName(array[1]);
                    city.setProvinceId(provinceId);
                    coolWeatherDB.saveCity(city);
                }return true;
            }

        }return false;
    }

    public static boolean handleCountiesResponse(CoolWeatherDB coolWeatherDB, String response,
                                                int cityId){
        if(!TextUtils.isEmpty(response)){
            String[] allcounties = response.split(",");
            if(allcounties != null && allcounties.length > 0){
                for(String c : allcounties){
                    String[] array = c.split("\\|");
                    County county = new County();
                    county.setCountyCode(array[0]);
                    county.setCountyCode(array[1]);
                    county.setCityId(cityId);
                    coolWeatherDB.saveCounty(county);
                }return true;
            }
        }return false;
    }


}

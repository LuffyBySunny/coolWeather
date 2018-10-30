package com.example.droodsunny.coolweather.db;

import org.litepal.crud.DataSupport;

/**
 * Created by DroodSunny on 2017/10/15.
 */

public class City extends DataSupport {
    private int id;
    private String cityName;
    private int cityCodee;
    private int provinceId;
    public City() {
    }
    public int getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(int provinceId) {
        this.provinceId = provinceId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public int getCityCodee() {
        return cityCodee;
    }

    public void setCityCodee(int cityCodee) {
        this.cityCodee = cityCodee;
    }
}

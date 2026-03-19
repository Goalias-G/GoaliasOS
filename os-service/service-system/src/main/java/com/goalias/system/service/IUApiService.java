package com.goalias.system.service;

import com.goalias.system.domain.vo.HomeInfoVo;

public interface IUApiService {


    //0积分
    String saying();

    //1积分
    HomeInfoVo.HotBoard getHotBoard(String type);

    //2积分
    HomeInfoVo.Weather getWeather(String city);

    //4积分
    String translate(String text, String toLanguage);
}

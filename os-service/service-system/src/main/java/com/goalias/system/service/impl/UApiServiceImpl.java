package com.goalias.system.service.impl;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.goalias.system.constant.UApiConstants;
import com.goalias.system.domain.vo.HomeInfoVo;
import com.goalias.system.service.IUApiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class UApiServiceImpl implements IUApiService {

    @Value("${uapi.apiKey}")
    private String apiKey;


    @Override
    public String saying() {
        HttpResponse response = HttpUtil.createGet(UApiConstants.SAYING).bearerAuth(apiKey.trim()).execute();
        return JSONUtil.parseObj(response.body()).getStr("text");
    }

    @Override
    public HomeInfoVo.HotBoard getHotBoard(String type) {
        HttpResponse response = HttpUtil.createGet(UApiConstants.HOT_BOARD).form("type", type).bearerAuth(apiKey.trim()).execute();
        JSONObject jsonObject = JSONUtil.parseObj(response.body());
        return JSONUtil.toBean(jsonObject.toString(), HomeInfoVo.HotBoard.class);
    }

    @Override
    public HomeInfoVo.Weather getWeather(String city) {
        HttpResponse response = HttpUtil.createGet(UApiConstants.WEATHER).bearerAuth(apiKey.trim()).form("city", city).execute();
        JSONObject jsonObject = JSONUtil.parseObj(response.body());
        return JSONUtil.toBean(jsonObject.toString(), HomeInfoVo.Weather.class);
    }

    @Override
    public String translate(String text, String toLanguage) {
        JSONObject request = new JSONObject();
        request.set("text", text);
        request.set("to_lang", toLanguage);
        HttpResponse response = HttpUtil.createPost(UApiConstants.TRANSLATE).bearerAuth(apiKey.trim()).body(request.toString(), "application/json").execute();
        JSONObject jsonObject = JSONUtil.parseObj(response.body());
        return jsonObject.getStr("translate");
    }
}

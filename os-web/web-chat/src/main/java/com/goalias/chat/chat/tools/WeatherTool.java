package com.goalias.chat.chat.tools;

import org.springframework.stereotype.Component;

@Component
public class WeatherTool {

    @OsTool(name = "get_weather", description = "根据城市名称查询当前天气")
    public String getWeather(
            @OsToolParam(name = "city", description = "城市名称，例如 '北京'") String city,
            @OsToolParam(name = "unit", description = "温度单位", required = false) String unit
    ) {
        return "北京天气：晴，25°C";
    }
}

package com.goalias.system.constant;

import java.util.List;

/**
 * @apiNote : <a href="https://uapis.cn/">uapi</a>
 **/
public interface UApiConstants {

    //新闻类型 - 微博、百度、掘金、抖音、网易云音乐、英雄联盟
    List<String> newsTypes = List.of("weibo", "baidu", "juejin", "douyin", "netease-music", "lol");



    //一言
    String SAYING = "https://uapis.cn/api/v1/saying";

    String HOT_BOARD = "https://uapis.cn/api/v1/misc/hotboard";

    //IP信息
    String IP_INFO = "https://uapis.cn/api/v1/network/ipinfo";

    //天气查询
    String WEATHER = "https://uapis.cn/api/v1/misc/weather";

    //翻译
    String TRANSLATE = "https://uapis.cn/api/v1/translate/text";

    //敏感词检测
    String PROFANITY_CHECK = "https://uapis.cn/api/v1/text/profanitycheck";

    //必应壁纸
    String BING_IMAGE = "https://uapis.cn/api/v1/image/bing-daily";


}

package com.goalias.system.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
public class HomeInfoVo {

    private String saying;

    private List<HotBoard> hotBoards;

    private Weather weather;

    private AiRecommend aiRecommend;

    @Data
    public static class Weather {
        private String province;
        private String city;
        private String district;
        private String weather;
        private String temperature;
        private String wind_direction;
        private String wind_power;
        private String humidity;//相对湿度
        private String report_time;//报告时间
    }

    @Data
    public static class HotBoard {
        private String type;
        private String update_time;
        private List<NewsInfo> list;

        @Data
        public static class NewsInfo {
            private Integer index;
            private String title;
            private String url;
        }
    }


}

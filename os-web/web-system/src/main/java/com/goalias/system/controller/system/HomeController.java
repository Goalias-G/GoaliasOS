package com.goalias.system.controller.system;

import com.goalias.common.core.domain.R;
import com.goalias.common.core.domain.model.LoginUser;
import com.goalias.common.redis.constant.CacheNames;
import com.goalias.common.redis.service.RedisService;
import com.goalias.common.satoken.utils.LoginHelper;
import com.goalias.system.domain.vo.AiRecommend;
import com.goalias.system.domain.vo.HomeInfoVo;
import com.goalias.system.service.IUApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/home")
public class HomeController {

    private final IUApiService uApiService;

    private final RedisService redisService;


    @GetMapping("/info")
    public R<HomeInfoVo> homeInfo() {
        HomeInfoVo homeInfoVo = new HomeInfoVo();

        LoginUser loginUser = LoginHelper.getLoginUser();
        String location = loginUser.getLoginLocation().split(" ")[0];
        if (!Objects.isNull(location) && !"本机".equals(location)) {
            try {
                HomeInfoVo.Weather weather = uApiService.getWeather(location);
                homeInfoVo.setWeather(weather);
            } catch (Exception e) {
                log.error("获取首页天气失败，地址：{}", location, e);
            }

        }
        String saying = (String) redisService.get(CacheNames.HOME_INFO_QUOTE);

        Map<String, Object> newsMap = redisService.hmGet(CacheNames.HOME_INFO_NEWS);
        List<HomeInfoVo.HotBoard> newsList = new ArrayList<>();
        newsMap.forEach((key, value) -> newsList.add((HomeInfoVo.HotBoard) value));

        AiRecommend aiRecommend = (AiRecommend) redisService.get(CacheNames.HOME_INFO_AI_RECOMMEND);
        if (!LoginHelper.isSuperAdmin() && Objects.nonNull(aiRecommend)) {
            aiRecommend.setLifeAnalysis("只有 Goalias (管理员)才可以进行生活行为分析哦~");
        }

        homeInfoVo.setSaying(saying);
        homeInfoVo.setHotBoards(newsList);
        homeInfoVo.setAiRecommend(aiRecommend);

        return R.ok(homeInfoVo);
    }

    @GetMapping("/translate")
    public R<Object> translate(@RequestParam String text, @RequestParam(defaultValue = "zh-CHS") String toLanguage) {
        return R.ok((Object) uApiService.translate(text, toLanguage));
    }


}

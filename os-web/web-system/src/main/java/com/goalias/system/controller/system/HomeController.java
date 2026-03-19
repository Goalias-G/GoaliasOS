package com.goalias.system.controller.system;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.goalias.common.core.domain.R;
import com.goalias.common.core.domain.model.LoginUser;
import com.goalias.common.redis.constant.CacheNames;
import com.goalias.common.redis.service.RedisService;
import com.goalias.common.satoken.utils.LoginHelper;
import com.goalias.system.domain.vo.AiRecommend;
import com.goalias.system.domain.vo.HomeInfoVo;
import com.goalias.system.domain.vo.LoginVo;
import com.goalias.system.service.IUApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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


    @PostMapping("/info")
    public R<HomeInfoVo> homeInfo() {
        HomeInfoVo homeInfoVo = new HomeInfoVo();

        LoginUser loginUser = LoginHelper.getLoginUser();
        String location = loginUser.getLoginLocation().split(" ")[0];
        if (!Objects.isNull(location) && !"本机".equals(location)) {
            HomeInfoVo.Weather weather = uApiService.getWeather(location);
            homeInfoVo.setWeather(weather);
        }

        String saying = (String) redisService.get(CacheNames.HOME_INFO_QUOTE);

        Map<String, Object> newsMap = redisService.hmGet(CacheNames.HOME_INFO_NEWS);
        List<HomeInfoVo.HotBoard> newsList = new ArrayList<>();
        newsMap.forEach((key, value) -> newsList.add((HomeInfoVo.HotBoard) value));

        AiRecommend aiRecommend = (AiRecommend) redisService.get(CacheNames.HOME_INFO_AI_RECOMMEND);
        if (!LoginHelper.isSuperAdmin()){
            aiRecommend.setLifeAnalysis("只有 Goalias (管理员)才可以进行生活行为分析哦~");
        }

        homeInfoVo.setSaying(saying);
        homeInfoVo.setHotBoards(newsList);
        homeInfoVo.setAiRecommend(aiRecommend);

        return R.ok(homeInfoVo);
    }

    @GetMapping("/translate")
    public R<String> translate(@RequestParam String text, @RequestParam(defaultValue = "zh-CHS") String toLanguage) {
        return R.ok(uApiService.translate(text, toLanguage));
    }


}

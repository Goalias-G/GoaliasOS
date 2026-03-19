package com.goalias.schedule;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.goalias.chat.chat.service.ISseService;
import com.goalias.chat.enums.PromptTemplateEnum;
import com.goalias.common.chat.request.ChatRequest;
import com.goalias.common.core.constant.UserConstants;
import com.goalias.common.redis.constant.CacheNames;
import com.goalias.common.redis.service.RedisService;
import com.goalias.common.satoken.utils.LoginHelper;
import com.goalias.system.constant.UApiConstants;
import com.goalias.system.domain.DailyHealth;
import com.goalias.system.domain.DailyKnowledge;
import com.goalias.system.domain.bo.DailyHealthBo;
import com.goalias.system.domain.vo.AiRecommend;
import com.goalias.system.domain.vo.HomeInfoVo;
import com.goalias.system.mapper.DailyKnowledgeMapper;
import com.goalias.system.service.IDailyHealthService;
import com.goalias.system.service.IUApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 应用启动Runner
 *
 * @author Goalias
 */
@Slf4j
@RequiredArgsConstructor
@Component
@Profile("prod")
public class HomeInfoSchedule {

    private final RedisService redisService;

    private final IUApiService uApiService;

    private final ISseService sseService;

    private final DailyKnowledgeMapper dailyKnowledgeMapper;

    private final IDailyHealthService dailyHealthService;


    /**
     * 每4小时 24积分/天
     */
    @Scheduled(cron = "0 0 8,10,12,14,16,18,20,22 * * ? ")
    public void syncToDatabase() {
        log.info("开始更新首页信息新闻热榜与一言...");
        Map<String, HomeInfoVo.HotBoard> newsMap = new HashMap<>();
        //6积分
        for (String newsType : UApiConstants.newsTypes) {
            HomeInfoVo.HotBoard hotBoard = uApiService.getHotBoard(newsType);
            if (Objects.nonNull(hotBoard) && CollUtil.isNotEmpty(hotBoard.getList())) {
                hotBoard.setList(hotBoard.getList().subList(0, Math.min(hotBoard.getList().size(), 8)));
                newsMap.put(newsType, hotBoard);
            }
        }
        //0
        String saying = uApiService.saying();
        redisService.hmSet(CacheNames.HOME_INFO_NEWS, newsMap);
        redisService.set(CacheNames.HOME_INFO_QUOTE, saying);
        log.info("更新首页信息成功...");
    }

    /**
     * 每天凌晨三点更新
     * {
     * "greeting": "string",
     * "psychology": {
     * "title": "string",
     * "content": "string"
     * },
     * "knowledge": "string",
     * "lifeAnalysis": "string"
     * }
     */
    @Scheduled(cron = "0 0 3 * * ? ")
    public void aiRecommend() {
        log.info("aiRecommend 开始执行");

        DailyHealthBo healthBo = new DailyHealthBo();
        healthBo.setUserId(UserConstants.SUPER_ADMIN_ID);
        healthBo.setStartTime(new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(3)));
        List<DailyHealth> healthList = dailyHealthService.queryList(healthBo);
        String threeDaysHealthData = healthList.toString();
        String jsonResult = sseService.simpleChat(new ChatRequest(), PromptTemplateEnum.AI_RECOMMEND, threeDaysHealthData);
        if (JSONUtil.isTypeJSON(jsonResult)) {
            AiRecommend aiRecommend = JSONUtil.toBean(jsonResult, AiRecommend.class);

            DailyKnowledge psychology = DailyKnowledge.builder()
                    .type("psychology").title(aiRecommend.getPsychology().getTitle()).content(aiRecommend.getPsychology().getContent())
                    .build();
            DailyKnowledge knowledge = DailyKnowledge.builder()
                    .type("knowledge").title(aiRecommend.getKnowledge().getTitle()).content(aiRecommend.getKnowledge().getContent())
                    .build();

            dailyKnowledgeMapper.insert(psychology);
            dailyKnowledgeMapper.insert(knowledge);
            redisService.set(CacheNames.HOME_INFO_AI_RECOMMEND, aiRecommend);
            log.info("aiRecommend 记录信息完成");
        } else {
            log.warn("aiRecommend 执行失败,AI 响应结果{}", jsonResult);
        }

    }
}

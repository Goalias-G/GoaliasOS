package com.goalias.schedule;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.goalias.chat.chat.service.ISseService;
import com.goalias.chat.enums.PromptTemplateEnum;
import com.goalias.common.chat.request.ChatRequest;
import com.goalias.common.core.constant.UserConstants;
import com.goalias.common.core.utils.DateUtils;
import com.goalias.common.notification.core.MailTemplate;
import com.goalias.common.redis.constant.CacheNames;
import com.goalias.common.redis.service.RedisService;
import com.goalias.common.satoken.utils.LoginHelper;
import com.goalias.system.constant.UApiConstants;
import com.goalias.system.domain.DailyHealth;
import com.goalias.system.domain.DailyKnowledge;
import com.goalias.system.domain.bo.DailyHealthBo;
import com.goalias.system.domain.vo.AiRecommend;
import com.goalias.system.domain.vo.HomeInfoVo;
import com.goalias.system.domain.vo.SysUserVo;
import com.goalias.system.mapper.DailyKnowledgeMapper;
import com.goalias.system.service.IDailyHealthService;
import com.goalias.system.service.ISysUserService;
import com.goalias.system.service.IUApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    private final MailTemplate mailTemplate;

    private final ISysUserService userService;

    private final ResourceLoader resourceLoader;


    /**
     * 每4小时 24积分/天
     */
    @Scheduled(cron = "0 0 8,12,16,20 * * ? ")
    public void syncToDatabase() {
        log.info("开始更新首页信息新闻热榜与一言...");
        Map<String, HomeInfoVo.HotBoard> newsMap = new HashMap<>();
        //6积分
        for (String newsType : UApiConstants.newsTypes) {
            HomeInfoVo.HotBoard hotBoard = uApiService.getHotBoard(newsType);
            if (Objects.nonNull(hotBoard) && CollUtil.isNotEmpty(hotBoard.getList())) {
                List<HomeInfoVo.HotBoard.NewsInfo> subList = hotBoard.getList().subList(0, Math.min(hotBoard.getList().size(), 8));
                hotBoard.setList(new ArrayList<>(subList));
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
     * 每天早上六点更新
     * {
     * "greeting": "string",
     * "psychology": {
     * "title": "string",
     * "content": "string"
     * },
     * "knowledge": {
     * * "title": "string",
     * * "content": "string"
     * * },
     * "lifeAnalysis": "string"
     * }
     */
    @Scheduled(cron = "0 0 6 * * ? ")
    public void aiRecommend() {
        log.info("aiRecommend 开始执行");

        DailyHealthBo healthBo = new DailyHealthBo();
        healthBo.setUserId(UserConstants.SUPER_ADMIN_ID);
        healthBo.setStartTime(new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(3)));
        List<DailyHealth> healthList = dailyHealthService.queryList(healthBo);
        List<String> toAiHealthList = healthList.stream()
                .map(dailyHealth -> String.format("{日期: %s,此日记录: %s} ", DateUtils.dateTime(dailyHealth.getCreateTime()), dailyHealth)).toList();
        String jsonResult = sseService.simpleChat(new ChatRequest(), PromptTemplateEnum.AI_RECOMMEND, toAiHealthList);
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

    /**
     *
     */
    @Scheduled(cron = "0 0 22 * * ?")
    public void homeInfoNotice() {
        log.info("开始检查今日健康记录填写情况并通知提醒");
        Long userId = UserConstants.SUPER_ADMIN_ID;
        DailyHealthBo healthBo = new DailyHealthBo();
        healthBo.setUserId(userId);
        healthBo.setStartTime(Date.from(LocalDate.now().atStartOfDay().toInstant(ZoneOffset.of("+8"))));
        healthBo.setEndTime(Date.from(LocalDate.now().atTime(23, 59, 59).toInstant(ZoneOffset.of("+8"))));
        List<DailyHealth> dailyHealths = dailyHealthService.queryList(healthBo);
        if (CollUtil.isEmpty(dailyHealths) || dailyHealths.get(0).getUpTime() == null) {
            log.info("今日没有填写健康记录,开始提醒");
            SysUserVo sysUserVo = userService.selectUserById(userId);

            Resource resource = resourceLoader.getResource("classpath:templates/healthRecordTemplate.html");
            try (InputStream inputStream = resource.getInputStream()) {
                mailTemplate.sendHtmlMail(sysUserVo.getEmail(), "Goalias OS 生活记录提醒", new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
            } catch (Exception e) {
                log.error("读取发送模板文件失败", e);
                throw new RuntimeException(e);
            }
        }
    }


}

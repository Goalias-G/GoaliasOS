package com.goalias.system.controller.monitor;

import com.goalias.common.core.domain.R;
import com.goalias.common.core.utils.StringUtils;
import com.goalias.system.domain.vo.CacheListInfoVo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * 缓存监控
 *
 * @author Goalias
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/monitor/cache")
public class CacheController {

    private final RedisConnectionFactory connectionFactory;

    /**
     * 获取缓存监控列表
     */
    @GetMapping()
    public R<CacheListInfoVo> getInfo() throws Exception {
        RedisConnection connection = connectionFactory.getConnection();
        Properties commandStats = connection.commands().info("commandstats");

        List<Map<String, String>> pieList = new ArrayList<>();
        if (commandStats != null) {
            commandStats.stringPropertyNames().forEach(key -> {
                Map<String, String> data = new HashMap<>(2);
                String property = commandStats.getProperty(key);
                data.put("name", StringUtils.removeStart(key, "cmdstat_"));
                data.put("value", StringUtils.substringBetween(property, "calls=", ",usec"));
                pieList.add(data);
            });
        }

        CacheListInfoVo infoVo = new CacheListInfoVo();
        infoVo.setInfo(connection.commands().info());
        infoVo.setDbSize(connection.commands().dbSize());
        infoVo.setCommandStats(pieList);
        return R.ok(infoVo);
    }

}

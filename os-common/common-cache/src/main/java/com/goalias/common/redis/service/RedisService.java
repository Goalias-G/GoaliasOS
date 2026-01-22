package com.goalias.common.redis.service;

import org.springframework.data.domain.Sort;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("all")
public interface RedisService {

    /**
 * @apiNote: 保存属性
     * @param key
     * @param value
     * @param time 秒
    */
    void set(String key, Object value, long time);

    void set(String key, Object value, Duration time);

    /**
 * @apiNote: 保存属性
     * @param key
     * @param value
    */
    void set(String key, Object value);

    /**
 * @apiNote: 获取属性
     * @param key
     * @return {@link Object}
    */
    Object get(String key);

    /**
 * @apiNote: 删除属性
     * @param key
     * @return {@link Boolean}
    */
    Boolean del(String key);

    /**
 * @apiNote: 批量删除属性
     * @param keys
     * @return {@link Long}
    */
    Long del(List<String> keys);

    /**
 * @apiNote: 设置过期时间
     * @param key
     * @param time
     * @return {@link Boolean}
    */
    Boolean expire(String key, long time);

    /**
 * @apiNote: 获取过期时间
     * @param key
     * @return {@link Long}
    */
    Long getExpire(String key);

    /**
 * @apiNote: 判断key是否存在
     * @param key
     * @return {@link Boolean}
    */
    Boolean hasKey(String key);

    /**
 * @apiNote: 按delta递增
     * @param key
     * @param delta
     * @return {@link Long}
    */
    Long incr(String key, long delta);

    /**
 * @apiNote: 设定过期时间的递增1
     * @param key
     */
    Long incrExpire(String key, long time);

    /**
 * @apiNote: 按delta递减
     * @param key
     * @param delta
     * @return {@link Long}
    */
    Long decr(String key, long delta);

    /**
 * @apiNote: 获取Hash结构中的属性
     * @param key
     * @param hashKey
     * @return {@link Object}
    */
    Object hGet(String key, String hashKey);

    /**
 * @apiNote: 向Hash结构中放入一个属性
     * @param key
     * @param hashKey
     * @param value
     * @param time
     * @return {@link Boolean}
    */
    Boolean hSet(String key, String hashKey, Object value, long time);

    /**
 * @apiNote: 向Hash结构中放入一个属性
     * @param key
     * @param hashKey
     * @param value
    */
    void hSet(String key, String hashKey, Object value);

    /**
 * @apiNote: 获取hash结构中所有属性
     * @param key
     * @return {@link Map<String,Object>}
    */
    Map<String, Object> hmGet(String key);

    /**
 * @apiNote: 添加多个hash结构
     * @param key
     * @param value
     * @param time
     * @return {@link Boolean}
    */
    Boolean hmSet(String key, Map<String, Object> value, long time);

    /**
 * @apiNote: 添加多个hash结构
     * @param key
     * @param value
    */
    void hmSet(String key, Map<String, ?> value);

    /**
 * @apiNote: 删除Hash结构中的属性
     * @param key
     * @param hashKeys
    */
    void hDel(String key, Object... hashKeys);

    /**
 * @apiNote: 判断Hash结构中是否有该属性
     * @param key
     * @param hashKey
     * @return {@link Boolean}
    */
    Boolean hHasKey(String key, String hashKey);

    /**
 * @apiNote: Hash结构中属性递增
     * @param key
     * @param hashKey
     * @param delta
     * @return {@link Long}
    */
    Long hIncr(String key, String hashKey, Long delta);

    /**
 * @apiNote: Hash结构中属性递减
     * @param key
     * @param hashKey
     * @param delta
     * @return {@link Long}
    */
    Long hDecr(String key, String hashKey, Long delta);

    /**
 * @apiNote: 获取Hash结构长度
     * @param key
     * @return {@link Long}
    */
    Long hSize(String key);

    /**
 * @apiNote: 有序集合中数据递增
     * @param key
     * @param value
     * @param score
     * @return {@link Double}
    */
    Double zIncr(String key, Object value, Double score);

    /**
 * @apiNote: 有序集合中数据递减
     * @param key
     * @param value
     * @param score
     * @return {@link Double}
    */
    Double zDecr(String key, Object value, Double score);

    /**
 * @apiNote: 根据分数排名获取指定元素信息
     * @param key
     * @param start
     * @param end
     * @return {@link Map<Object,Double>}
    */
    Map<Object, Double> zReverseRangeWithScore(String key, long start, long end);

    /**
 * @apiNote: 获取指定元素分数
     * @param key
     * @param value
     * @return {@link Double}
    */
    Double zScore(String key, Object value);

    /**
 * @apiNote: 获取所有分数
     * @param key
     * @return {@link Map<Object,Double>}
    */
    Map<Object, Double> zAllScore(String key);

    /**
 * @apiNote: 删除指定Zset元素
     * @param key
     * @param value
     * @return {@link Long}
    */
    Long zRem(String key, Object... value);

    /**
 * @apiNote: 获取Set结构
     * @param key
     * @return {@link Set<Object>}
    */
    Set<Object> sMembers(String key);

    /**
 * @apiNote: 随机获取指定数量的Set
     * @param key
     * @param count
     * @return {@link List<Object>}
    */
    List<Object> sRandMembers(String key, Long count);

    /**
 * @apiNote: 随机获取Set
     * @param key
     * @return {@link Object}
    */
    Object sRandMember(String key);

    /**
 * @apiNote: 获取不同的随机成员
     * @param key
     * @param count
     * @return {@link Set<Object>}
    */
    Set<Object> sDistinctRandomMembers(String key, Long count);

    /**
 * @apiNote: 向Set结构中添加属性
     * @param key
     * @param values
     * @return {@link Long}
    */
    Long sAdd(String key, Object... values);

    /**
 * @apiNote: 向Set结构中添加属性
     * @param key
     * @param time
     * @param values
    */
    Long sAddExpire(String key, Long time, Object... values);

    /**
 * @apiNote: 是否为Set中的属性
     * @param key
     * @param value
     * @return {@link Boolean}
    */
    Boolean sIsMember(String key, Object value);

    /**
 * @apiNote: Set的长度
     * @param key
     * @return {@link Long}
    */
    Long sSize(String key);

    /**
 * @apiNote: 删除Set中的属性
     * @param key
     * @param values
     * @return {@link Long}
    */
    Long sRemove(String key, Object... values);

    /**
 * @apiNote: 删除Set中的属性
     * @param key
     * @param start
     * @param end
     * @return {@link List<Object>}
    */
    List<Object> lRange(String key, long start, long end);

    /**
 * @apiNote: 获取List中的长度
     * @param key
     * @return {@link Long}
    */
    Long lSize(String key);

    /**
 * @apiNote: 根据索引获取List中的属性
     * @param key
     * @param index
     * @return {@link Object}
    */
    Object lIndex(String key, long index);

    /**
 * @apiNote: 向List中添加属性
     * @param key
     * @param value
    */
    Long lPush(String key, Object value);

    /**
 * @apiNote: 向List中添加属性
     * @param key
     * @param value
     * @param time
     * @return {@link Long}
    */
    Long lPush(String key, Object value, long time);

    /**
 * @apiNote: 向List中批量添加属性
     * @param key
     * @param values
     * @return {@link Long}
    */
    Long lPushAll(String key, Object... values);

    /**
 * @apiNote: 向List中批量添加属性
     * @param key
     * @param time
     * @param values
     * @return {@link Long}
     * @auther apecode
 * @since 2026-01-22    */
    Long lPushAll(String key, Long time, Object... values);

    /**
 * @apiNote: 从List中移除属性
     * @param key
     * @param count
     * @param value
     * @return {@link Long}
    */
    Long lRemove(String key, long count, Object value);

    /**
 * @apiNote: 向Bitmap中新增值
     * @param key
     * @param offset
     * @param b
     * @return {@link Boolean}
    */
    Boolean bitAdd(String key, int offset, boolean b);
    
    /**
 * @apiNote: 从Bitmap中获取偏移量的值
     * @param key
     * @param offset
     * @return {@link Boolean}
    */
    Boolean bitGet(String key, int offset);

    /**
 * @apiNote: 获取Bitmap的key值总和
     * @param key
     * @return {@link Long}
    */
    Long bitCount(String key);

    /**
 * @apiNote: 获取Bitmap范围值
     * @param key
     * @param limit
     * @param offset
     * @return {@link List<Long>}
    */
    List<Long> bitField(String key, int limit, int offset);

    /**
 * @apiNote: 获取所有Bitmap
     * @param key
     * @return {@link byte}
     * @auther apecode
 * @since 2026-01-22    */
    byte[] bitGetAll(String key);

    /**
 * @apiNote: 向hyperlog中添加数据
     * @param key
     * @param value
     * @return {@link Long}
    */
    Long hyperAdd(String key, Object... value);

    /**
 * @apiNote: 获取hyperlog元素数量
     * @param key
     * @return {@link Long}
    */
    Long hyperGet(String... key);

    /**
 * @apiNote: 删除hyperlog数据
     * @param key
    */
    void hyperDel(String key);

    /**
 * @apiNote: 增加坐标
     * @param key
     * @param x
     * @param y
     * @param name
     * @return {@link Long}
    */
    Long geoAdd(String key, Double x, Double y, String name);

    /**
 * @apiNote: 根据城市名称获取坐标集合
     * @param key
     * @param place
     * @return {@link List<Point>}
    */
    List<Point> geoGetPointList(String key, Object... place);

    /**
 * @apiNote: 计算两个城市之间的距离
     * @param key
     * @param placeOne
     * @param placeTow
     * @return {@link Distance}
    */
    Distance geoCalculationDistance(String key, String placeOne, String placeTow);

    /**
 * @apiNote: 获取附该地点附近的其他地点
     * @param key
     * @param place
     * @param distance
     * @param limit
     * @param sort
     * @return {@link GeoResults<GeoLocation<Object>>}
    */
    GeoResults<RedisGeoCommands.GeoLocation<Object>> geoNearByPlace(String key, String place, Distance distance, long limit, Sort.Direction sort);

    /**
 * @apiNote: 获取地点的hash
     * @param key
     * @param place
     * @return {@link List<String>}
    */
    List<String> geoGetHash(String key, String... place);

}

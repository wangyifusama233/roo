package com.roo.service;

import com.blade.ioc.annotation.Bean;
import com.roo.config.RooConst;
import com.roo.utils.ArrayUtils;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 关系服务
 * <p>
 * 粉丝、关注、收藏、点赞
 *
 * @author biezhi
 * @date 2017/8/2
 */
@Bean
public class RelationService {

    private DB db = DBMaker.fileDB(RooConst.MAPDB_NAME).fileMmapEnable().make();

    /**
     * 获取一个用户的所有粉丝id列表
     *
     * @param uid
     * @return 返回用户id列表
     */
    public List<Long> getFollowers(Long uid) {
        Map<Long, long[]> map = db.treeMap(RooConst.DBKEY_FOLLOW)
                .keySerializer(Serializer.LONG)
                .valueSerializer(Serializer.LONG_ARRAY)
                .counterEnable()
                .createOrOpen();
        return this.getIds(map.get(uid));
    }

    /**
     * 获取一个用户的所有关注
     *
     * @param uid
     * @return 返回用户id列表
     */
    public List<Long> getFollowing(Long uid) {
        Map<String, long[]> map = db.treeMap(RooConst.DBKEY_FOLLOWING)
                .keySerializer(Serializer.STRING)
                .valueSerializer(Serializer.LONG_ARRAY)
                .counterEnable()
                .createOrOpen();
        return this.getIds(map.get(uid));
    }

    /**
     * 获取一个用户收藏的所有帖子
     *
     * @param uid
     * @return 返回帖子id列表
     */
    public List<String> getUserFavorites(Long uid) {
        Map<Long, long[]> map = db.treeMap(RooConst.DBKEY_USER_FAVORITES)
                .keySerializer(Serializer.LONG)
                .valueSerializer(Serializer.LONG_ARRAY)
                .counterEnable()
                .createOrOpen();
        List<Long> longs = this.getIds(map.get(uid));
        if (null != longs && !longs.isEmpty()) {
            return longs.stream()
                    .map(id -> Long.toString(id, 36))
                    .collect(Collectors.toList());
        }
        return null;
    }

    /**
     * 获取一个帖子的所有like人员列表
     *
     * @param tid
     * @return 返回uid列表
     */
    public List<Long> getLikes(String tid) {
        Map<String, long[]> map = db.treeMap(RooConst.DBKEY_TOPIC_LIKES)
                .keySerializer(Serializer.STRING)
                .valueSerializer(Serializer.LONG_ARRAY)
                .counterEnable()
                .createOrOpen();
        return this.getIds(map.get(tid));
    }

    /**
     * 关注一个用户
     *
     * @param uid    被关注的用户
     * @param fansId 粉丝
     */
    public void follow(Long uid, Long fansId) {

        // uid的粉丝+1
        Map<Long, long[]> map = db.treeMap("follow")
                .keySerializer(Serializer.LONG)
                .valueSerializer(Serializer.LONG_ARRAY)
                .counterEnable()
                .createOrOpen();
        map.put(uid, ArrayUtils.append(map.get(uid), fansId));

        // fansId的关注列表+1
        Map<Long, long[]> mapFollowing = db.treeMap("following")
                .keySerializer(Serializer.LONG)
                .valueSerializer(Serializer.LONG_ARRAY)
                .counterEnable()
                .createOrOpen();
        mapFollowing.put(fansId, ArrayUtils.append(mapFollowing.get(fansId), uid));
    }

    /**
     * 取消关注一个用户
     *
     * @param uid    关注的用户uid
     * @param fansId 我的uid
     */
    public void unfollow(Long uid, Long fansId) {

        // uid的粉丝-1
        Map<Long, long[]> map = db.treeMap("follow")
                .keySerializer(Serializer.LONG)
                .valueSerializer(Serializer.LONG_ARRAY)
                .counterEnable()
                .createOrOpen();
        if (map.containsKey(uid)) {
            map.put(uid, ArrayUtils.remove(map.get(uid), fansId));
        } else {
            map.put(uid, new long[]{fansId});
        }

        // fansId的关注列表-1
        Map<Long, long[]> mapFollowing = db.treeMap("following")
                .keySerializer(Serializer.LONG)
                .valueSerializer(Serializer.LONG_ARRAY)
                .counterEnable()
                .createOrOpen();
        if (mapFollowing.containsKey(uid)) {
            mapFollowing.put(fansId, ArrayUtils.remove(mapFollowing.get(fansId), uid));
        } else {
            mapFollowing.put(fansId, new long[]{uid});
        }

    }

    /**
     * 收藏一个帖子
     *
     * @param uid
     * @param tid
     */
    public void favoriteTopic(Long uid, String tid) {
        Map<Long, long[]> map = db.treeMap(RooConst.DBKEY_USER_FAVORITES)
                .keySerializer(Serializer.LONG)
                .valueSerializer(Serializer.LONG_ARRAY)
                .counterEnable()
                .createOrOpen();
        if (map.containsKey(uid)) {
            map.put(uid, ArrayUtils.append(map.get(uid), Long.parseLong(tid, 36)));
        } else {
            map.put(uid, new long[]{Long.parseLong(tid, 36)});
        }
    }

    /**
     * 取消收藏帖子
     *
     * @param uid
     * @param tid
     */
    public void unfavoriteTopic(Long uid, String tid) {
        Map<Long, long[]> map = db.treeMap(RooConst.DBKEY_USER_FAVORITES)
                .keySerializer(Serializer.LONG)
                .valueSerializer(Serializer.LONG_ARRAY)
                .counterEnable()
                .createOrOpen();
        map.put(uid, ArrayUtils.remove(map.get(uid), Long.parseLong(tid, 36)));
    }

    /**
     * 喜欢一个帖子
     *
     * @param uid
     * @param tid
     */
    public void likeTopic(Long uid, String tid) {
        Map<String, long[]> map = db.treeMap(RooConst.DBKEY_TOPIC_LIKES)
                .keySerializer(Serializer.STRING)
                .valueSerializer(Serializer.LONG_ARRAY)
                .counterEnable()
                .createOrOpen();
        if (map.containsKey(tid)) {
            map.put(tid, ArrayUtils.append(map.get(tid), uid));
        } else {
            map.put(tid, new long[]{uid});
        }
    }

    /**
     * 取消喜欢帖子
     *
     * @param uid
     * @param tid
     */
    public void unlikeTopic(Long uid, String tid) {
        Map<String, long[]> map = db.treeMap(RooConst.DBKEY_USER_FAVORITES)
                .keySerializer(Serializer.STRING)
                .valueSerializer(Serializer.LONG_ARRAY)
                .counterEnable()
                .createOrOpen();
        map.put(tid, ArrayUtils.remove(map.get(tid), uid));
    }

    private List<Long> getIds(long[] follwers) {
        if (null == follwers || follwers.length == 0) {
            return null;
        }
        List<Long> followers = new ArrayList<>(follwers.length);
        for (long follwer : follwers) {
            followers.add(follwer);
        }
        return followers;
    }

}
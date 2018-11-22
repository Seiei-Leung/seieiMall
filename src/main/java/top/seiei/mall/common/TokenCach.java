package top.seiei.mall.common;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 服务器本地缓存类
 */
public class TokenCach {

    private static Logger logger = LoggerFactory.getLogger(TokenCach.class);

    public static final String TOKENNAME_PREFIX = "token_";

    // 本地服务器静态内存块
    // CacheBuilder 是一个链式的调用
    // initialCapacity(1000) 表示设置缓存的初始化容量是 1000
    // maximumSize(10000) 表示最大缓存容量，超过后悔移除缓存项（LRU 算法）
    // expireAfterAccess(12, TimeUnit.HOURS) 表示设置有效时长，现在表示12个小时
    // build 方法有两个，带 CacheLoader 抽象类作为参数的这个，就是表示当获取缓存中的key其数值为null时，返回的默认数值
    private static LoadingCache<String, String> loadingCache = CacheBuilder.newBuilder().initialCapacity(1000).maximumSize(10000).expireAfterAccess(12, TimeUnit.HOURS).build(new CacheLoader<String, String>() {
        @Override
        // 默认的数据加载实现，当调用get取值时，若key没有对应的数值，则调用这个方法进行加载
        public String load(String s) throws Exception {
            return null;
        }
    });

    // 存放缓存
    public static void setKey(String key, String value) {
        loadingCache.put(key, value);
    }

    // 获取缓存对应的数值
    public static String getKey(String key) {
        try {
            return loadingCache.get(key);
        } catch (ExecutionException e) {
            logger.error("服务器本地缓存出错", e);
        }
        return null;
    }
}

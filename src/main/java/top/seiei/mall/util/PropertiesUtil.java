package top.seiei.mall.util;


import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * 读取 seieiMall.properties 属性文件，获取对应的键值对
 */
public class PropertiesUtil {
    private static Properties prop;
    private static Logger logger;

    // 读取属性文件
    static {
        String fileName = "/properties/seieiMall.properties";
        prop = new Properties();
        try {
            prop.load(new InputStreamReader(PropertiesUtil.class.getResourceAsStream(fileName)));
        } catch (IOException e) {
            logger.error("配置文件读取异常", e);
        }
    }

    /**
     * 读取键值
     * @param key 键
     * @return 值
     */
    public static String getProperty(String key) {
        String result = prop.getProperty(key.trim());
        if (StringUtils.isBlank(result)) {
            return null;
        }
        return result.trim();
    }


    /**
     * 读取键值，设置默认值返回
     * @param key 键
     * @param defaultvalue 默认值
     * @return 值
     */
    public static String getProperty(String key, String defaultvalue) {
        String result = prop.getProperty(key.trim());
        if (StringUtils.isBlank(result)) {
            return defaultvalue;
        }
        return result.trim();
    }

    public static void main(String[] args) {
        System.out.println(PropertiesUtil.getProperty("password.salt"));
    }
}

package com.mmall.util;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * 描述：属性操作工具
 * 作者：NearJC
 * 时间：2019.4.26
 */
@Slf4j
public class PropertiesUtil {

    private static Properties props;

    static {
        String fileName = "src/main/resources.dev/mmall.properties";
        props = new Properties();
        try {
            props.load(new InputStreamReader(PropertiesUtil.class.getClassLoader().getResourceAsStream(fileName), "UTF-8"));
        } catch (IOException e) {
            log.error("配置文件加载失败", e);
        }
    }

    /**
     * 获取属性值，没有时返回默认值
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public static String getProperty(String key, String defaultValue) {
        String property = props.getProperty(key);
        if (property == null || "".equals(property)) {
            property = defaultValue;
        }

        return property.trim();
    }

    /**
     * 获取属性值
     *
     * @param key
     * @return
     */
    public static String getProperty(String key) {
        String property = props.getProperty(key);
        if (property == null || "".equals(property)) {
            return null;
        }

        return property.trim();
    }
}

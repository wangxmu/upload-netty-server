package com.cnc.qoss.common;

import java.util.Date;
import java.util.Iterator;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 读取配置文件工具类
 * @author Administrator
 * @Email yuxf@chinanetcenter.com
 * @Date 13-12-31 下午5:09
 */
public class ConfigUtil {
	
	static Logger logger = LoggerFactory.getLogger(ConfigUtil.class);
    public static PropertiesConfiguration pc;
    
    
    
    static {
    	setConfigurationByFileName("config.properties");
    }
    
    /**
     * add by luxb
     * 根据配置文件名加载配置文件
     * @param fileName
     */
    public static void setConfigurationByFileName(String fileName){
    	try {
            pc = new PropertiesConfiguration(fileName);
            //pc.load();
            
            Iterator<String> itor = pc.getKeys();
            while(itor.hasNext()){
            	String key = itor.next();
            	logger.info("key:" + key + ",prop:" + pc.getProperty(key));
            }
        } catch (Throwable e) {
        	logger.error("获取日志目录映射", e);
        }
    }

    public static String getString(String key) {
        return pc.getString(key);
    }

    public static String[] getArray(String key) {
        return pc.getStringArray(key);
    }

    public static int getInt(String key) {
        return pc.getInt(key, 0);
    }
    

    public static long getLong(String key) {
        return pc.getLong(key, 0);
    }

    public static boolean getBoolean(String key) {
        return pc.getBoolean(key, false);
    }

    public static int getInt(String key, int defaultValue) {
        return pc.getInt(key, defaultValue);
    }

    /**
     * 按配置文件路径获取配置内容
     *
     * @param filePath 文件路径
     * @return
     */
    public static Configuration getConfiguration(String filePath) {
        PropertiesConfiguration pc = new PropertiesConfiguration();
        try {
            pc.setPath(filePath);
            pc.load();
        } catch (Exception e) {
            logger.error("获取日志目录映射失败", e);
        }
        return pc;
    }

    /**
     * 保存配置内容到文件
     *
     * @param config   配置
     * @param fileName 文件
     */
    public static void saveConfig(Configuration config, String fileName) {
        try {
            PropertiesConfiguration pc = new PropertiesConfiguration();
            pc.append(config);
            pc.setHeader("this is data dir track file." + new Date().toString());
            pc.save(fileName);
        } catch (ConfigurationException e) {
        	logger.error("保存日志目录映射失败", e);
        }
    }
}

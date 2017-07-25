package com.cnc.qoss.common;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.core.JdbcTemplate;


/**
 * Author: YU.X.F
 * Time: 2012-09-10 17:35
 */

/**
 * 获取Spring上下文的工具类
 */
public final class SpringContextUtil implements ApplicationContextAware {
    private static ApplicationContext context;

    @SuppressWarnings("static-access")
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.context = context;
    }

    public static ApplicationContext getApplicationContext() {
        return context;
    }

    public static JdbcTemplate getJdbc(){
    	  return (JdbcTemplate)context.getBean("jdbcTemplate");
    }
    
    public static Object getBean(String beanName) {
        return context.getBean(beanName);
    }

    public static <T> T getBean(Class<T> clazz) {
        return context.getBean(clazz);
    }
}

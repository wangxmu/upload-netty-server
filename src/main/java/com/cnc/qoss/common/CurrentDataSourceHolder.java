package com.cnc.qoss.common;
/**
 * 切换动态数据源
 * @author chensl
 *
 */
public class CurrentDataSourceHolder {
	private static final ThreadLocal contextHolder = new ThreadLocal();
    public static void setCurrentDataSource(String dataSourceName) {
        contextHolder.set(dataSourceName);
    }
    public static String getCurrentDataSource() {
        return (String) contextHolder.get();
    }
    public static void clearCurrentDataSource() {
        contextHolder.remove();
    }
}

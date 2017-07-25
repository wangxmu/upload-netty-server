package com.cnc.qoss.common;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
/**
 * 动态数据源接口
 * @author chensl
 *
 */
public class DynamicDataSource extends AbstractRoutingDataSource {

	@Override
	protected Object determineCurrentLookupKey() {
		 return CurrentDataSourceHolder.getCurrentDataSource();
	}

}

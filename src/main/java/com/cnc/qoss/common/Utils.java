package com.cnc.qoss.common;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import com.alibaba.fastjson.JSON;
/**
 * 通用工具类
 * @author chensl
 *
 */
public class Utils {
	private static Logger logger = LoggerFactory.getLogger(Utils.class);
	public static  String getTimeByIndex(int index,int day){
		DateTime dateTime = new DateTime(2015, day, 03, 0, 55);
		dateTime = dateTime.plusMinutes(5*index);
		return dateTime.toString("yyyyMMddHHmm"); 
	}
	
	public static void initDb(JdbcTemplate jdbc){
		jdbc.queryForInt("select 1 from dual");
	}
	
	public static int queryTable(JdbcTemplate jdbc,String table,int i,int day,String channelId){
		
		String sql = "SELECT sql_no_cache count(active_player_count) FROM live_channel_player_count.channel_player_count_" + Utils.getTimeByIndex(i,day) + " where channel_id = '" + channelId  +"'";
		System.out.println(sql);
		int count = jdbc.queryForInt(sql);
		return count;
	}
	
	public static String getHostName(){
		InetAddress ia=null;
		String localName = null;
		try {
			ia=InetAddress.getLocalHost();
			localName=ia.getHostName();
		} catch (Exception e) {
			logger.error("getHostName ERROE!",e);
		} finally{
			return localName;
		}
	}
	
}

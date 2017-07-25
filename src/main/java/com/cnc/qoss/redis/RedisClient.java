package com.cnc.qoss.redis;

import redis.clients.jedis.Jedis;

public class RedisClient {
	public static void main(String[] args) {
		 Jedis jedis = new Jedis("192.168.19.12", 9852);
		 System.out.println(jedis.asking());
	}
}

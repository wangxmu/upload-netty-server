package com.cnc.qoss.upload;


import static com.codahale.metrics.MetricRegistry.name;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory; 

import com.cnc.qoss.common.ConfigUtil;
import com.cnc.qoss.common.Utils;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

/**
 * @author chensl
 * @date 20151225
 * @version 1.0
 */
public class UploadServer {

	private static final String DEFAULT_URL = "/upload";
	
	private static Logger logger = LoggerFactory.getLogger(UploadServer.class);
	
	public void run(final int port, final String url,final KafkaProducer<String, String> producer, final KafkaProducer<String, String> producerBak) throws Exception {
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup)
			.channel(NioServerSocketChannel.class)
			.childHandler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel(SocketChannel ch)
						throws Exception {
					ch.pipeline().addLast("http-decoder",
							new HttpRequestDecoder());
					ch.pipeline().addLast("http-encoder",
							new HttpResponseEncoder());
					ch.pipeline().addLast("http-aggregator",
							new HttpObjectAggregator(1024*1024*100));
					ch.pipeline().addLast("http-chunked",
							new ChunkedWriteHandler());
					ch.pipeline().addLast("fileServerHandler",
							new UploadServerHandler(url,producer,producerBak));
				}
			});
			ChannelFuture future = b.bind( port).sync();
			logger.info("HTTP SERVER STARTED: " + ",port:" + port + ":"
					+ port + url);
			future.channel().closeFuture().sync();
		}catch(Exception e){
			logger.error("fatal error!",e);
		}finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}

	public static void main(String[] args) throws Exception {
//		/**
//		 * 初始化kafka producer
//		 */
		Map<String, Object> props = new HashMap<String, Object>();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, ConfigUtil.pc.getProperty("kafka"));
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
		props.put(ProducerConfig.CLIENT_ID_CONFIG, "uploadServer_"+Utils.getHostName());
		props.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, "10485760");
		props.put(ProducerConfig.BLOCK_ON_BUFFER_FULL_CONFIG, false);
		props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, "335544320");
		KafkaProducer<String, String> producer = new KafkaProducer<String, String>(props);
		
		
		Map<String, Object> props2 = new HashMap<String, Object>();
		props2.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, ConfigUtil.pc.getProperty("kafka_bak"));
		props2.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
		props2.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
		props2.put(ProducerConfig.CLIENT_ID_CONFIG, "uploadServer_bak_"+Utils.getHostName());
		props2.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, "10485760");
		props2.put(ProducerConfig.BLOCK_ON_BUFFER_FULL_CONFIG, false);
		props2.put(ProducerConfig.BUFFER_MEMORY_CONFIG, "335544320");
		KafkaProducer<String, String> producerBak = new KafkaProducer<String, String>(props2);
		/**
		 * 初始化服务器
		 */
		int port = ConfigUtil.getInt("port",8082);
		//采样开始
		new UploadServer().run(port, DEFAULT_URL,producer,producerBak);
	}
}


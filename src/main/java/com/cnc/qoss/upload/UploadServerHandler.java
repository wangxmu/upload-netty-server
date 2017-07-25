package com.cnc.qoss.upload;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaders.Names.LOCATION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpMethod.POST;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.METHOD_NOT_ALLOWED;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import com.google.common.base.Joiner;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpHeaders.Values;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.DiskFileUpload;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;
import io.netty.util.CharsetUtil;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.kafka.clients.producer.BufferExhaustedException;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.jboss.netty.handler.codec.http.multipart.InternalAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cnc.qoss.common.ConfigUtil;
import com.cnc.qoss.common.R;
import com.codahale.metrics.Meter;
import com.google.common.base.Stopwatch;


/**
 * @author chensl
 * @version 1.0
 * @date 20151225
 */
public class UploadServerHandler extends
        SimpleChannelInboundHandler<FullHttpRequest> {
    private final String url;
    private final KafkaProducer<String, String> producer;
    private final KafkaProducer<String, String> producerBak;
    private final String TOPIC;

    private static final HttpDataFactory factory =
            new DefaultHttpDataFactory(1024 * 1024 * 100);
    Logger logger = LoggerFactory.getLogger(UploadServerHandler.class);
    public static final Pattern PATTERN_HOST_URL = Pattern.compile("[/]([^/]+)");

/*	public static void main(String[] args) {
        String body="223.104.13.22 1476932908136|http://p1.pstatp.com/list/f7400035c0a91607027|5941|75|200|5|111.11.31.102^^^^^o1^0^16183728622";
		List<String>fields=new LinkedList<>();
		fields.add(String.valueOf(System.currentTimeMillis()/1000L));
		Matcher matcher = PATTERN_HOST_URL.matcher(body);
		if(matcher.find()){
			String host=matcher.group(1);
			fields.add(host);
		}
		fields.add(body);
		String msg= Joiner.on(R.STRING_CONSTANTS.COLUMN_SEPARATOR_BLANK).join(fields);
		System.out.println(msg);
	}*/

    public UploadServerHandler(String url, KafkaProducer<String, String> producer2, KafkaProducer<String, String> producerBak) {
        this.url = url;
        this.producer = producer2;
        this.producerBak = producerBak;
        TOPIC = ConfigUtil.getString("topic");
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        Stopwatch time = Stopwatch.createStarted();
        /**
         * 打印出头部信息
         */
        logger.debug("------RECEIVED REQUEST----------");
        for (Entry<String, String> entry : request.headers()) {
            logger.debug("HEADER: " + entry.getKey() + '=' + entry.getValue() + "\r\n");
        }

        /**
         * 容错处理
         */
        if (!request.getDecoderResult().isSuccess()) {
            sendError(ctx, BAD_REQUEST);
            return;
        }
        if (request.getMethod() != POST) {
            sendError(ctx, METHOD_NOT_ALLOWED);
            return;
        }
//		
//		InetSocketAddress insocket = (InetSocketAddress) ctx.channel()
//				.remoteAddress();
//		String clientIP = insocket.getAddress().getHostAddress();
//		logger.debug("client_ip:" + clientIP);

        try {
            HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(factory, request);
            List<InterfaceHttpData> datas = decoder.getBodyHttpDatas();

            /**
             * 获取body数据
             */
            ByteBuf buf = request.content();
            byte[] req = new byte[buf.readableBytes()];
            buf.readBytes(req);
            String body = new String(req, "UTF-8");

            if (datas.size() == 0) {
                logger.info("Get msg from body!!!");
                List<String> fields = new LinkedList<>();
                fields.add(String.valueOf(System.currentTimeMillis() / 1000L));
                Matcher matcher = PATTERN_HOST_URL.matcher(body);
                if (matcher.find()) {
                    String host = matcher.group(1);
                    fields.add(host);
                } else {
                    throw new Exception("Couldn't extract host from request: " + body);
                }
                fields.add(body);
                String msg = Joiner.on(R.STRING_CONSTANTS.COLUMN_SEPARATOR_BLANK).join(fields);
                try {
                    producer.send(new ProducerRecord<String, String>(TOPIC, UUID.randomUUID().toString(), msg));
                    producerBak.send(new ProducerRecord<String, String>(TOPIC, UUID.randomUUID().toString(), msg));
                    logger.info(msg);
                } catch (BufferExhaustedException e) {
                    logger.error("cache is full!", e);
                }
            }


            for (InterfaceHttpData interfaceHttpData : datas) {
                if (interfaceHttpData.getHttpDataType() == HttpDataType.FileUpload) {
                    FileUpload fileUpload = (FileUpload) interfaceHttpData;
                    if (fileUpload.isCompleted()) {
                        FileUtils.writeByteArrayToFile(new File(ConfigUtil.getString("file_path") + "/" + fileUpload.getFilename()), fileUpload.get());
                    }
                } else if (interfaceHttpData.getHttpDataType() == HttpDataType.Attribute) {
                    Attribute attribute = (Attribute) interfaceHttpData;
                    String value = attribute.getValue();
                    String name = attribute.getName();

                    StringBuffer sb = new StringBuffer()
                            .append(System.currentTimeMillis() / 1000L)
                            .append(R.STRING_CONSTANTS.COLUMN_SEPARATOR_BLANK)
                            .append(name)
                            .append(R.STRING_CONSTANTS.COLUMN_SEPARATOR_BLANK)
                            .append(value);
                    logger.info(sb.toString());
                    try {
                        producer.send(new ProducerRecord<String, String>(TOPIC, UUID.randomUUID().toString(), sb.toString()));
                        producerBak.send(new ProducerRecord<String, String>(TOPIC, UUID.randomUUID().toString(), sb.toString()));
                    } catch (BufferExhaustedException e) {
                        logger.error("cache is full!", e);
                    }
                }
            }

            String res = "RECEIVED DATA SUCCESS!";
            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
                    OK, Unpooled.wrappedBuffer(res.getBytes("UTF-8")));
            response.headers().set(CONTENT_TYPE, "text/plain");
            response.headers().set(CONTENT_LENGTH,
                    response.content().readableBytes());
            if (HttpHeaders.isKeepAlive(request)) {
                response.headers().set(CONNECTION, Values.KEEP_ALIVE);
            }
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            factory.cleanAllHttpDatas();
            logger.debug("cost_time:" + time.stop());
            logger.debug("------REQUEST ENDDDDDD----------");
        } catch (Exception e) {
            logger.error("unknow error!", e);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        logger.error("ERROR!", cause);
        if (ctx.channel().isActive()) {
            sendError(ctx, INTERNAL_SERVER_ERROR);
        }
    }


    private static void sendError(ChannelHandlerContext ctx,
                                  HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
                status, Unpooled.copiedBuffer("Failure: " + status.toString()
                + "\r\n", CharsetUtil.UTF_8));
        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

}

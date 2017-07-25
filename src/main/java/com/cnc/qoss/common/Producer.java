package com.cnc.qoss.common;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




import java.io.IOException;
import java.util.*;
import java.util.concurrent.Future;

/**
 * Author: lifei
 * Email: lifei@chinanetcenter.com
 * Date: 2015/5/13
 * Description: Kafka生产者
 */
public class Producer {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private KafkaProducer<String,String> producer;
    private String topic;
    private String clientId;
    private  Properties props;
    private Producer() throws IOException {
    }

    public Producer(String topic,String clientId) throws IOException {
        this(topic,clientId,(Properties)null);
    }
    public Producer(String topic,String clientId,Properties props) throws IOException {
        this.topic = topic;
        this.clientId=clientId;
        this.props=props;
        if(props==null){
            props=new Properties();
        }
        props.load(this.getClass().getClassLoader().getResourceAsStream("producer.properties"));
        producer = new KafkaProducer<String,String>(props);
    }
    public Future<RecordMetadata>  produce(String key,Object value) {
        String valueStr=value instanceof String?(String)value:JsonUtils.toString(value);
//        valueStr="test";
        producer.send(new ProducerRecord<String, String>(this.topic,key, valueStr), new Callback() {
			@Override
			public void onCompletion(RecordMetadata metadata, Exception exception) {
				//do nothing
				 System.out.println("send success!");
			}
		});
        //Future<RecordMetadata> metadataFuture= producer.send();
        return null;
    }


    public List<Future<RecordMetadata>>  produce(List<String> keyList,List<?> valueList) {
        if(keyList==null||keyList.size()==0||valueList==null||valueList.size()==0||keyList.size()!=valueList.size()){
            return null;
        }
        List<Future<RecordMetadata>> metadataFutureList=new ArrayList<Future<RecordMetadata>>(keyList.size());
        int index=0;
        for (String key :keyList){
            String valueStr=JsonUtils.toString(valueList.get(index));
            Future<RecordMetadata> metadataFuture= producer.send(new ProducerRecord<String, String>(this.topic, key, valueStr));
            metadataFutureList.add(metadataFuture);
            logger.info("producer produce message，send status: {} ",JsonUtils.toString(metadataFuture));
            index++;
        }
        return metadataFutureList;
    }


    public void close(){
        this.producer.close();
    }

    public static void main(String[] args) throws IOException {
        Producer p = new Producer("pandaTV_live","11211");
        for (int i = 0; i < 100; i++) {
        	System.out.println("produce!");
			p.produce(UUID.randomUUID().toString(),"456");
			System.out.println("success!");
		}
    }
}

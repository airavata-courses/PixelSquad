package com.iu.photosquad.mainuploadmgmt.mainuploadmgmt.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serializer;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

public class KafkaPublisherConfig {
	
	@Bean
	public ProducerFactory<String,Object> producerFactory(){
			Map<String,Object> config = new HashMap<>();
//	        Properties props = new Properties();
	        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
	        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, Serializer.class);
	        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, Serializer.class);
	        return new DefaultKafkaProducerFactory<String,Object>(config);
	    }
	
	@Bean
	public KafkaTemplate<String, Object> kafkaTemplate(){
		return new KafkaTemplate<>(producerFactory());
	}
}

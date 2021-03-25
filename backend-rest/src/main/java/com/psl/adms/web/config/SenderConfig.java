package com.psl.adms.web.config;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.psl.adms.commons.security.AESEncryptionSecurityProvider;
import com.psl.adms.commons.utils.DateMapperUtil;

import javax.jms.ConnectionFactory;

@PropertySource(value ="application-${spring.profiles.active}.properties",
ignoreResourceNotFound = true)
@Configuration
public class SenderConfig {

    @Value("${spring.activemq.broker-url}")
    private String brokerUrl;
    
    @Value("${spring.activemq.borker.username}")
    private String userName;
    
    @Value("${spring.activemq.borker.password}")
    private String password;

    @Value("${spring.jms.cache.session-cache-size}")
    private int sessionCacheSize=1;

    @Value("${spring.activemq.trusted}")
    private List<String> trustedPackages;
    
    @Value("${aes.key}")
    private String aesKey;
    
    @Autowired
   	private AESEncryptionSecurityProvider aesEncryptionSecurityProvider;

    @Bean
    public ConnectionFactory messageSenderActiveMQConnectionFactory() {
        ActiveMQConnectionFactory activeMQConnectionFactory =
                new ActiveMQConnectionFactory();
        activeMQConnectionFactory.setBrokerURL(brokerUrl);
        activeMQConnectionFactory.setTrustAllPackages(false);
        activeMQConnectionFactory.setTrustedPackages(trustedPackages);
        
        activeMQConnectionFactory.setUserName(userName);
        
        String decryptedPassword = aesEncryptionSecurityProvider.decryptAES256(password, aesKey);
        activeMQConnectionFactory.setPassword(decryptedPassword);
        
        return activeMQConnectionFactory;
    }

    @Bean
    public JmsTemplate jmsTopicTemplate() {
        JmsTemplate jmsTopicTemplate =
                new JmsTemplate(messageSenderActiveMQConnectionFactory());
        jmsTopicTemplate.setPubSubDomain(true);
        jmsTopicTemplate.setMessageConverter(jacksonJmsMessageConverter());
        return jmsTopicTemplate;
    }

    @Bean
    public JmsTemplate jmsQueueTemplate() {
        JmsTemplate jmsQueueTemplate =
                new JmsTemplate(messageSenderActiveMQConnectionFactory());
        jmsQueueTemplate.setPubSubDomain(false);
        jmsQueueTemplate.setMessageConverter(jacksonJmsMessageConverter());
        return jmsQueueTemplate;
    }

    @Bean
    public MessageConverter jacksonJmsMessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        converter.setObjectMapper(sourceObjectMapper());
        return converter;
    }

    @Bean
    public ObjectMapper sourceObjectMapper(){
        ObjectMapper mapper = new ObjectMapper();
//        mapper.registerModule(new JavaTimeModule());
//        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        SimpleDateFormat sdf = new SimpleDateFormat(DateMapperUtil.ISO8601_TIMESTAMP_FORMAT_WITH_TIMEZONE);
    	mapper.setDateFormat(sdf);
        return mapper;
    }
}

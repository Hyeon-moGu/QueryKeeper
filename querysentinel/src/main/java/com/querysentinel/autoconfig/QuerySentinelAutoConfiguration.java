package com.querysentinel.autoconfig;

import javax.sql.DataSource;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.querysentinel.collector.LoggingDataSource;

@Configuration
public class QuerySentinelAutoConfiguration {

    @Bean
    public BeanPostProcessor dataSourceWrappingPostProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) {
                if (bean instanceof DataSource && !(bean instanceof LoggingDataSource)) {
                    System.out.println("Wrapping DataSource with LoggingDataSource");
                    return new LoggingDataSource((DataSource) bean);
                }
                return bean;
            }
        };
    }
}

package com.querysentinel.autoconfig;

import com.querysentinel.collector.LoggingDataSource;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

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

package com.querykeeper.autoconfig;

import javax.sql.DataSource;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.querykeeper.collector.DetachedAccessExceptionCatcher;
import com.querykeeper.collector.LoggingDataSource;

@Configuration
public class QueryKeeperAutoConfiguration {

    @Bean
    public BeanPostProcessor dataSourceWrappingPostProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) {
                if (bean instanceof DataSource && !(bean instanceof LoggingDataSource)) {
                    return new LoggingDataSource((DataSource) bean);
                }
                return bean;
            }
        };
    }

    @Bean
    public DetachedAccessExceptionCatcher detachedAccessExceptionCatcher() {
        return new DetachedAccessExceptionCatcher();
    }
}

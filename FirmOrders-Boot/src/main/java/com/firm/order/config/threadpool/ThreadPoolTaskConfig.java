package com.firm.order.config.threadpool;

import java.util.concurrent.Executor;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Configuration
@EnableAsync
@ConfigurationProperties(prefix="threadpooltask")
@Getter @Setter @ToString
public class ThreadPoolTaskConfig {
	private int corePoolSize;
    private int maxPoolSize;
    private int queueCapacity;
    
	@Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.initialize();
        return executor;
    }
}

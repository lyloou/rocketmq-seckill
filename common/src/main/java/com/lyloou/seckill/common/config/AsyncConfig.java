package com.lyloou.seckill.common.config;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import org.apache.tomcat.util.threads.ThreadPoolExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@EnableAsync
public class AsyncConfig {
    @Bean(name = "normalExecutor")
    public Executor normalExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(50);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(500);
        executor.initialize();
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return executor;
    }

    @Bean(name = "producerExecutor")
    public ExecutorService producerExecutor() {
        return new ThreadPoolExecutor(
                5,
                10,
                60,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(50)
                , ThreadFactoryBuilder.create()
                .setDaemon(true)
                .setNamePrefix("producerExecutor - ")
                .build(),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }
}

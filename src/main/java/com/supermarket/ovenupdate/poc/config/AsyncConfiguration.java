package com.supermarket.ovenupdate.poc.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;

/**
 * Configuration for asynchronous task execution.
 * Configures thread pool executor with optimal sizing for concurrent file uploads.
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfiguration implements AsyncConfigurer {

    private static final int CORE_POOL_SIZE = 5;
    private static final int MAX_POOL_SIZE = 20;
    private static final int QUEUE_CAPACITY = 100;
    private static final int KEEP_ALIVE_SECONDS = 60;
    private static final String THREAD_NAME_PREFIX = "async-executor-";

    /**
     * Configures the thread pool task executor for asynchronous operations.
     * Uses rejection policy with fallback to caller thread when pool is exhausted.
     *
     * @return configured thread pool task executor
     */
    @Bean(name = "customTaskExecutor")
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        executor.setCorePoolSize(CORE_POOL_SIZE);
        executor.setMaxPoolSize(MAX_POOL_SIZE);
        executor.setQueueCapacity(QUEUE_CAPACITY);
        executor.setThreadNamePrefix(THREAD_NAME_PREFIX);
        executor.setKeepAliveSeconds(KEEP_ALIVE_SECONDS);
        executor.setAllowCoreThreadTimeOut(false);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.setRejectedExecutionHandler(createRejectionPolicy());
        executor.setTaskDecorator(runnable -> runnable);
        executor.initialize();
        
        log.info("Custom async executor initialized with corePoolSize={}, maxPoolSize={}, queueCapacity={}",
                CORE_POOL_SIZE, MAX_POOL_SIZE, QUEUE_CAPACITY);
        
        return executor;
    }

    /**
     * Creates rejection handler that executes rejected tasks in caller thread.
     * Provides throttling mechanism when thread pool and queue are full.
     */
    private RejectedExecutionHandler createRejectionPolicy() {
        return (runnable, executor) -> {
            log.warn("Task rejected from async executor. Queue is full and max threads reached. " +
                    "Active threads: {}, Queue size: {}, Max pool size: {}",
                    executor.getActiveCount(),
                    executor.getQueue().size(),
                    executor.getMaximumPoolSize());
            
            if (!executor.isShutdown()) {
                log.info("Executing rejected task in caller's thread");
                runnable.run();
            }
        };
    }

    /**
     * Provides exception handler for uncaught exceptions in async methods.
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new CustomAsyncExceptionHandler();
    }

    /**
     * Handles uncaught exceptions in asynchronous method execution.
     */
    @Slf4j
    private static class CustomAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

        @Override
        public void handleUncaughtException(Throwable throwable, Method method, Object... params) {
            log.error("Uncaught exception in async method: {}.{}",
                    method.getDeclaringClass().getSimpleName(),
                    method.getName(),
                    throwable);
            
            log.error("Method parameters: {}", (Object) params);
        }
    }
}

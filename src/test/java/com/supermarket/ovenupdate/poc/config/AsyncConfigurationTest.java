package com.supermarket.ovenupdate.poc.config;

import org.junit.jupiter.api.Test;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.*;

class AsyncConfigurationTest {

    private final AsyncConfiguration asyncConfiguration = new AsyncConfiguration();

    @Test
    void testGetAsyncExecutor_CreatesExecutorWithCorrectConfiguration() {
        Executor executor = asyncConfiguration.getAsyncExecutor();

        assertNotNull(executor);
        assertTrue(executor instanceof ThreadPoolTaskExecutor);

        ThreadPoolTaskExecutor taskExecutor = (ThreadPoolTaskExecutor) executor;
        assertEquals(5, taskExecutor.getCorePoolSize());
        assertEquals(20, taskExecutor.getMaxPoolSize());
        assertNotNull(taskExecutor.getThreadNamePrefix());
        assertTrue(taskExecutor.getThreadNamePrefix().startsWith("async-executor"));
        assertNotNull(taskExecutor.getThreadPoolExecutor());
    }

    @Test
    void testGetAsyncUncaughtExceptionHandler_ReturnsNonNull() {
        AsyncUncaughtExceptionHandler handler = asyncConfiguration.getAsyncUncaughtExceptionHandler();
        
        assertNotNull(handler);
    }

    @Test
    void testAsyncUncaughtExceptionHandler_HandlesException() {
        AsyncUncaughtExceptionHandler handler = asyncConfiguration.getAsyncUncaughtExceptionHandler();
        
        Exception testException = new RuntimeException("Test exception");
        final Method testMethod;
        try {
            testMethod = this.getClass().getDeclaredMethod("testAsyncUncaughtExceptionHandler_HandlesException");
            assertDoesNotThrow(() -> handler.handleUncaughtException(testException, testMethod, new Object[]{}));
        } catch (NoSuchMethodException e) {
            fail("Test method not found");
        }
    }

    @Test
    void testExecutor_IsInitialized() {
        Executor executor = asyncConfiguration.getAsyncExecutor();
        ThreadPoolTaskExecutor taskExecutor = (ThreadPoolTaskExecutor) executor;
        
        assertNotNull(taskExecutor.getThreadPoolExecutor());
    }

    @Test
    void testExecutor_HasRejectionPolicy() {
        Executor executor = asyncConfiguration.getAsyncExecutor();
        ThreadPoolTaskExecutor taskExecutor = (ThreadPoolTaskExecutor) executor;
        
        assertNotNull(taskExecutor.getThreadPoolExecutor().getRejectedExecutionHandler());
    }
}

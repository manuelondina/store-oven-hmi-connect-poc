package com.mercadona.poc.config;

import javax.servlet.MultipartConfigElement;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.unit.DataSize;
import org.springframework.web.client.RestTemplate;

@JacocoExcludedConfig
@Configuration
@EnableAsync
public class PocConfig {

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}

	@Bean
	public MultipartConfigElement multipartConfigElement() {
		MultipartConfigFactory factory = new MultipartConfigFactory();
		factory.setMaxFileSize(DataSize.ofBytes(100000000L));
		factory.setMaxRequestSize(DataSize.ofBytes(100000000L));
		return factory.createMultipartConfig();
	}
	

	@Bean(name = "customTaskExecutor")
	public ThreadPoolTaskExecutor customTaskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(10);
		executor.setMaxPoolSize(50);
		executor.setQueueCapacity(500);
		executor.setThreadNamePrefix("Hilo: ");
		executor.initialize();
		return executor;
	}
}

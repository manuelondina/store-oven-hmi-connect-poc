package com.mercadona.poc.config;


import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

import org.springframework.stereotype.Component;
import lombok.Getter;
import lombok.Setter;

@JacocoExcludedConfig
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "mercadona")
public class OvenPropertiesConfig {

	private Map<String, String> ovens;
	private Map<String, String> users;
	
	
}

package com.greg.golf.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

@Configuration
public class MessageSourceConfig {
 
	@Bean
	public ResourceBundleMessageSource messageSource() {
		var resourceBundleMessageSource= new ResourceBundleMessageSource();
		resourceBundleMessageSource.setBasenames("i18n/messages"); // directory with messages_XX.properties
		resourceBundleMessageSource.setDefaultEncoding("UTF-8");
		resourceBundleMessageSource.setAlwaysUseMessageFormat(true);
		return resourceBundleMessageSource;
	}
}

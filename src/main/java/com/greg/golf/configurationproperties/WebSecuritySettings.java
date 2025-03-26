package com.greg.golf.configurationproperties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Data;

@SuppressWarnings("unused")
@Data
@ConfigurationProperties("cors")
public class WebSecuritySettings {
	private String allowedOrigins;
}

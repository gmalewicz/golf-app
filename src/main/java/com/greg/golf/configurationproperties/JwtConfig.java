package com.greg.golf.configurationproperties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Data;

@Data
@ConfigurationProperties("jwt")
public class JwtConfig {
	private String secret;
	private String refresh;
}

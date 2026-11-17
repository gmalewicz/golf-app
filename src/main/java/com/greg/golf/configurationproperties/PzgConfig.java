package com.greg.golf.configurationproperties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("pzg")
public class PzgConfig {

	// when false the PZG service is never called
	private boolean enabled = false;

	private String url;

	private String username;

	private String password;

	private int timeoutMs = 3000;
}

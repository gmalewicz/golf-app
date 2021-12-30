package com.greg.golf.configurationproperties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("player")
public class PlayerServiceConfig {
	private String tempPwd;
}

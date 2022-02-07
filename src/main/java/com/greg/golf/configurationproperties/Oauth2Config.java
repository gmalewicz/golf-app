package com.greg.golf.configurationproperties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("oauth2")
public class Oauth2Config {
	private String redirect;
}

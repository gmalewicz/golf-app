package com.greg.golf.configurationproperties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Data;

@Data
@ConfigurationProperties("round")
public class RoundServiceConfig {
	private Integer pageSize;
}

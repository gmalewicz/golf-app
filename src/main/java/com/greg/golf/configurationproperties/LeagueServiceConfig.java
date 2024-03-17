package com.greg.golf.configurationproperties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("league")
public class LeagueServiceConfig {
	private Integer pageSize;
}

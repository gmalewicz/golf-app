package com.greg.golf.configurationproperties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("tournament")
public class TournamentServiceConfig {
	private Integer pageSize;
}

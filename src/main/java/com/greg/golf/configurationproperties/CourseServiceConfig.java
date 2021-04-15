package com.greg.golf.configurationproperties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Data;

@Data
@ConfigurationProperties("course")
public class CourseServiceConfig {
	private Integer pageSize;
	private Integer minSearchLength;
}

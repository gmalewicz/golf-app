package com.greg.golf.configurationproperties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("course")
public class CourseServiceConfig {
	private Integer pageSize;
	private Integer minSearchLength;
}

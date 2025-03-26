package com.greg.golf.configuration;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SuppressWarnings("unused")
@Configuration
public class ModelMapperConfig  {
 
	@Bean
	public ModelMapper modelMapper() {
	    return new ModelMapper();
	}
}

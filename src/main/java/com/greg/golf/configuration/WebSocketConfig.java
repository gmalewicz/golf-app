package com.greg.golf.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.DefaultManagedTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties(prefix = "cors")
@Slf4j
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	@Getter
	@Setter
	private String allowedOrigins;

	@Override
	public void configureMessageBroker(MessageBrokerRegistry config) {
		config	.enableSimpleBroker("/topic")
				.setTaskScheduler(new DefaultManagedTaskScheduler())
				.setHeartbeatValue(new long[]{0,20000});
		config.setApplicationDestinationPrefixes("/app");
		log.debug("Message broker configured");
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		log.info(this.getAllowedOrigins());
		registry.addEndpoint("/websocket/onlinescorecard")
				.setAllowedOrigins("http://" + this.getAllowedOrigins(),
								   "https://" + this.getAllowedOrigins(),
								   "http://www." + this.getAllowedOrigins(), 
								   "https://www." + this.getAllowedOrigins())
				;
		log.debug("STOMP endpoint registered");
	}
}

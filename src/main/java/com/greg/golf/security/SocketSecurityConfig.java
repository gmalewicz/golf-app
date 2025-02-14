package com.greg.golf.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;

@Configuration
@EnableWebSocketSecurity
@SuppressWarnings("unused")
public class SocketSecurityConfig {

	@SuppressWarnings({"unused", "squid:S1452"})
	@Bean
	AuthorizationManager<Message<?>> authorizationManager(MessageMatcherDelegatingAuthorizationManager.Builder messages) {
		messages

				.simpDestMatchers("/websocket/**").authenticated()
				.anyMessage().authenticated();

		return messages.build();
	}

	@SuppressWarnings("unused")
	// workaround to disable csrf for web socket
	@Bean
	public ChannelInterceptor csrfChannelInterceptor(){
		//disabling csrf
		return new ChannelInterceptor() {

		};
	}
}

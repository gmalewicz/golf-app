package com.greg.golf.configuration;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "cors")
@Slf4j
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	private static final String SIMP_MESSAGE_TYPE = "simpMessageType";
	private static final String STOMP_COMMAND = "stompCommand";

	@Getter
	@Setter
	private String allowedOrigins;

	private MessageChannel outChannel;

	@Override
	public void configureMessageBroker(MessageBrokerRegistry config) {
		config	.enableSimpleBroker("/topic")
				.setTaskScheduler(heartBeatScheduler());
		config.setApplicationDestinationPrefixes("/app");
		log.debug("Message broker configured");
	}

	@Bean
	public TaskScheduler heartBeatScheduler() {
		return new ThreadPoolTaskScheduler();
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

	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.interceptors( new InboundMessageInterceptor() );
	}

	@Override
	public void configureClientOutboundChannel(ChannelRegistration registration) {
		registration.interceptors( new OutboundMessageInterceptor() );
	}

	class OutboundMessageInterceptor implements ChannelInterceptor {
		@Override
		public void postSend(@NonNull Message message,
							 @NonNull MessageChannel channel,
							 boolean sent) {
            log.debug("postSend: {}", message);
			outChannel = channel;
		}
	}

	class InboundMessageInterceptor implements ChannelInterceptor {

		@SuppressWarnings("unchecked")
		@Override
		public Message<?> preSend(@NonNull Message message, @NonNull MessageChannel channel) {
            log.debug("preSend: {}", message);
			GenericMessage<?> genericMessage = (GenericMessage<?>)message;
			MessageHeaders headers = genericMessage.getHeaders();
			String simpSessionId = (String)headers.get( "simpSessionId" );
			if( ( SimpMessageType.MESSAGE.equals( headers.get( SIMP_MESSAGE_TYPE ) ) &&
					StompCommand.SEND.equals( headers.get( STOMP_COMMAND ) ) ) ||
					( SimpMessageType.SUBSCRIBE.equals( headers.get( SIMP_MESSAGE_TYPE ) ) &&
							StompCommand.SUBSCRIBE.equals( headers.get( STOMP_COMMAND ) ) ) &&
							( simpSessionId != null ) ) {
				Map<String, List<String>> nativeHeaders = (Map<String,List<String>>)headers.get( "nativeHeaders" );
				if( nativeHeaders != null ) {
					List<String> receiptList = nativeHeaders.get( "receipt" );
					if( receiptList != null ) {
						String rid = receiptList.getFirst();
                        log.debug("receipt requested: {}", rid);
						sendReceipt( rid, simpSessionId );
					}
				}
			}
			return message;
		}

		private void sendReceipt( String rid, String simpSessionId ) {
			if( outChannel != null ) {
				HashMap<String,Object> rcptHeaders = new HashMap<>();
				rcptHeaders.put( SIMP_MESSAGE_TYPE, SimpMessageType.OTHER );
				rcptHeaders.put( STOMP_COMMAND, StompCommand.RECEIPT );
				rcptHeaders.put( "simpSessionId", simpSessionId );
				HashMap<String,List<String>> nativeHeaders = new HashMap<>();
				ArrayList<String> receiptElements = new ArrayList<>();
				receiptElements.add( rid );
				nativeHeaders.put( "receipt-id", receiptElements );
				rcptHeaders.put( "nativeHeaders",nativeHeaders );
				GenericMessage<byte[]> rcptMsg = new GenericMessage<>( new byte[0],new MessageHeaders( rcptHeaders ) );
				outChannel.send( rcptMsg );
			} else
				log.error("receipt NOT sent" );
		}
	}
}

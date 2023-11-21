package com.greg.golf.security;

import com.greg.golf.security.oauth.GolfAuthenticationFailureHandler;
import com.greg.golf.security.oauth.GolfAuthenticationSuccessHandler;
import com.greg.golf.security.oauth.GolfOAuth2UserService;
import com.greg.golf.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import lombok.Getter;
import lombok.Setter;

@Slf4j
@ConfigurationProperties(prefix = "cors")
@Configuration
@EnableCaching
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfiguration implements WebMvcConfigurer {

	@Getter @Setter private String allowedOrigins;


	private final PasswordEncoder passwordEncoder;

	private final UserService userService;


	@Autowired
	public WebSecurityConfiguration(@Lazy PasswordEncoder passwordEncoder, @Lazy UserService userService) {

		this.passwordEncoder = passwordEncoder;
		this.userService = userService;
	}

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		// configure AuthenticationManager so that it knows from where to load
		// user for matching credentials
		auth.userDetailsService(userService).passwordEncoder(passwordEncoder);
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}

	@Bean
	public DefaultSecurityFilterChain filterChain(HttpSecurity httpSecurity,
										   @Autowired JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
										   @Autowired JwtRequestFilter jwtRequestFilter,
										   @Autowired GolfOAuth2UserService oauth2UserService,
										   @Autowired GolfAuthenticationSuccessHandler golfAuthenticationSuccessHandler,
										   @Autowired GolfAuthenticationFailureHandler golfAuthenticationFailureHandler) throws Exception {

		// Add a filter to validate the tokens with every request
		httpSecurity
			.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

		httpSecurity
			.authorizeHttpRequests(authorize -> authorize
					.requestMatchers("/rest/Authenticate", "/rest/AddPlayer", "/actuator/**", "/api/**", "/oauth2/**")
					.permitAll()
			);

		httpSecurity
			.authorizeHttpRequests(authorize -> authorize
					.anyRequest()
					.authenticated()
			);

		httpSecurity
			.exceptionHandling(exceptionHandling -> exceptionHandling
				.authenticationEntryPoint(jwtAuthenticationEntryPoint));

		httpSecurity
			.oauth2Login(oauth2Login -> oauth2Login
				.userInfoEndpoint(userInfoEndpoint -> userInfoEndpoint
						.userService(oauth2UserService)
				)
				.successHandler(golfAuthenticationSuccessHandler)
				.failureHandler(golfAuthenticationFailureHandler)
		);

		httpSecurity
			.sessionManagement(session -> session
					.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		httpSecurity
			.cors(Customizer.withDefaults());

		CookieCsrfTokenRepository tokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
		XorCsrfTokenRequestAttributeHandler delegate = new XorCsrfTokenRequestAttributeHandler();
		// set the name of the attribute the CsrfToken will be populated on
		delegate.setCsrfRequestAttributeName(null);
		// Use only the handle() method of XorCsrfTokenRequestAttributeHandler and the
		// default implementation of resolveCsrfTokenValue() from CsrfTokenRequestHandler
		CsrfTokenRequestHandler requestHandler = delegate::handle;

		httpSecurity
			.csrf(csrf -> csrf
				.ignoringRequestMatchers ("/rest/Authenticate", "/rest/AddPlayer", "/actuator/**", "/api/**", "/oauth2/**")
				.csrfTokenRepository(tokenRepository)
				.csrfTokenRequestHandler(requestHandler));

		return httpSecurity.build();
	}
	
	@Override
    public void addCorsMappings(CorsRegistry registry) {
		log.info("Attempt to set allowed origins: " + allowedOrigins);
			CorsRegistration cr = registry.addMapping("/**");
			cr.allowedOrigins("http://" + this.getAllowedOrigins(), 
						  "https://" + this.getAllowedOrigins(),
						  "http://www." + this.getAllowedOrigins(),
						  "https://www." + this.getAllowedOrigins());
			cr.allowedMethods("GET", "POST", "PATCH", "DELETE");
    }
   
}

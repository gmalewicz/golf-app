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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
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
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfiguration<CustomOAuth2UserService> extends WebSecurityConfigurerAdapter implements WebMvcConfigurer {

	@Getter @Setter private String allowedOrigins;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

	@Autowired
	private UserService userService;

	@Autowired
	private JwtRequestFilter jwtRequestFilter;

	@Autowired
	private GolfOAuth2UserService oauth2UserService;

	@Autowired
	private GolfAuthenticationSuccessHandler golfAuthenticationSuccessHandler;

	@Autowired
	private GolfAuthenticationFailureHandler golfAuthenticationFailureHandler;

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		// configure AuthenticationManager so that it knows from where to load
		// user for matching credentials
		auth.userDetailsService(userService).passwordEncoder(passwordEncoder);
	}

	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}

	@Override
	protected void configure(HttpSecurity httpSecurity) throws Exception {

		// Add a filter to validate the tokens with every request
		httpSecurity
			.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        
		httpSecurity
			.csrf().disable()
			.authorizeRequests()
			.antMatchers("/rest/Authenticate", "/rest/AddPlayer", "/actuator/**", "/api/**", "/oauth2/**").permitAll()
			//.antMatchers("/websocket/**").authenticated()
			// all other requests need to be authenticated
			.anyRequest().authenticated()
			.and()
			// make sure we use stateless session; session won't be used to
			// store user's state.
			.exceptionHandling()
			.authenticationEntryPoint(jwtAuthenticationEntryPoint)
			.and()
			.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			.and()
			.oauth2Login()
				.userInfoEndpoint()
					.userService(oauth2UserService)
				.and()
				.successHandler(golfAuthenticationSuccessHandler)
				.failureHandler(golfAuthenticationFailureHandler);

		httpSecurity
			.cors();
		 
		httpSecurity
		 	.csrf()
		 	    .ignoringAntMatchers ("/rest/Authenticate", "/rest/AddPlayer", "/actuator/**", "/api/**", "/oauth2/**")
		 		.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
		 		 
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

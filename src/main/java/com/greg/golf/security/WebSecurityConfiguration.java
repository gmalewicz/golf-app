package com.greg.golf.security;

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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.greg.golf.service.PlayerService;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Log4j2
@ConfigurationProperties(prefix = "cors")
@Configuration
@EnableCaching
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter implements WebMvcConfigurer {

	// private static final Logger logger = LogManager.getLogger(WebSecurityConfiguration.class);
	
	@Getter @Setter private String allowedOrigins;
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Autowired
	private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

	@Autowired
	private PlayerService playerService;
	
	@Autowired
	private JwtRequestFilter jwtRequestFilter;
	
	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		// configure AuthenticationManager so that it knows from where to load
		// user for matching credentials
		auth.userDetailsService(playerService).passwordEncoder(passwordEncoder());
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

				.authorizeRequests()
				.antMatchers("/rest/Authenticate", "/rest/AddPlayer", "/actuator/**", "/api/**").permitAll()
				.antMatchers("/websocket/**").authenticated()
				// all other requests need to be authenticated
				.anyRequest().authenticated()
				.and()
				// make sure we use stateless session; session won't be used to
				// store user's state.
				.exceptionHandling()
				.authenticationEntryPoint(jwtAuthenticationEntryPoint)
				.and()
				.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
		
		 httpSecurity
			.cors();
		 
		 httpSecurity
		 	.csrf()
		 		.ignoringAntMatchers ("/rest/Authenticate", "/rest/AddPlayer", "/actuator/**", "/api/**")
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

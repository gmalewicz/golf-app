package com.greg.golf.security;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.greg.golf.configurationproperties.WebSecuritySettings;
import com.greg.golf.service.PlayerService;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Configuration
@EnableCaching
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter implements WebMvcConfigurer {

	private final WebSecuritySettings webSecuritySettings;
	private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
	private final PlayerService playerService;
	private final JwtRequestFilter jwtRequestFilter;
	
	public WebSecurityConfiguration(WebSecuritySettings webSecuritySettings,
			JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint, PlayerService playerService,
			JwtRequestFilter jwtRequestFilter) throws Exception {
		super();
		this.webSecuritySettings = webSecuritySettings;
		this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
		this.playerService = playerService;
		this.jwtRequestFilter = jwtRequestFilter;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
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
				//.csrf().disable()
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
		log.info("Attempt to set allowed origins: " + webSecuritySettings.getAllowedOrigins());
			CorsRegistration cr = registry.addMapping("/**");
			cr.allowedOrigins("http://" + webSecuritySettings.getAllowedOrigins(), 
						  "https://" + webSecuritySettings.getAllowedOrigins(),
						  "http://www." + webSecuritySettings.getAllowedOrigins(),
						  "https://www." + webSecuritySettings.getAllowedOrigins());
			cr.allowedMethods("GET", "POST", "PATCH", "DELETE");
    }
   
}

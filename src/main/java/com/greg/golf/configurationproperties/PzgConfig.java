package com.greg.golf.configurationproperties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("pzg")
public class PzgConfig {

	// when false the PZG service is never called
	private boolean enabled = false;

	private String url;

	// path of the login endpoint, relative to url
	private String loginPath = "/api/_account1/auth/login";

	// path of the member profile endpoint, relative to url; {id} is replaced with pzgId
	private String profilePath = "/api/_account1/member/get_profile?id={id}";

	private String username;

	private String password;

	private int timeoutMs = 3000;
}

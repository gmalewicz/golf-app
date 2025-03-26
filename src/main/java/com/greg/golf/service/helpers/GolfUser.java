package com.greg.golf.service.helpers;

import java.io.Serial;
import java.util.Collection;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import com.greg.golf.entity.Player;

import lombok.EqualsAndHashCode;

@Getter
@EqualsAndHashCode(callSuper = false)
public class GolfUser extends User implements GolfUserDetails {

	@Serial
	private static final long serialVersionUID = 850077990799261062L;

	public GolfUser(String username, String password, Collection<? extends GrantedAuthority> authorities,
			Player player) {
		super(username, password, authorities);

		this.player = player;
	}

    private final transient Player player;
}

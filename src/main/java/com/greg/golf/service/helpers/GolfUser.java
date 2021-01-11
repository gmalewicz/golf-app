package com.greg.golf.service.helpers;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import com.greg.golf.entity.Player;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class GolfUser extends User implements GolfUserDetails {

	private static final long serialVersionUID = 850077990799261062L;

	public GolfUser(String username, String password, Collection<? extends GrantedAuthority> authorities,
			Player player) {
		super(username, password, authorities);

		this.player = player;
	}

	public Player getPlayer() {
		return player;
	}

	private transient Player player;
}

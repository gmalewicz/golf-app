package com.greg.golf.service.helpers;

import org.springframework.security.core.userdetails.UserDetails;

import com.greg.golf.entity.Player;

public interface GolfUserDetails extends UserDetails {
	
	Player getPlayer();

}

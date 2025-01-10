package com.greg.golf.entity.helpers;

public class Common {

	private Common() {
		throw new IllegalStateException("Utility class");
	}

	public static final boolean PLAYER_SEX_MALE = false;

	public static final int ROLE_PLAYER_ADMIN = 0;
	public static final String ADMIN = "ROLE_ADMIN";
	public static final int ROLE_PLAYER_REGULAR = 1;
	public static final String PLAYER = "ROLE_PLAYER";

	public static final int TEE_TYPE_18 = 0;

	public static final int TYPE_PLAYER_LOCAL = 0;
	public static final int TYPE_PLAYER_FACEBOOK = 1;
	public static final int TYPE_PLAYER_GOOGLE = 2;

	public static final int ALL_ROUNDS = 0;

	public static  final int HOLE_GIVEN_UP = 16;


	public static final long ACCESS_TOKEN_LIFETIME = 60L * 60 * 4;
	public static final long REFRESH_TOKEN_LIFETIME = 60L * 60 * 24 * 7;

	// standard rule that was applicable for Royal Golf Club Wilanow in 2022,2023,2024
	// rule is based on STB nett
	public static final int CYCLE_VERSION_2024 = 0;
	// standard rule that was applicable for Royal Golf Club Wilanow starting from  2025
	// rule is based on grand prix approach for 16 best players in each hcp category
	public static final int CYCLE_VERSION_2025 = 1;
}

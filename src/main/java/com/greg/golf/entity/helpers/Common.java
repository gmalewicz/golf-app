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

	public static final int HOLE_GIVEN_UP = 16;


	public static final long ACCESS_TOKEN_LIFETIME = 60L * 60 * 4;
	public static final long REFRESH_TOKEN_LIFETIME = 60L * 60 * 24 * 7;

	public static final int CYCLE_SERIES_STB = 1;
}
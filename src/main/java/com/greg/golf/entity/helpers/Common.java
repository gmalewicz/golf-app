package com.greg.golf.entity.helpers;

public class Common {

	private Common() {
		throw new IllegalStateException("Utility class");
	}

	public static final boolean PLAYER_SEX_MALE = false;

	public static final int ROLE_PLAYER_ADMIN = 0;
	public static final String ADMIN = "ADMIN";
	public static final int ROLE_PLAYER_REGULAR = 1;
	public static final String PLAYER = "PLAYER";

	public static final int TEE_TYPE_18 = 0;

	public static final int TYPE_PLAYER_LOCAL = 0;
	public static final int TYPE_PLAYER_FACEBOOK = 1;
	public static final int TYPE_PLAYER_GOOGLE = 2;

	public static final int ALL_ROUNDS = 0;

	public static  final int HOLE_GIVEN_UP = 16;

}

package com.mashinarius.jenkinszoo.commons;

public interface GlobusConstants
{
	
	/**
	 * GLOBUS Top Node
	 */
	final String TOP_NODE_GLOBUS = "/iba-globus";
	
	/**
	 * Release 48.0
	 */
	final String GPS_TEST1 = TOP_NODE_GLOBUS + "/test1";
	final String GPS_TEST1_BUILD_ID = GPS_TEST1 + "/build";
	final String GPS_TEST1_DB_SCHEME = "737";
	
	/**
	 * Release 47.0
	 */	
	final String GPS_TEST2 = TOP_NODE_GLOBUS + "/test2";
	final String GPS_TEST2_BUILD_ID = GPS_TEST2 + "/build";
	final String GPS_TEST2_DB_SCHEME = "737I";
	
	/**
	 * Release 45.0
	 */
	final String GPS_TEST1_PAI4_BUILD = "";
	final String GPS_TEST1_PAI4_DB_SCHEME = "737I";
}

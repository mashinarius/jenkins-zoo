package com.mashinarius.jenkinszoo.commons;

public interface Constants extends SDBConstants, GlobusConstants
{
	final String HOST = new String("hrytskevich-vv2.iba");
	final Integer PORT = 2181;
	final String CONNECTION = HOST + ":" + PORT;
	final int SESSION_TIMEOUT = 5000;

    /**
     * 200 ms
     */
    final int AWAIT_TIMEOUT = 200;
	final String SLASH = "/";
	

}

/**
 * Author: Carlo Tassi
 * 
 * Enumeration of all message types that can be 
 * sent over chord by Look@me App
 */
package com.dreamteam.lookme.communication;

public enum LookAtMeMessageType {

	PREVIEW, /* Minimal profile data */
	PROFILE, /* Complete profile data */
	INTEREST, /* Interest data */
	SHARED, /* Shared files data */
	LIKE; /* Love game */

	@Override
	public String toString() {
		return name();
	}

}
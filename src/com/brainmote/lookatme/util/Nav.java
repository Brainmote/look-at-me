package com.brainmote.lookatme.util;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.brainmote.lookatme.ChatConversationsActivity;
import com.brainmote.lookatme.ContactActivity;
import com.brainmote.lookatme.EditProfileActivity;
import com.brainmote.lookatme.HelpActivity;
import com.brainmote.lookatme.NearbyActivity;
import com.brainmote.lookatme.SettingsActivity;
import com.brainmote.lookatme.StatisticsActivity;
import com.google.common.collect.HashBiMap;

/**
 * Classe di utilità per la gestione della navigazione tra le varie activity
 * dell'applicazione
 */
public class Nav {

	// costanti per il passaggio di parametri tra le activity
	public final static String NOTIFICATION_KEY_ID = "notification_key_id";
	public final static String NODE_KEY_ID = "node_key_id";
	public final static String CONVERSATION_KEY_ID = "conversation_key_id";
	// costanti private
	private static final String STRING_VOID = "";
	private static final String STRING_KEY_ID = "string_key_id";
	private static final HashBiMap<Class<? extends Activity>, Integer> map;
	static {
		map = HashBiMap.create();
		map.put(EditProfileActivity.class, 0);
		map.put(NearbyActivity.class, 1);
		map.put(ChatConversationsActivity.class, 2);
		map.put(ContactActivity.class, 3);
		map.put(StatisticsActivity.class, 4);
		map.put(SettingsActivity.class, 5);
		map.put(HelpActivity.class, 6);
	}

	/**
	 * Avvia un'activity
	 * 
	 * @param currentActivity
	 * @param destinationActivity
	 */
	public static void startActivity(Activity currentActivity, Class<? extends Activity> destinationActivity) {
		startActivityWithParameters(currentActivity, destinationActivity, new Bundle());
	}

	/**
	 * Avvia un'activity passando un parametro di tipo String
	 * 
	 * @param currentActivity
	 * @param destinationActivity
	 * @param stringParameter
	 */
	public static void startActivityWithString(Activity currentActivity, Class<? extends Activity> destinationActivity, String stringParameter) {
		Bundle bundle = new Bundle();
		bundle.putString(STRING_KEY_ID, stringParameter);
		startActivityWithParameters(currentActivity, destinationActivity, bundle);
	}

	/**
	 * Avvia un'activity passando un gruppo di parametri
	 * 
	 * @param currentActivity
	 * @param destinationActivity
	 * @param bundle
	 */
	public static void startActivityWithParameters(Activity currentActivity, Class<? extends Activity> destinationActivity, Bundle bundle) {
		if (destinationActivity != null) {
			Intent intent = new Intent(currentActivity, destinationActivity).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.putExtras(bundle);
			currentActivity.startActivity(intent);
		}
	}

	/**
	 * Recupera l'eventuale parametro di tipo String passato all'activity
	 * 
	 * @param currentActivity
	 * @return
	 */
	public static String getStringParameter(Activity currentActivity) {
		if (currentActivity != null && currentActivity.getIntent() != null && currentActivity.getIntent().getExtras() != null
				&& currentActivity.getIntent().getExtras().getString(STRING_KEY_ID) != null) {
			return currentActivity.getIntent().getExtras().getString(STRING_KEY_ID);
		} else {
			return STRING_VOID;
		}
	}

	/**
	 * Recupera gli eventuali parametri passati all'activity. In caso non ve ne
	 * siano, viene ritornato un bundle vuoto.
	 * 
	 * @param currentActivity
	 * @return
	 */
	public static Bundle getParameters(Activity currentActivity) {
		if (currentActivity != null && currentActivity.getIntent() != null && currentActivity.getIntent().getExtras() != null) {
			return currentActivity.getIntent().getExtras();
		} else {
			return new Bundle();
		}
	}

	/**
	 * Recupera l'activity corrispondente alla posizione della voce di menu
	 * indicata
	 * 
	 * @param position
	 * @return
	 */
	public static Class<? extends Activity> getActivityFromMenuPosition(int position) {
		return map.inverse().get(position);
	}

	/**
	 * Recupera la posizione dell'activity all'interno delle voci di menu
	 * 
	 * @param activity
	 * @return
	 */
	public static int getMenuPositionFromActivityClass(Class<? extends Activity> activity) {
		return map.containsKey(activity) ? map.get(activity) : -1;
	}
}

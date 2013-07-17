/**
 * Author: Carlo Tassi
 */
package com.dreamteam.lookme.chord;

import java.util.List;

import android.content.Context;
import android.os.Environment;
import android.os.Looper;

import com.dreamteam.lookme.bean.Profile;
import com.dreamteam.lookme.communication.ILookAtMeCommunicationListener;
import com.dreamteam.lookme.communication.ILookAtMeCommunicationManager;
import com.dreamteam.lookme.communication.LookAtMeMessage;
import com.dreamteam.lookme.communication.LookAtMeMessageType;
import com.dreamteam.lookme.communication.LookAtMeNode;
import com.dreamteam.lookme.db.DBOpenHelper;
import com.dreamteam.lookme.db.DBOpenHelperImpl;
import com.dreamteam.lookme.error.LookAtMeErrorManager;
import com.dreamteam.lookme.error.LookAtMeException;
import com.samsung.chord.ChordManager;
import com.samsung.chord.IChordChannel;
import com.samsung.chord.IChordChannelListener;
import com.samsung.chord.IChordManagerListener;
import com.dreamteam.util.Log;

public class LookAtMeChordCommunicationManager implements
		ILookAtMeCommunicationManager {

	public static final String SOCIAL_CHANNEL_NAME = "com.dreamteam.lookme.SOCIAL_CHANNEL";

	public static final String chordFilePath = Environment
			.getExternalStorageDirectory().getAbsolutePath() + "/LookAtMeTmp";

	private ChordManager chord;
	private IChordChannel publicChannel;
	private IChordChannel socialChannel;

	private List<Integer> availableWifiInterface;
	private int currentWifiInterface;

	private LookAtMeChordErrorManager errorManager;

	private ILookAtMeCommunicationListener communicationListener;

	private Context context;
	private Looper looper;

	public LookAtMeChordCommunicationManager(Context context, Looper looper,
			ILookAtMeCommunicationListener communicationListener) {
		Log.d();
		this.context = context;
		this.errorManager = new LookAtMeChordErrorManager();
		this.communicationListener = communicationListener;
	}

	@Override
	public void startCommunication() throws LookAtMeException {
		Log.d();
		chord = ChordManager.getInstance(context);
		// Log.d(TAG, TAGClass + " : " +
		// "LookAtMeChordCommunicationManager creating tmpDir in\n"+chordFilePath);
		// this.chord.setTempDirectory(chordFilePath);
		chord.setHandleEventLooper(looper);
		errorManager.checkError(startChord());
		// notify started communication
		communicationListener.onCommunicationStarted();
	}

	@Override
	public void stopCommunication() {
		Log.d();
		if (publicChannel != null) {
			chord.leaveChannel(ChordManager.PUBLIC_CHANNEL);
		}
		if (socialChannel != null) {
			chord.leaveChannel(SOCIAL_CHANNEL_NAME);
		}
		chord.stop();
		// notify stopped communication
		communicationListener.onCommunicationStopped();
	}

	private IChordChannel joinPublicChannel() {
		Log.d();
		return chord.joinChannel(ChordManager.PUBLIC_CHANNEL,
				new IChordChannelListener() {

					@Override
					public void onNodeLeft(String arg0, String arg1) {
						Log.d("NOT IMPLEMENTED");
					}

					@Override
					public void onNodeJoined(String arg0, String arg1) {
						Log.d("NOT IMPLEMENTED");
					}

					@Override
					public void onFileWillReceive(String arg0, String arg1,
							String arg2, String arg3, String arg4, String arg5,
							long arg6) {
						Log.d("NOT IMPLEMENTED");
					}

					@Override
					public void onFileSent(String arg0, String arg1,
							String arg2, String arg3, String arg4, String arg5) {
						Log.d("NOT IMPLEMENTED");
					}

					@Override
					public void onFileReceived(String arg0, String arg1,
							String arg2, String arg3, String arg4, String arg5,
							long arg6, String arg7) {
						Log.d("NOT IMPLEMENTED");
					}

					@Override
					public void onFileFailed(String arg0, String arg1,
							String arg2, String arg3, String arg4, int arg5) {
						Log.d("NOT IMPLEMENTED");
					}

					@Override
					public void onFileChunkSent(String arg0, String arg1,
							String arg2, String arg3, String arg4, String arg5,
							long arg6, long arg7, long arg8) {
						Log.d("NOT IMPLEMENTED");
					}

					@Override
					public void onFileChunkReceived(String arg0, String arg1,
							String arg2, String arg3, String arg4, String arg5,
							long arg6, long arg7) {
						Log.d("NOT IMPLEMENTED");
					}

					@Override
					public void onDataReceived(String arg0, String arg1,
							String arg2, byte[][] arg3) {
						Log.d("NOT IMPLEMENTED");
					}
				});
	}

	private IChordChannel joinSocialChannel() {
		Log.d();
		return chord.joinChannel(SOCIAL_CHANNEL_NAME,
				new IChordChannelListener() {

					@Override
					public void onNodeLeft(String arg0, String arg1) {
						Log.d();
						communicationListener.onSocialNodeLeft(arg0);
					}

					@Override
					public void onNodeJoined(String arg0, String arg1) {
						Log.d();
						// send a preview profile request
						sendProfilePreviewRequest(arg0);
						// it will be notified after receive his profile
					}

					@Override
					public void onFileWillReceive(String arg0, String arg1,
							String arg2, String arg3, String arg4, String arg5,
							long arg6) {
						Log.d("NOT IMPLEMENTED");
					}

					@Override
					public void onFileSent(String arg0, String arg1,
							String arg2, String arg3, String arg4, String arg5) {
						Log.d("NOT IMPLEMENTED");
					}

					@Override
					public void onFileReceived(String arg0, String arg1,
							String arg2, String arg3, String arg4, String arg5,
							long arg6, String arg7) {
						Log.d("NOT IMPLEMENTED");
					}

					@Override
					public void onFileFailed(String arg0, String arg1,
							String arg2, String arg3, String arg4, int arg5) {
						Log.d("NOT IMPLEMENTED");
					}

					@Override
					public void onFileChunkSent(String arg0, String arg1,
							String arg2, String arg3, String arg4, String arg5,
							long arg6, long arg7, long arg8) {
						Log.d("NOT IMPLEMENTED");
					}

					@Override
					public void onFileChunkReceived(String arg0, String arg1,
							String arg2, String arg3, String arg4, String arg5,
							long arg6, long arg7) {
						Log.d("NOT IMPLEMENTED");
					}

					@Override
					public void onDataReceived(String arg0, String arg1,
							String arg2, byte[][] arg3) {
						Log.d();
						// here can be received profiles, previews, etc., now we
						// will consider only profile preview
						if (arg2.equals(LookAtMeMessageType.PREVIEW_REQUEST
								.name())) {
							// send my profile preview to arg0 node
							sendProfilePreviewResponse(arg0);
						} else if (arg2.equals(LookAtMeMessageType.PREVIEW
								.name())) {
							// get chord message from payload
							byte[] chordMessageByte = arg3[0];
							LookAtMeChordMessage message = LookAtMeChordMessage
									.obtainChordMessage(chordMessageByte, arg0);
							Profile profile = (Profile) message
									.getObject(LookAtMeMessage.PROFILE_KEY);
							LookAtMeNode node = new LookAtMeNode();
							node.setId(arg0);
							node.setProfile(profile);
							communicationListener.onSocialNodeJoined(node);
						}

					}
				});
	}

	private int startChord() {
		Log.d();
		// trying to use INTERFACE_TYPE_WIFIAP, otherwise get the first
		// available interface
		availableWifiInterface = chord.getAvailableInterfaceTypes();
		if (availableWifiInterface == null
				|| availableWifiInterface.size() == 0) {
			return LookAtMeErrorManager.ERROR_NO_INTERFACE_AVAILABLE;
		}
		if (availableWifiInterface.contains(ChordManager.INTERFACE_TYPE_WIFI)) {
			currentWifiInterface = ChordManager.INTERFACE_TYPE_WIFI;
		} else {
			currentWifiInterface = (availableWifiInterface.get(0)).intValue();
		}
		Log.d("connecting with interface " + currentWifiInterface);
		return chord.start(currentWifiInterface, new IChordManagerListener() {

			@Override
			public void onStarted(String arg0, int arg1) {
				Log.d();
				publicChannel = joinPublicChannel();
				socialChannel = joinSocialChannel();
				Log.d("now chord is joined to "
						+ chord.getJoinedChannelList().size() + " channels");
				// sendProfilePreviewRequestAll(); // QUI NON HA EFFETTO????
			}

			@Override
			public void onNetworkDisconnected() {
				Log.d("NOT IMPLEMENTED");
			}

			@Override
			public void onError(int arg0) {
				Log.d("NOT IMPLEMENTED");
			}
		});
	}

	@Override
	public boolean sendProfilePreviewRequestAll() {
		Log.d();
		List<String> socialNodeList = socialChannel.getJoinedNodeList();
		Log.d("there are "
				+ socialNodeList.size() + " nodes joined to social channel");
		return socialChannel.sendDataToAll(
				LookAtMeMessageType.PREVIEW_REQUEST.name(), new byte[0][0]);
	}

	private boolean sendProfilePreviewRequest(String nodeTo) {
		Log.d();
		return socialChannel.sendData(nodeTo,
				LookAtMeMessageType.PREVIEW_REQUEST.name(), new byte[0][0]);
	}

	private boolean sendProfilePreviewResponse(String nodeTo) {
		Log.d();
		LookAtMeChordMessage message = new LookAtMeChordMessage(
				LookAtMeMessageType.PREVIEW);
		message.setSenderNodeName(chord.getName());
		message.setReceiverNodeName(nodeTo);
		// getting my profile
		DBOpenHelper dbOpenHelper = DBOpenHelperImpl.getInstance(this.context);
		Profile myProfile = null;
		try {
			myProfile = dbOpenHelper.getMyProfile();
		} catch (Exception e) {
			Log.d("failed getting my profile");
			e.printStackTrace();
			return false;
		}
		// end getting my profile
		message.putObject(LookAtMeMessage.PROFILE_KEY, myProfile);
		return socialChannel.sendData(nodeTo,
				LookAtMeMessageType.PREVIEW.name(), obtainPayload(message));
	}

	private byte[][] obtainPayload(LookAtMeChordMessage message) {
		byte[][] payload = new byte[1][1];
		payload[0] = message.getBytes();
		return payload;
	}

}

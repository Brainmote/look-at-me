package com.dreamteam.lookme.chord;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Environment;
import android.os.Looper;

import com.dreamteam.lookme.bean.BasicProfile;
import com.dreamteam.lookme.bean.FullProfile;
import com.dreamteam.lookme.bean.Profile;
import com.dreamteam.lookme.constants.AppSettings;
import com.dreamteam.lookme.service.Services;
import com.dreamteam.util.CommonUtils;
import com.dreamteam.util.Log;
import com.samsung.chord.ChordManager;
import com.samsung.chord.IChordChannel;
import com.samsung.chord.IChordChannelListener;
import com.samsung.chord.IChordManagerListener;

public class CommunicationManagerImpl implements CommunicationManager {

	public static final String chordFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ChordTmp";

	private static final byte[][] EMPTY_PAYLOAD = new byte[0][0];

	private ChordManager chord;
	private IChordChannel publicChannel;
	private IChordChannel socialChannel;

	private List<Integer> availableWifiInterface;
	private int currentWifiInterface;

	private ChordErrorManager errorManager;

	private CommunicationListener communicationListener;

	private Context context;
	private Looper looper;

	public CommunicationManagerImpl(Context context, CommunicationListener communicationListener) {
		Log.d();
		this.context = context;
		this.errorManager = new ChordErrorManager();
		this.communicationListener = communicationListener;
	}

	@Override
	public void startCommunication() throws CustomException {
		Log.d();
		chord = ChordManager.getInstance(context);
		// this.chord.setTempDirectory(chordFilePath);
		chord.setHandleEventLooper(looper);
		errorManager.checkError(startChord());
		// notify started communication
		communicationListener.onCommunicationStarted();
	}

	@Override
	public void stopCommunication() {
		Log.d();
		// Se chiudo i canali direttamente va in concurrent modification
		// exception per cui mi serve un astruttura di appoggio da cui prendere
		// i nomi dei canali
		List<String> joinedChannelName = new ArrayList<String>();
		for (IChordChannel channel : chord.getJoinedChannelList()) {
			joinedChannelName.add(channel.getName());
		}
		// Da capire perch� ritorna sempre il messaggio:
		// "can't find channel (com.dreamteam.lookme.SOCIAL_CHANNEL)"
		for (String channelName : joinedChannelName) {
			Log.d("leaving channel " + channelName);
			chord.leaveChannel(channelName);
		}
		chord.stop();
		// notify stopped communication
		communicationListener.onCommunicationStopped();
	}

	private IChordChannel joinPublicChannel() {
		Log.d();
		return chord.joinChannel(ChordManager.PUBLIC_CHANNEL, new IChordChannelListener() {

			@Override
			public void onNodeLeft(String arg0, String arg1) {
				Log.d("NOT IMPLEMENTED");
			}

			@Override
			public void onNodeJoined(String arg0, String arg1) {
				Log.d("NOT IMPLEMENTED");
			}

			@Override
			public void onFileWillReceive(String arg0, String arg1, String arg2, String arg3, String arg4, String arg5, long arg6) {
				Log.d("NOT IMPLEMENTED");
			}

			@Override
			public void onFileSent(String arg0, String arg1, String arg2, String arg3, String arg4, String arg5) {
				Log.d("NOT IMPLEMENTED");
			}

			@Override
			public void onFileReceived(String arg0, String arg1, String arg2, String arg3, String arg4, String arg5, long arg6, String arg7) {
				Log.d("NOT IMPLEMENTED");
			}

			@Override
			public void onFileFailed(String arg0, String arg1, String arg2, String arg3, String arg4, int arg5) {
				Log.d("NOT IMPLEMENTED");
			}

			@Override
			public void onFileChunkSent(String arg0, String arg1, String arg2, String arg3, String arg4, String arg5, long arg6, long arg7, long arg8) {
				Log.d("NOT IMPLEMENTED");
			}

			@Override
			public void onFileChunkReceived(String arg0, String arg1, String arg2, String arg3, String arg4, String arg5, long arg6, long arg7) {
				Log.d("NOT IMPLEMENTED");
			}

			@Override
			public void onDataReceived(String arg0, String arg1, String arg2, byte[][] arg3) {
				Log.d("NOT IMPLEMENTED");
			}
		});
	}

	private IChordChannel joinSocialChannel() {
		Log.d();
		return chord.joinChannel(AppSettings.SOCIAL_CHANNEL_NAME, new IChordChannelListener() {

			@Override
			public void onNodeLeft(String arg0, String arg1) {
				Log.d();
				communicationListener.onNodeLeft(arg0);
			}

			@Override
			public void onNodeJoined(String arg0, String arg1) {
				Log.d();
				// send a preview profile request silently
				sendBasicProfileRequest(arg0);
				// it will be notified after receive his profile
			}

			@Override
			public void onFileWillReceive(String arg0, String arg1, String arg2, String arg3, String arg4, String arg5, long arg6) {
				Log.d("NOT IMPLEMENTED");
			}

			@Override
			public void onFileSent(String arg0, String arg1, String arg2, String arg3, String arg4, String arg5) {
				Log.d("NOT IMPLEMENTED");
			}

			@Override
			public void onFileReceived(String arg0, String arg1, String arg2, String arg3, String arg4, String arg5, long arg6, String arg7) {
				Log.d("NOT IMPLEMENTED");
			}

			@Override
			public void onFileFailed(String arg0, String arg1, String arg2, String arg3, String arg4, int arg5) {
				Log.d("NOT IMPLEMENTED");
			}

			@Override
			public void onFileChunkSent(String arg0, String arg1, String arg2, String arg3, String arg4, String arg5, long arg6, long arg7, long arg8) {
				Log.d("NOT IMPLEMENTED");
			}

			@Override
			public void onFileChunkReceived(String arg0, String arg1, String arg2, String arg3, String arg4, String arg5, long arg6, long arg7) {
				Log.d("NOT IMPLEMENTED");
			}

			@Override
			public void onDataReceived(String arg0, String arg1, String arg2, byte[][] arg3) {
				Log.d();
				// here can be received profiles, previews, etc.
				MessageType messageType = MessageType.valueOf(arg2);
				byte[] chordMessageByte = arg3[0];
				Message message = null;
				if (chordMessageByte != null && chordMessageByte.length > 0) {
					message = Message.obtainChordMessage(chordMessageByte, arg0);
				}
				switch (messageType) {
				case BASIC_PROFILE_REQUEST:
					// send my basic profile to arg0 node only if exists
					if (Services.currentState.getMyBasicProfile() != null) {
						sendBasicProfileResponse(arg0);
					}
					break;
				case BASIC_PROFILE:
					BasicProfile basicProfile = (BasicProfile) message.getObject(MessageType.BASIC_PROFILE.toString());
					Node basicNode = new Node();
					basicNode.setId(arg0);
					basicNode.setProfile(basicProfile);
					communicationListener.onBasicProfileNodeReceived(basicNode);
					break;
				case FULL_PROFILE_REQUEST:
					// send my full profile to arg0 node
					sendFullProfileResponse(arg0);
					break;
				case FULL_PROFILE:
					FullProfile fullProfile = (FullProfile) message.getObject(MessageType.FULL_PROFILE.toString());
					Node fullNode = new Node();
					fullNode.setId(arg0);
					fullNode.setProfile(fullProfile);
					communicationListener.onFullProfileNodeReceived(fullNode);
					break;
				case PROFILE_UPDATE:
					// TODO
					// BasicProfile updatedProfile = (BasicProfile)
					// message.getObject(LookAtMeMessageType.PROFILE_UPDATE.toString());
					// LookAtMeNode updatedNode = new LookAtMeNode();
					// updatedNode.setId(arg0);
					// updatedNode.setProfile(updatedProfile);
					// communicationListener.onSocialNodeUpdated(updatedNode);
					break;
				case START_CHAT_MESSAGE:
					String myId = Services.currentState.getMyBasicProfile().getId();
					String profileId = Services.currentState.getSocialNodeMap().get(arg0).getProfile().getId();
					String chatChannelName = CommonUtils.generateChannelName(myId, profileId);
					joinChatChannel(chatChannelName);
					communicationListener.onStartChatMessageReceived(arg0, chatChannelName);
					break;
				case LIKE:
					communicationListener.onLikeReceived(arg0);
					break;
				default:
					break;
				}
			}
		});
	}

	private IChordChannel joinChatChannel(String channelName) {
		Log.d();
		return chord.joinChannel(channelName, new IChordChannelListener() {

			@Override
			public void onNodeLeft(String arg0, String arg1) {
				Log.d("NOT IMPLEMENTED");
			}

			@Override
			public void onNodeJoined(String arg0, String arg1) {
				Log.d("NOT IMPLEMENTED");
			}

			@Override
			public void onFileWillReceive(String arg0, String arg1, String arg2, String arg3, String arg4, String arg5, long arg6) {
				Log.d("NOT IMPLEMENTED");
			}

			@Override
			public void onFileSent(String arg0, String arg1, String arg2, String arg3, String arg4, String arg5) {
				Log.d("NOT IMPLEMENTED");
			}

			@Override
			public void onFileReceived(String arg0, String arg1, String arg2, String arg3, String arg4, String arg5, long arg6, String arg7) {
				Log.d("NOT IMPLEMENTED");
			}

			@Override
			public void onFileFailed(String arg0, String arg1, String arg2, String arg3, String arg4, int arg5) {
				Log.d("NOT IMPLEMENTED");
			}

			@Override
			public void onFileChunkSent(String arg0, String arg1, String arg2, String arg3, String arg4, String arg5, long arg6, long arg7, long arg8) {
				Log.d("NOT IMPLEMENTED");
			}

			@Override
			public void onFileChunkReceived(String arg0, String arg1, String arg2, String arg3, String arg4, String arg5, long arg6, long arg7) {
				Log.d("NOT IMPLEMENTED");
			}

			@Override
			public void onDataReceived(String arg0, String arg1, String arg2, byte[][] arg3) {
				Log.d();
				// here can be received only chat messages
				MessageType messageType = MessageType.valueOf(arg2);
				switch (messageType) {
				case CHAT_MESSAGE:
					byte[] chordMessageByte = arg3[0];
					Message message = null;
					if (chordMessageByte != null && chordMessageByte.length > 0) {
						message = Message.obtainChordMessage(chordMessageByte, arg0);
					}
					String chatMessage = (String) message.getObject(MessageType.CHAT_MESSAGE.toString());
					communicationListener.onChatMessageReceived(arg0, chatMessage);
					break;
				default:
					break;
				}
			}
		});
	}

	private int startChord() {
		Log.d();
		// trying to use INTERFACE_TYPE_WIFI, otherwise get the first
		// available interface
		availableWifiInterface = chord.getAvailableInterfaceTypes();
		if (availableWifiInterface == null || availableWifiInterface.size() == 0) {
			return ErrorManager.ERROR_NO_INTERFACE_AVAILABLE;
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
				Log.d("now chord is joined to " + chord.getJoinedChannelList().size() + " channels");
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
	public boolean sendBasicProfileRequestAll() {
		Log.d();
		List<String> socialNodeList = socialChannel.getJoinedNodeList();
		Log.d("there are " + socialNodeList.size() + " nodes joined to social channel");
		return socialChannel.sendDataToAll(MessageType.BASIC_PROFILE_REQUEST.name(), EMPTY_PAYLOAD);
	}

	@Override
	public boolean sendBasicProfileAll() {
		// TODO
		Log.d();
		return false;
		// LookAtMeChordMessage message = obtainMyProfileMessage(BASIC_PROFILE,
		// LookAtMeMessageType.PROFILE_UPDATE, null);
		// if (message != null) {
		// return
		// socialChannel.sendDataToAll(LookAtMeMessageType.PROFILE_UPDATE.toString(),
		// obtainPayload(message));
		// } else {
		// return false;
		// }
	}

	private boolean sendBasicProfileRequest(String nodeTo) {
		Log.d();
		return socialChannel.sendData(nodeTo, MessageType.BASIC_PROFILE_REQUEST.name(), EMPTY_PAYLOAD);
	}

	private boolean sendBasicProfileResponse(String nodeTo) {
		Log.d();
		Message message = obtainMyProfileMessage(Services.currentState.getMyBasicProfile(), MessageType.BASIC_PROFILE, nodeTo);
		if (message != null) {
			return socialChannel.sendData(nodeTo, MessageType.BASIC_PROFILE.toString(), obtainPayload(message));
		} else {
			return false;
		}
	}

	@Override
	public boolean sendFullProfileResponse(String nodeTo) {
		Log.d();
		Message message = obtainMyProfileMessage(Services.currentState.getMyFullProfile(), MessageType.FULL_PROFILE, nodeTo);
		if (message != null) {
			return socialChannel.sendData(nodeTo, MessageType.FULL_PROFILE.toString(), obtainPayload(message));
		} else {
			return false;
		}
	}

	private Message obtainMyProfileMessage(Profile myProfile, MessageType type, String receiverNodeName) {
		Log.d();
		Message message = new Message(type);
		message.setSenderNodeName(chord.getName());
		message.setReceiverNodeName(receiverNodeName); // this maybe null
		message.putObject(type.toString(), myProfile);
		return message;
	}

	private byte[][] obtainPayload(Message message) {
		byte[][] payload = new byte[1][1];
		payload[0] = message.getBytes();
		return payload;
	}

	@Override
	public boolean sendFullProfileRequest(String nodeTo) {
		Log.d();
		return socialChannel.sendData(nodeTo, MessageType.FULL_PROFILE_REQUEST.name(), EMPTY_PAYLOAD);
	}

	@Override
	public boolean sendLike(String nodeTo) {
		Log.d();
		return socialChannel.sendData(nodeTo, MessageType.LIKE.name(), EMPTY_PAYLOAD);
	}

	@Override
	public boolean sendStartChatMessage(String nodeTo) {
		Log.d();
		return socialChannel.sendData(nodeTo, MessageType.START_CHAT_MESSAGE.toString(), EMPTY_PAYLOAD);
	}

	@Override
	public boolean sendChatMessage(String nodeTo, String message) {
		Log.d();
		Message chordMessage = new Message(MessageType.CHAT_MESSAGE);
		chordMessage.setSenderNodeName(chord.getName());
		chordMessage.setReceiverNodeName(nodeTo);
		chordMessage.putString(MessageType.CHAT_MESSAGE.toString(), message);
		String myId = Services.currentState.getMyBasicProfile().getId();
		String profileId = Services.currentState.getSocialNodeMap().get(nodeTo).getProfile().getId();
		String chatChannelName = CommonUtils.generateChannelName(myId, profileId);
		IChordChannel chatChannel = chord.getJoinedChannel(chatChannelName);
		if (chatChannel == null) {
			chatChannel = joinChatChannel(chatChannelName);
		}
		return chatChannel.sendData(nodeTo, MessageType.CHAT_MESSAGE.toString(), obtainPayload(chordMessage));
	}

}
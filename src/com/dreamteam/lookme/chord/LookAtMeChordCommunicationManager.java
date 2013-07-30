/**
 * Author: Carlo Tassi
 */
package com.dreamteam.lookme.chord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.os.Environment;
import android.os.Looper;

import com.dreamteam.lookme.CommonActivity;
import com.dreamteam.lookme.bean.BasicProfile;
import com.dreamteam.lookme.bean.FullProfile;
import com.dreamteam.lookme.bean.MessageItem;
import com.dreamteam.lookme.bean.Profile;
import com.dreamteam.lookme.communication.ILookAtMeCommunicationListener;
import com.dreamteam.lookme.communication.ILookAtMeCommunicationManager;
import com.dreamteam.lookme.communication.LookAtMeMessageType;
import com.dreamteam.lookme.communication.LookAtMeNode;
import com.dreamteam.lookme.constants.AppSettings;
import com.dreamteam.lookme.db.DBOpenHelper;
import com.dreamteam.lookme.db.DBOpenHelperImpl;
import com.dreamteam.lookme.error.LookAtMeErrorManager;
import com.dreamteam.lookme.error.LookAtMeException;
import com.dreamteam.util.CommonUtils;
import com.dreamteam.util.Log;
import com.samsung.chord.ChordManager;
import com.samsung.chord.IChordChannel;
import com.samsung.chord.IChordChannelListener;
import com.samsung.chord.IChordManagerListener;

public class LookAtMeChordCommunicationManager implements ILookAtMeCommunicationManager {

	public static final String chordFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/LookAtMeTmp";

	private static byte[][] EMPTY_PAYLOAD = new byte[0][0];
	
	public static Map <String,IChordChannel> chatChannelMap=new HashMap<String,IChordChannel>();
	
	public static Map<String,List<MessageItem>> messagesHistoryMap= new HashMap<String, List<MessageItem>>();

	private ChordManager chord;
	private IChordChannel publicChannel;
	private IChordChannel socialChannel;

	private List<Integer> availableWifiInterface;
	private int currentWifiInterface;

	private LookAtMeChordErrorManager errorManager;

	private ILookAtMeCommunicationListener communicationListener;

	private Context context;
	private Looper looper;

	public LookAtMeChordCommunicationManager(Context context, Looper looper, ILookAtMeCommunicationListener communicationListener) {
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
			chord.leaveChannel(AppSettings.SOCIAL_CHANNEL_NAME);
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
				// here can be received profiles, previews, etc., now we
				LookAtMeMessageType messageType = LookAtMeMessageType.valueOf(arg2);
				byte[] chordMessageByte = arg3[0];
				LookAtMeChordMessage message = null;
				if (chordMessageByte != null && chordMessageByte.length > 0) {
					message = LookAtMeChordMessage.obtainChordMessage(chordMessageByte, arg0);
				}
				switch (messageType) {
				case PREVIEW_REQUEST:
					// send my basic profile to arg0 node
					sendProfilePreviewResponse(arg0);
					break;
				case PREVIEW:
					BasicProfile basicProfile = (BasicProfile) message.getObject(LookAtMeMessageType.PREVIEW.toString());
					LookAtMeNode previewNode = new LookAtMeNode();
					previewNode.setId(arg0);
					previewNode.setProfile(basicProfile);
					communicationListener.onSocialNodeJoined(previewNode);
					break;
				case PROFILE_REQUEST:
					// send my full profile to arg0 node
					sendProfileResponse(arg0);
					break;
				case PROFILE:
					FullProfile fullProfile = (FullProfile) message.getObject(LookAtMeMessageType.PROFILE.toString());
					LookAtMeNode profileNode = new LookAtMeNode();
					profileNode.setId(arg0);
					profileNode.setProfile(fullProfile);
					communicationListener.onSocialNodeProfileReceived(profileNode);
					break;
				case PROFILE_UPDATE:
					BasicProfile updatedProfile = (BasicProfile) message.getObject(LookAtMeMessageType.PROFILE_UPDATE.toString());
					LookAtMeNode updatedNode = new LookAtMeNode();
					updatedNode.setId(arg0);
					updatedNode.setProfile(updatedProfile);
					communicationListener.onSocialNodeUpdated(updatedNode);
					break;
				case CHAT_MESSAGE:
					String chatMessage = (String) message.getObject(LookAtMeMessageType.CHAT_MESSAGE.toString());
					communicationListener.onChatMessageReceived(arg0, chatMessage);
					break;
				case START_CHAT_MESSAGE:						
					String startChatMessage = (String) message.getObject(LookAtMeMessageType.CHAT_MESSAGE.toString());
					chatChannelMap.put(CommonUtils.generateChannelName(arg0, getMyBasicProfile().getId()), joinChannel(CommonUtils.generateChannelName(arg0, getMyBasicProfile().getId())));					 
					List <MessageItem>chat = new ArrayList<MessageItem>();					
					MessageItem messageItem = new MessageItem(null, "prova", true);
					chat.add(messageItem);
					messagesHistoryMap.put(CommonUtils.generateChannelName(arg0, getMyBasicProfile().getId()), chat);
					communicationListener.onChatMessageReceived(arg0, startChatMessage);
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
	
	@Override
	public IChordChannel joinChannel(String channelName) {
		Log.d();
		return chord.joinChannel(channelName, new IChordChannelListener() {

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
				// here can be received profiles, previews, etc., now we
				LookAtMeMessageType messageType = LookAtMeMessageType.valueOf(arg2);
				byte[] chordMessageByte = arg3[0];
				LookAtMeChordMessage message = null;
				if (chordMessageByte != null && chordMessageByte.length > 0) {
					message = LookAtMeChordMessage.obtainChordMessage(chordMessageByte, arg0);
				}
				switch (messageType) {
				case PREVIEW_REQUEST:
					// send my basic profile to arg0 node
					sendProfilePreviewResponse(arg0);
					break;
				case PREVIEW:
					BasicProfile basicProfile = (BasicProfile) message.getObject(LookAtMeMessageType.PREVIEW.toString());
					LookAtMeNode previewNode = new LookAtMeNode();
					previewNode.setId(arg0);
					previewNode.setProfile(basicProfile);
					communicationListener.onSocialNodeJoined(previewNode);
					break;
				case PROFILE_REQUEST:
					// send my full profile to arg0 node
					sendProfileResponse(arg0);
					break;
				case PROFILE:
					FullProfile fullProfile = (FullProfile) message.getObject(LookAtMeMessageType.PROFILE.toString());
					LookAtMeNode profileNode = new LookAtMeNode();
					profileNode.setId(arg0);
					profileNode.setProfile(fullProfile);
					communicationListener.onSocialNodeProfileReceived(profileNode);
					break;
				case PROFILE_UPDATE:
					BasicProfile updatedProfile = (BasicProfile) message.getObject(LookAtMeMessageType.PROFILE_UPDATE.toString());
					LookAtMeNode updatedNode = new LookAtMeNode();
					updatedNode.setId(arg0);
					updatedNode.setProfile(updatedProfile);
					communicationListener.onSocialNodeUpdated(updatedNode);
					break;
				case CHAT_MESSAGE:
					String chatMessage = (String) message.getObject(LookAtMeMessageType.CHAT_MESSAGE.toString());
					communicationListener.onChatMessageReceived(arg0, chatMessage);
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

	private int startChord() {
		Log.d();
		// trying to use INTERFACE_TYPE_WIFIAP, otherwise get the first
		// available interface
		availableWifiInterface = chord.getAvailableInterfaceTypes();
		if (availableWifiInterface == null || availableWifiInterface.size() == 0) {
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
				Log.d("now chord is joined to " + chord.getJoinedChannelList().size() + " channels");
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
		Log.d("there are " + socialNodeList.size() + " nodes joined to social channel");
		return socialChannel.sendDataToAll(LookAtMeMessageType.PREVIEW_REQUEST.name(), EMPTY_PAYLOAD);
	}


	@Override
	public boolean sendProfilePreviewAll() {
		Log.d();
		LookAtMeChordMessage message = null;
		try {
			message = obtainMyProfileMessage(false, LookAtMeMessageType.PROFILE_UPDATE, null);
		} catch (Exception e) {
			Log.d("failed getting my profile");
			e.printStackTrace();
			return false;
		}
		if (message != null) {
			return socialChannel.sendDataToAll(LookAtMeMessageType.PROFILE_UPDATE.toString(), obtainPayload(message));
		} else {
			return false;
		}
	}

	private boolean sendProfilePreviewRequest(String nodeTo) {
		Log.d();
		return socialChannel.sendData(nodeTo, LookAtMeMessageType.PREVIEW_REQUEST.name(), EMPTY_PAYLOAD);
	}

	private boolean sendProfilePreviewResponse(String nodeTo) {
		Log.d();
		LookAtMeChordMessage message = null;
		try {
			message = obtainMyProfileMessage(false, LookAtMeMessageType.PREVIEW, nodeTo);
		} catch (Exception e) {
			Log.d("failed getting my profile");
			e.printStackTrace();
			return false;
		}
		if (message != null) {
			return socialChannel.sendData(nodeTo, LookAtMeMessageType.PREVIEW.toString(), obtainPayload(message));
		} else {
			return false;
		}
	}

	private boolean sendProfileResponse(String nodeTo) {
		Log.d();
		LookAtMeChordMessage message = null;
		try {
			message = obtainMyProfileMessage(true, LookAtMeMessageType.PROFILE, nodeTo);
		} catch (Exception e) {
			Log.d("failed getting my profile");
			e.printStackTrace();
			return false;
		}
		if (message != null) {
			return socialChannel.sendData(nodeTo, LookAtMeMessageType.PROFILE.toString(), obtainPayload(message));
		} else {
			return false;
		}
	}

	private LookAtMeChordMessage obtainMyProfileMessage(boolean fullProfile, LookAtMeMessageType type, String receiverNodeName) throws Exception {
		Log.d();
		LookAtMeChordMessage message = new LookAtMeChordMessage(type);
		message.setSenderNodeName(chord.getName());
		message.setReceiverNodeName(receiverNodeName); // this maybe null
		// getting my profile
		DBOpenHelper dbOpenHelper = DBOpenHelperImpl.getInstance(this.context);
		Profile myProfile = null;
		if (fullProfile) {
			myProfile = dbOpenHelper.getMyFullProfile();
		} else {
			myProfile = dbOpenHelper.getMyBasicProfile();
		}
		// end getting my profile
		message.putObject(type.toString(), myProfile);
		return message;
	}

	private byte[][] obtainPayload(LookAtMeChordMessage message) {
		byte[][] payload = new byte[1][1];
		payload[0] = message.getBytes();
		return payload;
	}
	
	private byte[][] obtainPayload(List arguments) {
		Iterator iter = arguments.iterator();
		int i = 0;
		byte[][] payload = new byte[1][arguments.size()];
		while(iter.hasNext())
		{
			payload[0] = CommonUtils.getBytes(iter.next());
			i++;
		}				
		return payload;
	}
	
	@Override
	public boolean sendProfileRequest(String nodeTo) {
		Log.d();
		return socialChannel.sendData(nodeTo, LookAtMeMessageType.PROFILE_REQUEST.name(), new byte[0][0]);
	}

	@Override
	public boolean sendLike(String nodeTo) {
		Log.d();
		return socialChannel.sendData(nodeTo, LookAtMeMessageType.LIKE.name(), new byte[0][0]);
	}

	@Override
	public boolean sendChatMessage(LookAtMeNode nodeTo, String message,String channelName) {
		Log.d();		
		LookAtMeChordMessage chordMessage = new LookAtMeChordMessage(LookAtMeMessageType.CHAT_MESSAGE);
		chordMessage.setSenderNodeName(chord.getName());
		chordMessage.setReceiverNodeName(nodeTo.getId());
		chordMessage.putString(LookAtMeMessageType.CHAT_MESSAGE.toString(), message);
		if(channelName.equals(AppSettings.SOCIAL_CHANNEL_NAME))
		{
			Profile nodeFrom = null;
			try{
				DBOpenHelper dbOpenHelper = DBOpenHelperImpl.getInstance(this.context);
				
				nodeFrom = dbOpenHelper.getMyBasicProfile();
			}catch(Exception e)
			{
				Log.e("error while sending chat message on social channel,nodeTo: "+nodeTo+" message: "+message);
			}			
			return socialChannel.sendData(nodeTo.getId(), LookAtMeMessageType.START_CHAT_MESSAGE.name(), obtainPayload(chordMessage));
		}			
		else
		{			
			//TODO:GENERARE QUA IL CHANNEL NAME
			if (chatChannelMap.get(channelName)==null)
			{
				chatChannelMap.put(channelName,joinChannel(channelName));
			}				
			IChordChannel selectedChannel = chatChannelMap.get(channelName);
			if(selectedChannel!=null)			
				return selectedChannel.sendData(nodeTo.getId(), LookAtMeMessageType.CHAT_MESSAGE.name(), obtainPayload(chordMessage));
			else
			{
				android.util.Log.d("Error", "sendChatMessage channel not found");
				return false;
			}
		}
	}
	
	@Override
	public boolean sendPrivateChatMessage(String nodeTo, String message,String channelName) {
		Log.d();
		LookAtMeChordMessage chordMessage = new LookAtMeChordMessage(LookAtMeMessageType.CHAT_MESSAGE);
		chordMessage.setSenderNodeName(chord.getName());
		chordMessage.setReceiverNodeName(nodeTo);
		chordMessage.putString(LookAtMeMessageType.CHAT_MESSAGE.toString(), message);
		IChordChannel selectedChannel = chatChannelMap.get(channelName);
		if(selectedChannel!=null)			
			return selectedChannel.sendData(nodeTo, LookAtMeMessageType.CHAT_MESSAGE.name(), obtainPayload(chordMessage));
		else return false;
	}	
	
	@Override
	public IChordChannel getChannel(String channelName)
	{
		return chatChannelMap.get(channelName);
	}
	
	@Override
	public Map<String,IChordChannel> getOpenChannels()
	{
		List <IChordChannel>openChannels = new ArrayList<IChordChannel>();
		openChannels.addAll(chatChannelMap.values());
		return chatChannelMap;
		
	}	
	
	
	public Map<String,List<MessageItem>> getOpenedChat()
	{
		return messagesHistoryMap;
	}
	
	@Override
	public List<MessageItem> getChat(String channelName)
	{
		return messagesHistoryMap.get(channelName);
	}

	
	private static Profile getMyBasicProfile()
	{
		return CommonActivity.myProfile;
	}
	
	

}

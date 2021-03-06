package com.brainmote.lookatme.chord.impl;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Looper;

import com.brainmote.lookatme.ChatConversation;
import com.brainmote.lookatme.bean.BasicProfile;
import com.brainmote.lookatme.bean.ChatConversationImpl;
import com.brainmote.lookatme.bean.FullProfile;
import com.brainmote.lookatme.bean.Profile;
import com.brainmote.lookatme.chord.CommunicationListener;
import com.brainmote.lookatme.chord.CommunicationManager;
import com.brainmote.lookatme.chord.CustomException;
import com.brainmote.lookatme.chord.Message;
import com.brainmote.lookatme.chord.MessageType;
import com.brainmote.lookatme.chord.Node;
import com.brainmote.lookatme.constants.AppSettings;
import com.brainmote.lookatme.service.Services;
import com.brainmote.lookatme.util.CommonUtils;
import com.brainmote.lookatme.util.Log;
import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.chord.InvalidInterfaceException;
import com.samsung.android.sdk.chord.Schord;
import com.samsung.android.sdk.chord.SchordChannel;
import com.samsung.android.sdk.chord.SchordManager;
import com.samsung.android.sdk.chord.SchordManager.StatusListener;

public class CommunicationManagerImpl implements CommunicationManager {

	private static final byte[][] EMPTY_PAYLOAD = new byte[0][0];

	private SchordManager chordManager;
	private SchordChannel socialChannel;
	private CommunicationListener communicationListener;
	private Context context;
	private Integer lastWiFiInterface;
	private boolean shouldRestart;

	public CommunicationManagerImpl(Context context, CommunicationListener communicationListener) {
		this.context = context;
		this.communicationListener = communicationListener;
	}

	@Override
	public void startCommunication() throws CustomException {
		try {
			Services.groupPlayManager.init(context);
		} catch (SsdkUnsupportedException e) {
			// TODO better exception handling
			e.printStackTrace();
		}
		// Create an instance of Schord.
		Schord chord = new Schord();
		try {
			if (chordManager == null) {
				// Initialize an instance of Schord.
				chord.initialize(context);
				chordManager = new SchordManager(context);
				chordManager.setTempDirectory(context.getFilesDir() + "/chordtemp");
				chordManager.setLooper(Looper.getMainLooper());
			}
			final Integer bestWiFiInterface = getBestWiFiInterface();
			Log.d("connecting with interface " + bestWiFiInterface);
			chordManager.start(bestWiFiInterface, new StatusListener() {

				@Override
				public void onStopped(int arg0) {
					Log.d("====== onStopped communication manager");
					lastWiFiInterface = null;
					// Verifico se è stato pianificato un riavvio
					if (shouldRestart) {
						try {
							Log.d("====== restart communication manager");
							startCommunication();
						} catch (CustomException e) {
							e.printStackTrace();
						}
					}
				}

				@Override
				public void onStarted(String arg0, int arg1) {
					Log.d("====== onStarted communication manager");
					lastWiFiInterface = bestWiFiInterface;
					socialChannel = joinSocialChannel();
					Log.d("now chord is joined to " + chordManager.getJoinedChannelList().size() + " channels");
				}
			});
		} catch (InvalidInterfaceException e) {
			e.printStackTrace();
		} catch (SsdkUnsupportedException e) {
			// Error Handling
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void stopCommunication(boolean shouldRestart) {
		this.shouldRestart = shouldRestart;
		Log.d();
		// Se chiudo i canali direttamente va in concurrent modification
		// exception per cui mi serve una struttura di appoggio da cui prendere
		// i nomi dei canali
		List<String> joinedChannelName = new ArrayList<String>();
		for (SchordChannel channel : chordManager.getJoinedChannelList()) {
			joinedChannelName.add(channel.getName());
		}
		// Da capire perchè a volte ritorna il messaggio:
		// "can't find channel (com.brainmote.lookatme.SOCIAL_CHANNEL)"
		for (String channelName : joinedChannelName) {
			Log.d("leaving channel " + channelName);
			chordManager.leaveChannel(channelName);
		}
		chordManager.stop();
	}

	private SchordChannel joinSocialChannel() {
		Log.d();
		return chordManager.joinChannel(AppSettings.MAIN_CHANNEL, new SchordChannel.StatusListener() {

			@Override
			public void onNodeLeft(String nodeId, String arg1) {
				Log.i("Il nodo che è appena scomparso: " + nodeId + " - " + arg1);
				communicationListener.onNodeLeft(nodeId);

			}

			@Override
			public void onNodeJoined(String nodeId, String arg1) {
				Log.i("Il nodo che è appena apparso: " + nodeId + " - " + arg1);
				communicationListener.onNodeJoined(nodeId);

			}

			@Override
			public void onMultiFilesWillReceive(String arg0, String arg1, String arg2, String arg3, int arg4, String arg5, long arg6) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onMultiFilesSent(String arg0, String arg1, String arg2, String arg3, int arg4, String arg5) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onMultiFilesReceived(String arg0, String arg1, String arg2, String arg3, int arg4, String arg5, long arg6, String arg7) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onMultiFilesFinished(String arg0, String arg1, String arg2, int arg3) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onMultiFilesFailed(String arg0, String arg1, String arg2, String arg3, int arg4, int arg5) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onMultiFilesChunkSent(String arg0, String arg1, String arg2, String arg3, int arg4, String arg5, long arg6, long arg7, long arg8) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onMultiFilesChunkReceived(String arg0, String arg1, String arg2, String arg3, int arg4, String arg5, long arg6, long arg7) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onFileWillReceive(String arg0, String arg1, String arg2, String arg3, String arg4, String arg5, long arg6) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onFileSent(String arg0, String arg1, String arg2, String arg3, String arg4, String arg5) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onFileReceived(String arg0, String arg1, String arg2, String arg3, String arg4, String arg5, long arg6, String arg7) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onFileFailed(String arg0, String arg1, String arg2, String arg3, String arg4, int arg5) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onFileChunkSent(String arg0, String arg1, String arg2, String arg3, String arg4, String arg5, long arg6, long arg7, long arg8) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onFileChunkReceived(String arg0, String arg1, String arg2, String arg3, String arg4, String arg5, long arg6, long arg7) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onDataReceived(String senderNodeId, String arg1, String arg2, byte[][] arg3) {
				Log.d("joinSocialChannel -" + arg1 + " - " + arg2);
				// here can be received profiles, previews, etc.
				MessageType messageType = MessageType.valueOf(arg2);
				byte[] chordMessageByte = arg3[0];
				Message message = null;
				if (chordMessageByte != null && chordMessageByte.length > 0) {
					message = Message.obtainChordMessage(chordMessageByte, senderNodeId);
				}
				switch (messageType) {
				case BASIC_PROFILE_REQUEST:
					Log.d("Qualcuno ha chiesto il mio profilo...");
					// Mando il mio profilo solamente se già completo
					if (Services.currentState.getMyBasicProfile() != null) {
						sendBasicProfileResponse(senderNodeId);
					}
					break;
				case BASIC_PROFILE:
					Log.d("Ho ricevuto un basic profile dal nodo " + senderNodeId);
					BasicProfile basicProfile = (BasicProfile) message.getObject(MessageType.BASIC_PROFILE.toString());
					Node basicNode = new Node();
					basicNode.setId(senderNodeId);
					basicNode.setProfile(basicProfile);
					communicationListener.onBasicProfileNodeReceived(basicNode);
					break;
				case FULL_PROFILE_REQUEST:
					// send my full profile to arg0 node
					sendFullProfileResponse(senderNodeId);
					communicationListener.onFullProfileRequestReceived(senderNodeId);
					break;
				case FULL_PROFILE:
					FullProfile fullProfile = (FullProfile) message.getObject(MessageType.FULL_PROFILE.toString());
					Node fullNode = new Node();
					fullNode.setId(senderNodeId);
					fullNode.setProfile(fullProfile);
					communicationListener.onFullProfileNodeReceived(fullNode);
					break;
				case PROFILE_UPDATE:
					Log.d("Ho ricevuto l'aggiornamento di un profilo");
					BasicProfile updatedProfile = (BasicProfile) message.getObject(MessageType.PROFILE_UPDATE.toString());
					Node updatedNode = new Node();
					updatedNode.setId(senderNodeId);
					updatedNode.setProfile(updatedProfile);
					communicationListener.onBasicProfileNodeReceived(updatedNode);
					break;
				case START_CHAT_MESSAGE:
					Log.d("Ricevuta richiesta di start chat");
					String myProfileId = Services.currentState.getMyBasicProfile().getId();
					if (Services.currentState.getSocialNodeMap().containsNode(senderNodeId)) {
						String profileId = Services.currentState.getSocialNodeMap().getProfileIdByNodeId(senderNodeId);
						String chatChannelName = Services.currentState.getConversationsStore().calculateConversationId(myProfileId, profileId);
						joinChatChannel(chatChannelName);
					} else {
						Log.d("PROFILO DI DESTINAZIONE NON PRESENTE IN TABELLA");
					}
					break;
				case LIKE:
					Log.d("Ho ricevuto un like");
					communicationListener.onLikeReceived(senderNodeId);
					break;
				default:
					break;
				}
			}

			@Override
			public void onUdpDataDelivered(String arg0, String arg1, String arg2) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onUdpDataReceived(String arg0, String arg1, String arg2, byte[][] arg3, String arg4) {
				// TODO Auto-generated method stub
				
			}
		});
	}

	private SchordChannel joinChatChannel(String channelName) {
		Log.d();
		return chordManager.joinChannel(channelName, new SchordChannel.StatusListener() {

			@Override
			public void onNodeLeft(String arg0, String arg1) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onNodeJoined(String arg0, String arg1) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onMultiFilesWillReceive(String arg0, String arg1, String arg2, String arg3, int arg4, String arg5, long arg6) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onMultiFilesSent(String arg0, String arg1, String arg2, String arg3, int arg4, String arg5) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onMultiFilesReceived(String arg0, String arg1, String arg2, String arg3, int arg4, String arg5, long arg6, String arg7) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onMultiFilesFinished(String arg0, String arg1, String arg2, int arg3) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onMultiFilesFailed(String arg0, String arg1, String arg2, String arg3, int arg4, int arg5) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onMultiFilesChunkSent(String arg0, String arg1, String arg2, String arg3, int arg4, String arg5, long arg6, long arg7, long arg8) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onMultiFilesChunkReceived(String arg0, String arg1, String arg2, String arg3, int arg4, String arg5, long arg6, long arg7) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onFileWillReceive(String arg0, String arg1, String arg2, String arg3, String arg4, String arg5, long arg6) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onFileSent(String arg0, String arg1, String arg2, String arg3, String arg4, String arg5) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onFileReceived(String arg0, String arg1, String arg2, String arg3, String arg4, String arg5, long arg6, String arg7) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onFileFailed(String arg0, String arg1, String arg2, String arg3, String arg4, int arg5) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onFileChunkSent(String arg0, String arg1, String arg2, String arg3, String arg4, String arg5, long arg6, long arg7, long arg8) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onFileChunkReceived(String arg0, String arg1, String arg2, String arg3, String arg4, String arg5, long arg6, long arg7) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onDataReceived(String fromNodeId, String arg1, String arg2, byte[][] arg3) {
				Log.d("joinChatChannel");
				// here can be received only chat messages
				MessageType messageType = MessageType.valueOf(arg2);
				switch (messageType) {
				case CHAT_MESSAGE:
					byte[] chordMessageByte = arg3[0];
					Message message = null;
					if (chordMessageByte != null && chordMessageByte.length > 0) {
						message = Message.obtainChordMessage(chordMessageByte, fromNodeId);
					}
					String chatMessage = (String) message.getObject(MessageType.CHAT_MESSAGE.toString());
					communicationListener.onChatMessageReceived(fromNodeId, chatMessage);
					break;
				default:
					break;
				}

			}

			@Override
			public void onUdpDataDelivered(String arg0, String arg1, String arg2) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onUdpDataReceived(String arg0, String arg1, String arg2, byte[][] arg3, String arg4) {
				// TODO Auto-generated method stub
				
			}
		});
	}

	@Override
	public void requestAllProfiles() {
		if (socialChannel != null)
			socialChannel.sendDataToAll(MessageType.BASIC_PROFILE_REQUEST.name(), EMPTY_PAYLOAD);
	}

	@Override
	public void notifyMyProfileIsUpdated() {
		Message message = obtainMyProfileMessage(Services.currentState.getMyBasicProfile(), MessageType.PROFILE_UPDATE, null);
		if (message == null || socialChannel == null)
			return;
		socialChannel.sendDataToAll(MessageType.PROFILE_UPDATE.toString(), obtainPayload(message));
	}

	private void sendBasicProfileResponse(String nodeTo) {
		Message message = obtainMyProfileMessage(Services.currentState.getMyBasicProfile(), MessageType.BASIC_PROFILE, nodeTo);
		if (message == null || socialChannel == null)
			return;
		socialChannel.sendData(nodeTo, MessageType.BASIC_PROFILE.toString(), obtainPayload(message));
	}

	private void sendFullProfileResponse(String nodeTo) {
		Message message = obtainMyProfileMessage(Services.currentState.getMyFullProfile(), MessageType.FULL_PROFILE, nodeTo);
		if (message == null || socialChannel == null)
			return;
		socialChannel.sendData(nodeTo, MessageType.FULL_PROFILE.toString(), obtainPayload(message));
	}

	private Message obtainMyProfileMessage(Profile myProfile, MessageType type, String receiverNodeName) {
		Message message = new Message(type);
		message.setSenderNodeName(chordManager.getName());
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
	public void requestFullProfile(String nodeTo) {
		socialChannel.sendData(nodeTo, MessageType.FULL_PROFILE_REQUEST.name(), EMPTY_PAYLOAD);
	}

	@Override
	public void sendLike(String nodeTo) {
		socialChannel.sendData(nodeTo, MessageType.LIKE.name(), EMPTY_PAYLOAD);
	}

	@Override
	public void startChat(String withNode) {
		String myProfileId = Services.currentState.getMyBasicProfile().getId();
		BasicProfile otherProfile = (BasicProfile) Services.currentState.getSocialNodeMap().findNodeByNodeId(withNode).getProfile();
		String otherProfileId = otherProfile.getId();
		String conversationId = Services.currentState.getConversationsStore().calculateConversationId(myProfileId, otherProfileId);
		ChatConversation conversation = Services.currentState.getConversationsStore().get(conversationId);
		// Se una conversazione con lo stesso id non è mai stata iniziata, la
		// crea
		if (conversation == null) {
			conversationId = Services.currentState.getConversationsStore().calculateConversationId(myProfileId, otherProfileId);
			Services.businessLogic.storeConversation(new ChatConversationImpl(conversationId, otherProfile));
		}
		// Effettua il join al canale prescelto per la chat
		SchordChannel chatChannel = chordManager.getJoinedChannel(conversationId);
		if (chatChannel == null) {
			Log.d("il device non era in join del chat channel " + conversationId + ", riprovo ora.");
			chatChannel = joinChatChannel(conversationId);
		}
		// Manda all'utente un messaggio affinchè si agganci automaticamente
		// al canale per chattare
		socialChannel.sendData(withNode, MessageType.START_CHAT_MESSAGE.toString(), EMPTY_PAYLOAD);
	}

	@Override
	public void sendChatMessage(ChatConversation conversation, String text) {
		// Verifico lo stato della conversazione corrente
		if (!checkAndJoinChatConversation(conversation)) {
			return;
		}
		SchordChannel privateChannel = chordManager.getJoinedChannel(conversation.getId());
		String toNodeId = getNodeIdFromConversation(conversation);
		// Preparo il messaggio chord da inviare
		Message chordMessage = new Message(MessageType.CHAT_MESSAGE);
		chordMessage.setSenderNodeName(chordManager.getName());
		chordMessage.setReceiverNodeName(toNodeId);
		chordMessage.putString(MessageType.CHAT_MESSAGE.toString(), text);
		privateChannel.sendData(toNodeId, MessageType.CHAT_MESSAGE.toString(), obtainPayload(chordMessage));
	}

	@Override
	public boolean checkAndJoinChatConversation(ChatConversation conversation) {
		boolean chatConversationReady = true;
		String toNodeId = getNodeIdFromConversation(conversation);
		// Verifico che l'utente con il quale voglio parlare esista
		if (isNodeAlive(toNodeId)) {
			// Verifico di essere connesso al canale privato della chat
			SchordChannel privateChannel = chordManager.getJoinedChannel(conversation.getId());
			if (privateChannel == null) {
				Log.w("Non sono in join nel canale privato " + conversation.getId() + ", rieseguo una richiesta di chat.");
				startChat(toNodeId);
				chatConversationReady = false;
			}
			// Se esiste verifico che sia correttamente in join sul canale
			if (!getActiveNodeListInChannel(conversation.getId()).contains(toNodeId)) {
				Log.w("Il nodo " + toNodeId + " con cui voglio parlare non è connesso al canale privato " + conversation.getId() + ", mando un invito al join");
				startChat(toNodeId);
				chatConversationReady = false;
			}
		}
		// Restituisce lo stato della conversazione
		return chatConversationReady;
	}

	@Override
	public String getNodeIdFromConversation(ChatConversation conversation) {
		return Services.currentState.getSocialNodeMap().getNodeIdByProfileId(Services.businessLogic.getProfileIdFromConversationId(conversation.getId()));
	}

	@Override
	public boolean isNodeAlive(String nodeId) {
		return getActiveNodeList().contains(nodeId);
	}

	@Override
	public List<String> getActiveNodeListInChannel(String channelId) {
		SchordChannel channel = chordManager.getJoinedChannel(channelId);
		if (channel == null)
			return new ArrayList<String>();
		return channel.getJoinedNodeList();
	}

	@Override
	public List<String> getActiveNodeList() {
		return getActiveNodeListInChannel(AppSettings.MAIN_CHANNEL);
	}

	/**
	 * Verifica che la connessione WiFi sia correttamente disponibile
	 * 
	 * @return
	 */
	private boolean isWiFiAviable() {
		// Verifica che la connessione WiFi sia correttamente disponibile
		Integer currentWiFiInterface = getCurrentWiFiInterface(context);
		// Verifica anche che la connessione sia coerente con quella
		// precentemente
		// impostata
		return currentWiFiInterface != null && currentWiFiInterface == lastWiFiInterface;
	}

	/**
	 * Recupera l'identificativo (in gergo Chord) della connessione WiFi da
	 * utilizzare
	 * 
	 * @return
	 * @throws CustomException
	 */
	private Integer getBestWiFiInterface() throws CustomException {
		// trying to use INTERFACE_TYPE_WIFI, otherwise get the first available
		// interface
		List<Integer> availableWifiInterface = chordManager.getAvailableInterfaceTypes();
		Integer wiFiInterface = null;
		if (availableWifiInterface == null || availableWifiInterface.size() == 0) {
			throw new CustomException("Nessuna interfaccia WiFi disponibile.");
		}
		if (availableWifiInterface.contains(SchordManager.INTERFACE_TYPE_WIFI)) {
			wiFiInterface = SchordManager.INTERFACE_TYPE_WIFI;
		} else {
			wiFiInterface = (availableWifiInterface.get(0)).intValue();
		}
		return wiFiInterface;
	}

	/**
	 * Recupera l'identificativo (in gergo Chord) della connessione WiFi
	 * attualmente utilizzata
	 * 
	 * @param context
	 * @return
	 */
	private Integer getCurrentWiFiInterface(Context context) {
		if (CommonUtils.isWifiClassicConnected(context))
			return SchordManager.INTERFACE_TYPE_WIFI;
		if (CommonUtils.isWifiApEnabled(context))
			return SchordManager.INTERFACE_TYPE_WIFI_AP;
		return null;
	}
}

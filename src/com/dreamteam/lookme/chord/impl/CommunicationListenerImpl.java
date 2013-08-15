package com.dreamteam.lookme.chord.impl;

import com.dreamteam.lookme.ChatConversation;
import com.dreamteam.lookme.bean.BasicProfile;
import com.dreamteam.lookme.bean.ChatConversationImpl;
import com.dreamteam.lookme.bean.ChatMessage;
import com.dreamteam.lookme.chord.CommunicationListener;
import com.dreamteam.lookme.chord.Node;
import com.dreamteam.lookme.service.Event;
import com.dreamteam.lookme.service.EventType;
import com.dreamteam.lookme.service.Services;
import com.dreamteam.util.CommonUtils;
import com.dreamteam.util.Log;

public class CommunicationListenerImpl implements CommunicationListener {

	@Override
	public void onCommunicationStarted() {
	}

	@Override
	public void onCommunicationStopped() {
	}

	@Override
	public void onBasicProfileNodeReceived(Node node) {
		Log.d();
		Services.currentState.putSocialNodeInMap(node);
		Services.event.post(new Event(EventType.NODE_JOINED, node.getId()));
	}

	@Override
	public void onFullProfileNodeReceived(Node node) {
		Log.d();
		Services.currentState.setProfileViewed(node);
		Services.event.post(new Event(EventType.PROFILE_RECEIVED, node.getId()));
	}

	@Override
	public void onNodeLeft(String nodeId) {
		Log.d();
		Services.currentState.removeSocialNodeFromMap(nodeId);
		Services.event.post(new Event(EventType.NODE_LEFT, nodeId));
	}

	@Override
	public void onProfileNodeUpdated(Node node) {
		Log.d();
	}

	@Override
	public void onLikeReceived(String fromNodeId) {
		Log.d();
		Services.currentState.addLikedToSet(fromNodeId);
		Services.event.post(new Event(EventType.LIKE_RECEIVED, fromNodeId));
		if (Services.currentState.checkLikeMatch(fromNodeId)) {
			Services.event.post(new Event(EventType.LIKE_MATCH, Services.currentState.getSocialNodeMap().get(fromNodeId).getProfile().getNickname()));
			Services.notification.perfectMatch(Services.currentState.getContext(), Services.currentState.getNickname(fromNodeId));
		}
		Services.notification.like(Services.currentState.getContext(), Services.currentState.getNickname(fromNodeId), fromNodeId);
	}

	@Override
	public void onChatMessageReceived(String fromNodeId, String message) {
		Log.d("node " + fromNodeId + " says: " + message);
		Node node = Services.currentState.getSocialNodeMap().get(fromNodeId);
		String nodeId = node.getId();
		BasicProfile otherProfile = (BasicProfile) node.getProfile();
		String otherNickName = otherProfile.getNickname();
		int otherAge = otherProfile.getAge();
		String otherProfileId = otherProfile.getId();
		BasicProfile myProfile = Services.currentState.getMyBasicProfile();
		String conversationId = CommonUtils.getConversationId(myProfile.getId(), otherProfileId);
		ChatConversation conversation = Services.currentState.getConversationsStore().get(conversationId);
		if (conversation == null || conversation.isEmpty())
			conversation = new ChatConversationImpl(conversationId, otherNickName, otherAge, nodeId, otherProfile.getMainProfileImage().getImageBitmap());
		ChatMessage chatMessage = new ChatMessage(otherNickName, myProfile.getNickname(), message, false);
		conversation.addMessage(chatMessage);
		Services.businessLogic.storeConversation(conversation);
		Services.event.post(new Event(EventType.CHAT_MESSAGE_RECEIVED, otherNickName));
		Services.notification.chatMessage(Services.currentState.getContext(), otherNickName, fromNodeId, message, conversationId);
	}
}

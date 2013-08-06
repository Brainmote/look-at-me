package com.dreamteam.lookme.chord.impl;

import java.util.ArrayList;
import java.util.List;

import com.dreamteam.lookme.bean.MessageItem;
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
		Log.d();
	}

	@Override
	public void onCommunicationStopped() {
		Log.d();
	}

	@Override
	public void onBasicProfileNodeReceived(Node node) {
		Log.d();
		Services.currentState.putSocialNodeInMap(node);
		try {
			Services.eventBus.post(new Event(EventType.NODE_JOINED, node.getId()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onFullProfileNodeReceived(Node node) {
		Log.d();
		Services.currentState.setProfileViewed(node);
		try {
			Services.eventBus.post(new Event(EventType.PROFILE_RECEIVED, node.getId()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onNodeLeft(String nodeName) {
		Log.d();
		Services.currentState.removeSocialNodeFromMap(nodeName);
		try {
			Services.eventBus.post(new Event(EventType.NODE_LEFT, nodeName));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onProfileNodeUpdated(Node node) {
		Log.d();
	}

	@Override
	public void onLikeReceived(String fromNode) {
		Log.d();
		Services.currentState.addLikedToSet(fromNode);
		try {
			Services.eventBus.post(new Event(EventType.LIKE_RECEIVED, fromNode));
			if (Services.currentState.checkLikeMatch(fromNode)) {
				Services.eventBus.post(new Event(EventType.LIKE_MATCH, fromNode));
				Services.notify.perfectMatch(Services.currentState.getContext(), Services.currentState.getNickname(fromNode));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		Services.notify.like(Services.currentState.getContext(), Services.currentState.getNickname(fromNode), fromNode);
	}

	@Override
	public void onStartChatMessageReceived(String nodeFrom, String channelName) {
		Log.d("Silently join to private channel");
		String nickName = Services.currentState.getSocialNodeMap().get(nodeFrom).getProfile().getNickname();
		String nodeId = Services.currentState.getSocialNodeMap().get(nodeFrom).getId();
		String deviceId = Services.currentState.getSocialNodeMap().get(nodeFrom).getProfile().getId();
		List<MessageItem> messagesList = Services.currentState.getMessagesHistoryMap().get(channelName);
		if (messagesList == null || messagesList.isEmpty())
			messagesList = new ArrayList<MessageItem>();
		// MessageItem messageItem = new MessageItem(nodeId,deviceId, "",
		// false);
		// messagesList.add(messageItem);
		Services.currentState.getMessagesHistoryMap().put(channelName, messagesList);
	}

	@Override
	public void onChatMessageReceived(String nodeFrom, String message) {
		Log.d("node " + nodeFrom + " says: " + message);
		try {
			Services.eventBus.post(new Event(EventType.CHAT_MESSAGE_RECEIVED, nodeFrom));
		} catch (Exception e) {
			e.printStackTrace();
		}
		String nickName = Services.currentState.getSocialNodeMap().get(nodeFrom).getProfile().getNickname();
		String nodeId = Services.currentState.getSocialNodeMap().get(nodeFrom).getId();
		String deviceId = Services.currentState.getSocialNodeMap().get(nodeFrom).getProfile().getId();
		String channelName = CommonUtils.generateChannelName(deviceId, Services.currentState.getMyBasicProfile().getId());
		List<MessageItem> messagesList = Services.currentState.getMessagesHistoryMap().get(channelName);
		if (messagesList == null || messagesList.isEmpty())
			messagesList = new ArrayList<MessageItem>();
		MessageItem messageItem = new MessageItem(nodeId, deviceId, message, false);
		messagesList.add(messageItem);
		Services.currentState.getMessagesHistoryMap().put(channelName, messagesList);
		Services.notify.chatMessage(Services.currentState.getContext(), nickName, message);
	}
}
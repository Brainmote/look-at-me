package com.dreamteam.lookme.bean;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.graphics.Bitmap;

import com.dreamteam.lookme.ChatConversation;

public class ChatConversationImpl implements ChatConversation {

	private String conversationId;
	private String otherNickname;
	private int otherAge;
	private String otherNodeId;
	private Bitmap otherImageBitamp;
	private Date lastMessageTimestamp;
	private List<ChatMessage> chatMessagesList;

	public ChatConversationImpl(String conversationId, String otherNickname, int otherAge, String otherNodeId, Bitmap otherImageBitamp) {
		this.conversationId = conversationId;
		this.otherNickname = otherNickname;
		this.otherAge = otherAge;
		this.otherNodeId = otherNodeId;
		this.otherImageBitamp = otherImageBitamp;
		this.chatMessagesList = new ArrayList<ChatMessage>();
		this.lastMessageTimestamp = new Date();
	}

	@Override
	public String getId() {
		return conversationId;
	}

	@Override
	public String getNickname() {
		return otherNickname;
	}

	@Override
	public Date getLastMessageTimestamp() {
		return lastMessageTimestamp;
	}

	@Override
	public Bitmap getImageBitmap() {
		return otherImageBitamp;
	}

	@Override
	public String getNodeId() {
		return otherNodeId;
	}

	@Override
	public ChatMessage getMessage(int position) {
		return chatMessagesList.get(position);
	}

	@Override
	public int size() {
		return chatMessagesList.size();
	}

	@Override
	public void addMessage(ChatMessage message) {
		chatMessagesList.add(message);
		lastMessageTimestamp = message.getTimestamp();
	}

	@Override
	public boolean isEmpty() {
		return chatMessagesList.isEmpty();
	}

	@Override
	public int getAge() {
		return otherAge;
	}

}
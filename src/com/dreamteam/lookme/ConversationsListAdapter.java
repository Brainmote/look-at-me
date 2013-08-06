package com.dreamteam.lookme;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dreamteam.lookme.bean.BasicProfile;
import com.dreamteam.lookme.bean.ChatMessage;
import com.dreamteam.lookme.bean.ChatConversation;
import com.dreamteam.lookme.chord.Node;
import com.dreamteam.lookme.service.Services;
import com.dreamteam.util.CommonUtils;
import com.dreamteam.util.ImageUtil;

public class ConversationsListAdapter extends BaseAdapter {

	private Activity activity;
	private List<String> channelList;

	public ConversationsListAdapter(Activity activity) {
		this.activity = activity;
		channelList = getListFromMessageMap();
		// Poich� sulla variabile fanno affidamento altri metodi, mi assicuro
		// che non sia null
		if (channelList == null)
			channelList = new ArrayList<String>();
	}

	@Override
	public int getCount() {
		return channelList.size();
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public ChatConversation getItem(int position) {
		Node node = CommonUtils.getNodeFromChannelName(channelList.get(position));
		List<ChatMessage> conversation = Services.currentState.getConversationsStore().get(channelList.get(position));
	
//		return conversation.get(conversation.size() -1);
//		ChatMessage fakeMessage = new ChatMessage(node.getId(), node.getProfile().getId(), "", false);
//		if (conversation != null && !conversation.isEmpty()) {
//			fakeMessage.setText(conversation.get(conversation.size() - 1).getText());
//		}
		return null;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			// LayoutInflater class is used to instantiate layout XML file
			// into its corresponding View objects.
			LayoutInflater layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = layoutInflater.inflate(R.layout.fragment_message_list_single_row, null);
		}
		//ChatMessage message = (ChatMessage) getItem(position);
		ChatMessage message = null;
		
		Node node = Services.currentState.getSocialNodeMap().get(message.getFrom());
		TextView nickNameText = (TextView) convertView.findViewById(R.id.nickNameText);
		nickNameText.setText("conversation to: " + node.getProfile().getNickname());
		// TextView lastMessageText = (TextView)
		// convertView.findViewById(R.id.lastMessageText);
		// lastMessageText.setText(message.getMessage());
		TextView lastMessageDate = (TextView) convertView.findViewById(R.id.lastMessageDate);
		String timeElapsed = CommonUtils.timeElapsed(message.getTimestamp(), new Date(System.currentTimeMillis()));
		lastMessageDate.setText(timeElapsed);
		// Imposto l'immagine del profilo
		ImageView photoImage = (ImageView) convertView.findViewById(R.id.profilePhotoImage);
		BasicProfile profile = (BasicProfile) node.getProfile();
		photoImage.setImageBitmap(ImageUtil.getBitmapProfileImage(activity.getResources(), profile));
		return convertView;
	}

	private List<String> getListFromMessageMap() {
		List<String> list = new ArrayList<String>();
		list.addAll(Services.currentState.getConversationsStore().keySet());
		return list;
	}
}

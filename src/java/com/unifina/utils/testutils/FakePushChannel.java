package com.unifina.utils.testutils;

import com.unifina.push.PushChannel;
import com.unifina.push.PushChannelMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FakePushChannel extends PushChannel {

	public Map<String, List<Object>> receivedContentByChannel = new HashMap<>();

	@Override
	protected void doPush(PushChannelMessage msg) {
		String c = msg.getChannel();
		if (!receivedContentByChannel.containsKey(c)) {
			receivedContentByChannel.put(c, new ArrayList<>());
		}
		receivedContentByChannel.get(c).add(msg.getContent());
	}
}

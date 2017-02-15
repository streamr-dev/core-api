package com.unifina.utils.testutils;

import com.unifina.push.PushChannel;
import com.unifina.push.PushChannelMessage;

import java.util.*;

public class FakePushChannel extends PushChannel {

	public Map<String, List<Object>> receivedContentByChannel = new HashMap<>();

	@Override
	protected void doPush(PushChannelMessage msg) {
		String c = msg.getChannel();
		if (!receivedContentByChannel.containsKey(c)) {
			receivedContentByChannel.put(c, new ArrayList<>());
		}
		receivedContentByChannel.get(c).add(handleContent(msg.getContent()));
	}

	private static Object handleContent(Object content) {
		return content instanceof Map ? new LinkedHashMap((Map) content) : content;
	}
}

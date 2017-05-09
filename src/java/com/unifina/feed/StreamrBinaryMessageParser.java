package com.unifina.feed;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.unifina.data.StreamrBinaryMessage;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class StreamrBinaryMessageParser implements MessageParser<StreamrBinaryMessage, StreamrMessage> {

	private static Type type = new TypeToken<LinkedHashMap<String, Object>>(){}.getType();

	private Gson gson = new GsonBuilder()
			.serializeNulls()
			.setDateFormat(DateFormat.LONG)
			.create();

	@Override
	public StreamrMessage parse(StreamrBinaryMessage raw) {
		if (raw.getContentType()==StreamrBinaryMessage.CONTENT_TYPE_JSON) {
			String s = raw.toString();
			LinkedHashMap<String, Object> json = gson.fromJson(s, type);
			return new StreamrMessage(raw.getStreamId(), raw.getPartition(), new Date(raw.getTimestamp()), new Date(), json);
		}
		else throw new RuntimeException("Unknown content type: "+raw.getContentType());
	}

}

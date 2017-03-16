package com.unifina.feed;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.unifina.data.StreamrBinaryMessage;
import com.unifina.feed.json.JSONStreamrMessage;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * This is the class that understands how the content is StreamrBinaryMessages is encoded.
 * It returns appropriate AbstractStreamrMessage subclass instances depending on what
 * content type is set on the StreamrBinaryMessage.
 */
public class StreamrBinaryMessageParser implements MessageParser<StreamrBinaryMessage, AbstractStreamrMessage> {

	private static Type mapType = new TypeToken<Map<String, Object>>(){}.getType();
	private static Type listType = new TypeToken<List<Map>>(){}.getType();

	private Gson gson = new GsonBuilder()
			.serializeNulls()
			.setDateFormat(DateFormat.LONG)
			.create();

	@Override
	public AbstractStreamrMessage parse(StreamrBinaryMessage raw) {
		if (raw.getContentType()==StreamrBinaryMessage.CONTENT_TYPE_JSON) {
			String s = raw.toString();
			if (s.charAt(0) == '{') {
				Map<String, Object> map = gson.fromJson(s, mapType);
				return new JSONStreamrMessage(raw.getStreamId(), raw.getPartition(), new Date(raw.getTimestamp()), new Date(), map);
			} else if (s.charAt(0) == '[') {
				List<Map> list = gson.fromJson(s, listType);
				return new JSONStreamrMessage(raw.getStreamId(), raw.getPartition(), new Date(raw.getTimestamp()), new Date(), list);
			} else {
				throw new RuntimeException("Unable to parse as JSON: "+s);
			}
		}
		else throw new RuntimeException("Unknown content type: "+raw.getContentType());
	}

}

package com.unifina.signalpath.streams;

import com.unifina.domain.data.Stream;
import com.unifina.service.FeedService;
import com.unifina.service.StreamService;
import com.unifina.signalpath.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchStream extends AbstractSignalPathModule {

	private final StringInput searchName = new StringInput(this, "name");
	private final StringOutput streamOutput = new StringOutput(this, "stream");
	private final MapOutput fields = new MapOutput(this, "fields");
	private final BooleanOutput found = new BooleanOutput(this, "found");

	private transient StreamService streamService;

	@Override
	public void sendOutput() {
		if (streamService == null) {
			streamService = getGlobals().getBean(StreamService.class);
		}

		Stream stream = streamService.findByName(searchName.getValue());
		if (stream == null) {
			found.send(false);
		} else {
			found.send(true);
			Map<String, Object> config = stream.getStreamConfigAsMap();
			if (config.containsKey("fields")) {
				fields.send(listOfFieldConfigsToMap((List<Map<String, String>>) config.get("fields")));
			}
			streamOutput.send(stream.getId());
		}
	}

	@Override
	public void clearState() {}

	private static Map<String, String> listOfFieldConfigsToMap(List<Map<String, String>> fieldConfigs) {
		Map<String, String> map = new HashMap<>();
		for (Map<String, String> fieldConfig: fieldConfigs) {
			map.put(fieldConfig.get("name"), fieldConfig.get("type"));
		}
		return map;
	}
}

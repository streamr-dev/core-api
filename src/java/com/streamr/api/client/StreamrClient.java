package com.streamr.api.client;

import java.util.List;
import java.util.Map;

public interface StreamrClient {
	CanvasesPerNode canvasesPerNode(String token, String nodeIp);
	List<Map<String, Object>> shutdown(String token, String nodeIp);
}

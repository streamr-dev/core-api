package com.streamr.api.client;

public interface StreamrClient {
	CanvasesPerNode canvasesPerNode(String token, String nodeIp);
}

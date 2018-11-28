package com.streamr.api.client;

import java.util.List;
import java.util.Map;

public class CanvasesPerNode {
	public List<Map<String, Object>> ok;
	public List<Map<String, Object>> shouldBeRunning;
	public List<Map<String, Object>> shouldNotBeRunning;

	public CanvasesPerNode() {}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("CanvasesPerNode[");
		sb.append("ok=").append(this.ok).append(',');
		sb.append("shouldBeRunning=").append(this.shouldBeRunning).append(',');
		sb.append("shouldNotBeRunning=").append(this.shouldNotBeRunning);
		sb.append(']');
		return sb.toString();
	}
}

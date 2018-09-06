package com.streamr.api.client;

import com.unifina.domain.signalpath.Canvas;

public class CanvasesPerNode {
	public Canvas[] ok;
	public Canvas[] shouldBeRunning;
	public Canvas[] shouldNotBeRunning;

	public CanvasesPerNode() {}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("CanvasesPerNode[");
		sb.append("ok=").append(this.ok.length).append(',');
		sb.append("shouldBeRunning=").append(this.shouldBeRunning.length).append(',');
		sb.append("shouldNotBeRunning=").append(this.shouldNotBeRunning.length);
		sb.append(']');
		return sb.toString();
	}
}

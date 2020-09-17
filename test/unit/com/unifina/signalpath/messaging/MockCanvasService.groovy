package com.unifina.signalpath.messaging

import com.unifina.domain.Canvas
import com.unifina.service.CanvasService

class MockCanvasService extends CanvasService {
	@Override
	String getCanvasURL(Canvas canvas) {
		return "https://www.streamr.com/canvas/editor/1"
	}
}

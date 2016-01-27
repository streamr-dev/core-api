package com.unifina.service

import com.unifina.domain.data.Stream
import com.unifina.domain.signalpath.Canvas
import groovy.json.JsonSlurper

class CanvasTestHelper {

	static def readCanvasJsonAndReplaceStreamId(Class clz, String fileName, Stream stream) {
		String txt = new File(clz.getResource(fileName).path).text
		txt = txt.replaceAll("STREAM_ID", stream.id.toString())
		return new JsonSlurper().parseText(txt)
	}

	static def modules(CanvasService canvasService, Canvas canvas) {
		canvasService.signalPathService.servletContext["signalPathRunners"][canvas.runner].signalPaths[0].mods
	}

	static def globals(CanvasService canvasService, Canvas canvas) {
		canvasService.signalPathService.servletContext["signalPathRunners"][canvas.runner].globals
	}
}

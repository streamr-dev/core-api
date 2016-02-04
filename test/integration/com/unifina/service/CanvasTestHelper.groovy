package com.unifina.service

import com.unifina.domain.data.Stream
import com.unifina.domain.signalpath.Canvas
import com.unifina.signalpath.SignalPath
import com.unifina.signalpath.simplemath.Sign
import groovy.json.JsonSlurper
import org.apache.log4j.Logger

class CanvasTestHelper {

	def final static log = Logger.getLogger(CanvasTestHelper)

	// A bit dirty, but we must do this because otherwise Canvas.executeUpdate() will be called in another thread
	// which causes exception related to transactions!
	static def hackServiceForTestFriendliness(SignalPathService signalPathService) {
		signalPathService.metaClass.saveState = { SignalPath sp ->
			Canvas liveCanvas = sp.canvas
			liveCanvas.serialized = signalPathService.serializationService.serialize(sp)
			liveCanvas.serializationTime = new Date()
		}

		signalPathService.servletContext = [:]

		log.warn("Hacked SignalPathService " + signalPathService + " for integration testing purposes...")
	}

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

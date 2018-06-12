package com.unifina.controller.api

import com.unifina.security.AllowRole
import com.unifina.security.StreamrApi
import com.unifina.service.SerializationService
import com.unifina.service.SignalPathService
import com.unifina.signalpath.SignalPath
import com.unifina.signalpath.map.ValueSortedMap
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

/*
    TODO: merge into NodeApiController when CORE-1421 is done
 */
@Secured(["IS_AUTHENTICATED_ANONYMOUSLY"])
class CanvasSizesApiController {
	SignalPathService signalPathService
	SerializationService serializationService

	@StreamrApi(allowRoles = AllowRole.ADMIN)
	def index() {
		def sizePerCanvas = new ValueSortedMap(true)
		signalPathService.runningSignalPaths.each { SignalPath sp ->
			long bytes = serializationService.serialize(sp).length
			sizePerCanvas[sp.canvas.id] = bytes
		}
		render(sizePerCanvas as JSON)
	}
}

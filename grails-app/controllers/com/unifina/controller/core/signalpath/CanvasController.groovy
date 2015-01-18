package com.unifina.controller.core.signalpath

import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.annotation.Secured

import org.atmosphere.cpr.BroadcasterFactory
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.springframework.util.FileCopyUtils

import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.SavedSignalPath
import com.unifina.service.SignalPathService
import com.unifina.signalpath.IReturnChannel
import com.unifina.signalpath.SignalPathRunner
import com.unifina.utils.Globals
import com.unifina.utils.GlobalsFactory

@Secured(["ROLE_USER"])
class CanvasController {

	static allowedMethods = [save: "POST", update: "POST", delete: "POST"]
	
	GrailsApplication grailsApplication
	SpringSecurityService springSecurityService
	SignalPathService signalPathService
	
	def index() {
		redirect(action: "build", params:params)
	}

	def build() {
		def beginDate = new Date()-1
		def endDate = new Date()-1
		
		def load = null
		
		if (params.load!=null) {
			load = createLink(controller:"savedSignalPath",action:"load",params:[id:params.load])
		}
		
		[beginDate:beginDate, endDate:endDate, load:load, examples:params.examples, user:SecUser.get(springSecurityService.currentUser.id)]
	}

	def abort() {
		String runnerId = params.runnerId
		String sessionId = params.sessionId
		
		Map r
		SignalPathRunner runner = servletContext["signalPathRunners"]?.get(runnerId)
		if (runner!=null && runner.isAlive()) {
			runner.abort()
			r = [success:true, runnerId:runnerId, status:"Aborting"]
		}
		else {
			IReturnChannel channel = servletContext["returnChannels"]?.get(sessionId)
			if (channel) {
				channel.destroy()
				r = [success:true, sessionId:sessionId, status:"Session cleaned"]
			}
			else r = [success:false, runnerId:runnerId, status:"Runner already stopped"]
		}
		
		render r as JSON
	}
	
	def run() {
		def iData
		if (params.signalPathData)
			iData = JSON.parse(params.signalPathData);
		else iData = JSON.parse(SavedSignalPath.get(Integer.parseInt(params.id)).json)

		// TODO: remove backwards compatibility
		def signalPathContext
		if (!params.signalPathContext) {
			signalPathContext = [beginFile:iData.beginFile, endFile:iData.endFile, timeOfDayFilter:iData.timeOfDayFilter]
		}
		else {
			signalPathContext = JSON.parse(params.signalPathContext)
		}
		
		signalPathContext.keepConsumedMessages = false
		
		// Create SignalPathRunner
		Globals globals = GlobalsFactory.createInstance(signalPathContext, grailsApplication)
		SignalPathRunner runner = new SignalPathRunner([iData], globals)
		String runnerId = runner.runnerId
		runner.start()
		
		Map result = [success:true, sessionId:runner.getReturnChannel(0).sessionId, runnerId:runnerId]
		render result as JSON
	}
	
	def reconstruct() {
		Map signalPathContext = (params.signalPathContext ? JSON.parse(params.signalPathContext) : [:])
		Globals globals = GlobalsFactory.createInstance(signalPathContext, grailsApplication)
		Map result = signalPathService.reconstruct(JSON.parse(params.signalPathData), globals)
		render result as JSON
	}
	
	def existsCsv() {
		String fileName = System.getProperty("java.io.tmpdir") + File.separator + params.filename
		File file = new File(fileName)
		Map result = (file.canRead() ? [success:true, filename:params.filename] : [success:false])
		render result as JSON
	}
	
	def downloadCsv() {
		String fileName = System.getProperty("java.io.tmpdir") + File.separator + params.filename
		File file = new File(fileName)
		if (file.canRead()) {
			FileInputStream fileInputStream = new FileInputStream(file)
			response.setContentType("text/csv")
			response.setHeader("Content-disposition", "attachment; filename="+file.name);
			FileCopyUtils.copy(fileInputStream, response.outputStream)
			fileInputStream.close()
			file.delete()
		}
		else throw new FileNotFoundException("File not found: "+params.filename)
	}
	
	@Secured(["ROLE_ADMIN"])
	def debug() {
		return [runners: servletContext["signalPathRunners"], returnChannels: servletContext["returnChannels"], broadcasters: BroadcasterFactory.getDefault().lookupAll()]
	}
	
}

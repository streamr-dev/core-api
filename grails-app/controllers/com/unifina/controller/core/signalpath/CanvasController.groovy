package com.unifina.controller.core.signalpath

import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.annotation.Secured

import java.security.AccessControlException

import org.atmosphere.cpr.BroadcasterFactory
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.springframework.util.FileCopyUtils

import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.RunningSignalPath
import com.unifina.domain.signalpath.SavedSignalPath
import com.unifina.service.SignalPathService
import com.unifina.service.UnifinaSecurityService
import com.unifina.signalpath.SignalPathRunner
import com.unifina.utils.Globals
import com.unifina.utils.GlobalsFactory

@Secured(["ROLE_USER"])
class CanvasController {

	static allowedMethods = [save: "POST", update: "POST", delete: "POST"]
	
	GrailsApplication grailsApplication
	SpringSecurityService springSecurityService
	SignalPathService signalPathService
	UnifinaSecurityService unifinaSecurityService
	
	def index() {
		redirect(action: "build", params:params)
	}

	def build() {
		def beginDate = new Date()-1
		def endDate = new Date()-1
		
		def load = null
		
		if (params.load!=null) {
			load = createLink(controller:"savedSignalPathApi",action:"load",params:[id:params.load])
		}
		
		[beginDate:beginDate, endDate:endDate, load:load, examples:params.examples, user:SecUser.get(springSecurityService.currentUser.id)]
	}
	
	def reconstruct() {
		Map json = [signalPathContext: (params.signalPathContext ? JSON.parse(params.signalPathContext) : [:]), signalPathData: JSON.parse(params.signalPathData)]
		Globals globals = GlobalsFactory.createInstance(json.signalPathContext, grailsApplication)
		Map result = signalPathService.reconstruct(json, globals)
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

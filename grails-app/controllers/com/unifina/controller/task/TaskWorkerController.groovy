package com.unifina.controller.task

import grails.plugin.springsecurity.annotation.Secured

import com.unifina.domain.security.SecUser
import com.unifina.task.TaskWorker

@Secured(["ROLE_ADMIN"])
class TaskWorkerController {
	
	static defaultAction = "status"
	
	def status() {
		def workers = servletContext["taskWorkers"]
		def users = SecUser.list()
		[workers:workers,users:users]
	}
	
	def quitWorker() {
		def workers = servletContext["taskWorkers"]
		workers.each {
			if (it.workerId==Integer.parseInt(params.id))
				it.quit()
		}
		redirect(action: "status")
	}
	
	def startWorker() {
		def workers = servletContext["taskWorkers"]
		
		SecUser user = params.user ? SecUser.get(params.user) : null
		
		TaskWorker worker = new TaskWorker(grailsApplication,workers.size()+1,user)
		worker.start()
		workers.add(worker)
		
		redirect(action: "status")
	}
	
}

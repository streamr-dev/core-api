package com.unifina.controller.task

import grails.plugins.springsecurity.Secured

import com.unifina.domain.security.SecUser
import com.unifina.task.TaskWorker

class TaskWorkerController {
	
	static defaultAction = "status"
	
	@Secured(['ROLE_ADMIN'])
	def status() {
		def workers = servletContext["taskWorkers"]
		def users = SecUser.list()
		[workers:workers,users:users]
	}
	
	@Secured(['ROLE_ADMIN'])
	def quitWorker() {
		def workers = servletContext["taskWorkers"]
		workers.each {
			if (it.workerId==Integer.parseInt(params.id))
				it.quit()
		}
		redirect(action: "status")
	}
	
	@Secured(['ROLE_ADMIN'])
	def startWorker() {
		def workers = servletContext["taskWorkers"]
		
		SecUser user = params.user ? SecUser.get(params.user) : null
		
		TaskWorker worker = new TaskWorker(grailsApplication,workers.size()+1,user)
		worker.start()
		workers.add(worker)
		
		redirect(action: "status")
	}
	
}

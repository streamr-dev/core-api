package com.unifina.controller.task

import grails.plugin.springsecurity.annotation.Secured

import com.unifina.domain.security.SecUser

@Secured(["ROLE_ADMIN"])
class TaskWorkerController {
	
	static defaultAction = "status"

	def taskService

	def status() {
		def workers = taskService.getTaskWorkers()
		def users = SecUser.list()
		[workers:workers,users:users]
	}
	
	def quitWorker() {
		taskService.stopTaskWorker(params.int("id"))
		redirect(action: "status")
	}
	
	def startWorker() {
		SecUser user = params.user ? SecUser.get(params.user) : null
		taskService.startTaskWorker(user)
		redirect(action: "status")
	}
	
}

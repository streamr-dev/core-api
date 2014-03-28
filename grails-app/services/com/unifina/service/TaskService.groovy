package com.unifina.service

import grails.converters.JSON

import org.apache.log4j.Logger
import org.codehaus.groovy.grails.commons.GrailsApplication

import com.unifina.domain.task.Task
import com.unifina.task.AbstractTask

class TaskService {

	private static final Logger log = Logger.getLogger(TaskService)
	
	GrailsApplication grailsApplication

	String createTaskGroupId() {
		return UUID.randomUUID().toString()
	}
	
    AbstractTask getTaskInstance(Task task) {
		ClassLoader cl = this.getClass().getClassLoader()
		Map config = JSON.parse(task.config)
		AbstractTask t = (AbstractTask) cl.loadClass(task.implementingClass).newInstance(task, config, grailsApplication)
		return t
    }
	
	void setComplete(Task task) {
		Task.withTransaction() {
			task = task.attach()
			task.refresh()
			task.complete = true
			task.save(flush:true)
			log.info("Task $task.id complete.")
		}
	}
	
	void setStatus(Task task, AbstractTask taskImpl) {
		Task.withTransaction() {
			task = task.attach()
			task.refresh()
			task.status = taskImpl.toString()
			task.save(flush:true)
		}
	}
	
	void setError(Task task, Throwable throwable) {
		Task.withTransaction() {
			task = task.attach()
			task.refresh()
			task.error = throwable.toString()
			task.save(flush:true)
		}
	}
}

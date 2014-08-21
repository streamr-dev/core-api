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
	
	/**
	 * Deletes the Tasks from database if the whole group is marked complete.
	 * @param task
	 * @return
	 */
	boolean deleteGroupIfComplete(String taskGroupId) {
		int unitCount = Task.countByTaskGroupId(taskGroupId)
		int readyCount = Task.countByTaskGroupIdAndComplete(taskGroupId,true)
		
		if (unitCount==0 || readyCount < unitCount)
			return false
		else {
			log.info("Task group $taskGroupId is complete!")

			// Clean completed Tasks from the queue
			Task.executeUpdate("delete Task t where t.taskGroupId = ?",[taskGroupId])
			return true
		}
	}
	
	void setComplete(Task task) {
//		Task.withTransaction() {
			task = task.attach()
			task.refresh()
			task.complete = true
			task.progress = 100
			task.save(flush:true)
			log.info("Task $task.id complete.")
//		}
	}
	
	void skipTask(Task task) {
		task = task.attach()
		if (!task.complete) {
			task.skip = true
			task.available = true
			task.save(flush:true, failOnError:true)
		}
	}
	
	void setStatus(Task task, AbstractTask taskImpl) {
//		Task.withTransaction() {
			task = task.attach()
			task.refresh()
			task.status = taskImpl.toString()
			task.save(flush:true)
//		}
	}
	
	void setProgress(Task task, int progress) {
		Task.executeUpdate("update Task t set t.progress = ? where t.id = ?", [progress, task.id])
	}
	
	void setError(Task task, Throwable throwable) {
//		Task.withTransaction() {
			task = task.attach()
			task.refresh()
			task.error = throwable.toString()
			task.save(flush:true)
//		}
	}
	
	/**
	 * Gets the number of available tasks before the given task
	 * @param taskGroupId
	 * @return
	 */
	Integer getQueuePosition(Task task) {
		List result = Task.executeQuery("select count(t.id) from Task t where t.id < ?)", [task.id])
		return result[0] ?: 0
	}
	
	/**
	 * Gets the number of available tasks before the first task of the given taskGroupId
	 * @param taskGroupId
	 * @return
	 */
	Integer getGroupQueuePosition(String taskGroupId) {
		List result = Task.executeQuery("select count(t.id) from Task t where available = true and t.id < (select min(me.id) from Task me where me.taskGroupId = ?)", [taskGroupId])
		return result[0] ?: 0
	}
	
	/**
	 * Returns the task group progress as integer percentage between 0 and 100
	 * @param taskGroupId
	 * @return
	 */
	Integer getTaskGroupProgress(String taskGroupId) {
		List fields = Task.withCriteria(uniqueResult:true) {
			projections {
				sum("progress")
				rowCount()
			}
			eq("taskGroupId",taskGroupId)
		}
		
		double progress = fields[0]
		double maxProgress = 100D*fields[1]
		
		return (int) 100*(progress/maxProgress)
		
	}
}

package com.unifina.service

import grails.converters.JSON

import org.apache.log4j.Logger
import org.codehaus.groovy.grails.commons.GrailsApplication

import com.unifina.domain.task.Task
import com.unifina.task.AbstractTask

class TaskService {

	private static final Logger log = Logger.getLogger(TaskService)
	
	GrailsApplication grailsApplication
	def kafkaService

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
	
	void skipTask(Task task, boolean available=true) {
		task = task.attach()
		if (!task.complete) {
			task.skip = true
			task.available = available
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
	 * @param taskGroupIds
	 * @return
	 */
	Integer getTaskGroupProgress(List<String> taskGroupIds) {
		List rows = Task.withCriteria() {
			projections {
				groupProperty("taskGroupId")
				sum("progress")
				rowCount()
			}
			'in'("taskGroupId",taskGroupIds)
		}
		
		if (rows.size()==0)
			return 100
		
		// How many of the given task groups had no tasks left?
		Map resultsByTaskGroup = [:]
		
		rows.each {resultsByTaskGroup.put(it[0], it)}

		double progressSum = taskGroupIds.sum {
			def row = resultsByTaskGroup[it]
			if (row==null)
				return 1D
			else return row[1]/(100D*row[2]) // sum(progress) / rowCount
		}
		double maxProgress = taskGroupIds.size()
		
		return 100D * progressSum / maxProgress
		
	}
	
	void abortTask(Task task) {
		skipTask(task,false)
		kafkaService.sendMessage("unifina-tasks", task.id, [type:"abort",id:task.id])
	}
	
	/**
	 * Deletes all available Tasks in a task group and signals the unavailable
	 * but incomplete Tasks to abort. Returns a list of the latter.
	 * If the list is empty, then all Tasks in this group are either complete or 
	 * have been deleted.
	 * @param taskGroupId
	 */
	List<Task> abortTaskGroup(String taskGroupId) {
		// Delete all remaining available tasks
		Task.executeUpdate("delete Task t where t.taskGroupId = ? and available = true",[taskGroupId])
		// Find and abort the rest
		List<Task> tasks = Task.findAllByTaskGroupIdAndComplete(taskGroupId,false)
		tasks.each { abortTask(it) }
		return tasks
	}
}

package com.unifina.task

import grails.util.GrailsUtil

import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement

import org.apache.log4j.Logger
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.hibernate.jdbc.Work

import com.unifina.domain.security.SecUser
import com.unifina.domain.task.Task
import com.unifina.service.TaskService
import com.unifina.utils.NetworkInterfaceUtils


class TaskWorker extends Thread {
	
	GrailsApplication grailsApplication
	TaskService taskService
	SessionFactory sessionFactory

	boolean quit = false

	int stateCode = 0
	long lastKnownTaskId
	String lastKnownStatus
	Throwable lastError
	int workerId
	
	SecUser priorityUser
	
	Task currentTask
	AbstractTask currentTaskImpl

	public static final Logger log = Logger.getLogger(TaskWorker.class)
	
	public TaskWorker(GrailsApplication grailsApplication, int id, SecUser priorityUser=null) {
		super("TaskWorker-"+id)
		
		this.grailsApplication = grailsApplication
		taskService = (TaskService) grailsApplication.mainContext.getBean("taskService")
		sessionFactory = (SessionFactory) grailsApplication.mainContext.getBean("sessionFactory")
		
		this.workerId = id	
		this.priorityUser = priorityUser
	}


	private Task getTask(SecUser priorityUser=null) {
		boolean retry = true
		Task task = null
		
		try {
			
			// First transaction: try to find a Task for the priorityUser
			
			
			def id = null
			if (priorityUser) {
				Task.withTransaction {
					Session session = sessionFactory.getCurrentSession()
					session.doWork(new Work() {
						@Override
						public void execute(Connection connection) throws SQLException {
							Statement stmt = connection.createStatement()
							stmt.executeUpdate("SET @update_id := 0")
							stmt.executeUpdate("UPDATE task SET available=false, id = (SELECT @update_id := id), last_updated = current_timestamp WHERE user_id = $priorityUser.id AND available = true AND (run_after is null OR current_timestamp > run_after) LIMIT 1")
							ResultSet rs = stmt.executeQuery("SELECT @update_id as uid")
							rs.next()
							id = rs.getLong("uid")	
							rs.close()					
						}
					});		
				}
			}
			
			// Second transaction: If no priority units found, accept any
			if (!id) {
				Task.withTransaction {
					Session session = sessionFactory.getCurrentSession()
					session.doWork(new Work() {
						@Override
						public void execute(Connection connection) throws SQLException {
							Statement stmt = connection.createStatement()
							stmt.executeUpdate("SET @update_id := 0")
							stmt.executeUpdate("UPDATE task SET available=false, id = (SELECT @update_id := id), last_updated = current_timestamp WHERE available = true AND (run_after is null OR current_timestamp > run_after) LIMIT 1")
							ResultSet rs = stmt.executeQuery("SELECT @update_id as uid")
							rs.next()
							id = rs.getLong("uid")	
							rs.close()					
						}
					});		
				}
			}
			
			if (id) {
				Task.withTransaction {
					task = Task.get(id)
					
					// Unit has already been marked not available
					def myIp = NetworkInterfaceUtils.getIPAddress()
					task.serverIp = myIp?.toString()
					task.save(flush:true)
				}
			}
		
		} catch (Exception e) {
			println "Warning: couldn't acquire Task, error is: $e"
			e = GrailsUtil.deepSanitize(e)
			
			println e.toString()
			println e.cause
			e.printStackTrace(System.out)
			return null
		}

		return task
	}
	
	/**
	 * Aborts currently running task
	 */
	public void abort() {
		log.info("Calling abort on task implementation: $currentTaskImpl")
		currentTaskImpl?.abort();
	}
	
	public int getWorkerId() {
		return workerId;
	}
	
	void run() {
		int i = -1
		while(!quit) {
			i++

			Throwable error
			boolean taskGroupComplete = false
			
			try {
				// Get a unit of work
				currentTask = getTask(priorityUser)
	
				if (currentTask) {
					log.info("Found task $currentTask.id")
	
					stateCode = 1
						
					lastKnownTaskId = currentTask.id
					log.info("Running task $currentTask.id...")
					
					// On successful completion of unit, mark the unit as completed
					currentTaskImpl = taskService.getTaskInstance(currentTask)
					taskService.setStatus(currentTask, currentTaskImpl)
					lastKnownStatus = currentTask.status
					if (currentTask.skip || currentTaskImpl.run()) {
						taskService.setComplete(currentTask)
						taskGroupComplete = taskService.deleteGroupIfComplete(currentTask.taskGroupId)
						currentTaskImpl.onComplete(taskGroupComplete)
					}
				}
			} catch (Exception e) {
				log.error("Exception in TaskWorker! Tried to run task $currentTask",e)
				error = e
				lastError = e
			}
			
			// Try to report error
			if (error && !taskGroupComplete) try {
				taskService.setError(currentTask, error)
			} catch (Exception e) {
				log.error("Failed to write error to Task!", e)
			}

			synchronized(this) {
				if (!currentTask && !quit) {
					stateCode = 0
					if (i % 60 == 0) {
						log.info("No tasks available.")
					}
					wait(10000)
				}
			}
		}
		
		stateCode = -1
		log.info("Task worker thread $workerId stopped.")
	}

	public void quit() {
		synchronized(this) {
			quit = true
			this.notify()
		}
	}

}

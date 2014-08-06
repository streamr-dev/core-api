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
//	BacktestService backtestService
	TaskService taskService
	SessionFactory sessionFactory

	boolean quit = false

	int stateCode = 0
	long lastKnownTaskId
	String lastKnownStatus
	Throwable lastError
	int workerId
	
	SecUser priorityUser

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
							stmt.executeUpdate("UPDATE task SET available=false, id = (SELECT @update_id := id), last_updated = current_timestamp WHERE user_id = $priorityUser.id AND available = true LIMIT 1")
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
							stmt.executeUpdate("UPDATE task SET available=false, id = (SELECT @update_id := id), last_updated = current_timestamp WHERE available = true LIMIT 1")
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
	
	public int getWorkerId() {
		return workerId;
	}
	
	void run() {
		int i = -1
		while(!quit) {
			i++
			
			Task task
			Throwable error
			try {
				// Get a unit of work
				task = getTask(priorityUser)
	
				if (task) {
					log.info("Found task $task.id")
	
					stateCode = 1
						
					lastKnownTaskId = task.id
					log.info("Running task $task.id...")
					
					// On successful completion of unit, mark the unit as completed
					AbstractTask impl = taskService.getTaskInstance(task)
					taskService.setStatus(task, impl)
					lastKnownStatus = task.status
					if (task.skip || impl.run()) {
						taskService.setComplete(task)
						boolean groupComplete = taskService.deleteGroupIfComplete(task.taskGroupId)
						impl.onComplete(groupComplete)
					}
				}
			} catch (Exception e) {
				log.error("Exception in TaskWorker! Tried to run task $task",e)
				error = e
				lastError = e
			}
			
			// Try to report error
			if (error) try {
				taskService.setError(task, error)
			} catch (Exception e) {
				log.error("Failed to write error to Task!", e)
			}
			
			if (!task) {
				stateCode = 0
				if (i%60==0)
					log.info("No tasks available.")

				Thread.sleep(10*1000)
			}
		}
		
		stateCode = -1
	}

	public void quit() {
		quit = true
	}

}

package com.unifina.service

import com.unifina.domain.security.SecUser
import com.unifina.task.TaskWorker
import grails.test.mixin.*
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.junit.*

import com.unifina.domain.task.Task
import spock.lang.Specification

import static plastic.criteria.PlasticCriteria.* ; // mockCriteria() method

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(TaskService)
@Mock([Task])
class TaskServiceSpec extends Specification {

	void testDeleteGroupIfComplete() {
		// Must mock executeUpdate(String,List) because HQL is not supported in unit test GORM
		def unitMock = mockFor(Task)
		unitMock.demand.static.executeUpdate(1) {String s, List p->
			Task.findAllByTaskGroupId(p[0]).each {it.delete()}
		}
		
		String taskGroupId = "test"
		String configString = "{}"
		Task unit1 = new Task("DummyClass", configString, "test", taskGroupId)
		unit1.save(flush:true, failOnError:true)
		
		Task unit2 = new Task("DummyClass", configString, "test", taskGroupId)
		unit2.save(flush:true, failOnError:true)

		expect:
		Task.count() == 2
		Task.countByTaskGroupId(taskGroupId) == 2
		Task.countByTaskGroupIdAndComplete(taskGroupId,true) == 0
		
		// Not ready yet
		!service.deleteGroupIfComplete(unit1.taskGroupId)

		when:
		service.setComplete(unit1)

		then:
		Task.count() == 2
		Task.countByTaskGroupId(taskGroupId) == 2
		Task.countByTaskGroupIdAndComplete(taskGroupId,true) == 1
		// Not ready yet
		!service.deleteGroupIfComplete(unit1.taskGroupId)

		when:
		service.setComplete(unit2)

		then:
		Task.count() == 2
		Task.countByTaskGroupId(taskGroupId) == 2
		Task.countByTaskGroupIdAndComplete(taskGroupId,true) == 2
		// Now it's complete
		service.deleteGroupIfComplete(unit1.taskGroupId)
		// Check that the Tasks were deleted
		Task.count() == 0
		
	}
	
	
	void testSkipTask() {
		// Must mock executeUpdate(String,List) because HQL is not supported in unit test GORM
		def unitMock = mockFor(Task)
		unitMock.demand.static.executeUpdate(1) {String s, List p->
			Task.findAllByTaskGroupId(p[0]).each {it.delete()}
		}
		
		String taskGroupId = "test"
		String configString = "{}"
		Task unit1 = new Task("DummyClass", configString, "test", taskGroupId)
		unit1.save(flush:true, failOnError:true)

		expect:
		Task.countByAvailable(true) == 1

		when:
		unit1.available = false
		unit1.save(flush:true, failOnError:true)

		then:
		Task.countByAvailable(true) == 0

		when:
		service.skipTask(unit1)

		then:
		Task.countByAvailable(true) == 1

		when:
		unit1.available = false
		unit1.save(flush:true, failOnError:true)
		service.setComplete(unit1)

		then:
		service.deleteGroupIfComplete(unit1.taskGroupId)
		Task.count() == 0
	}
	
	void testGetTaskGroupProgress() { 
		mockCriteria([Task])
		
		String taskGroupId = "test"
		String configString = "{}"
		
		Task unit1 = new Task("DummyClass", configString, "test", taskGroupId)
		unit1.progress = 50
		unit1.save(flush:true, failOnError:true)
		
		Task unit2 = new Task("DummyClass", configString, "test", taskGroupId)
		unit2.save(flush:true, failOnError:true)

		expect:
		service.getTaskGroupProgress([taskGroupId]) == 25

		when:
		service.setComplete(unit1)

		then:
		service.getTaskGroupProgress([taskGroupId]) == 50

		when:
		service.setComplete(unit2)

		then:
		service.getTaskGroupProgress([taskGroupId]) == 100
	}
	
	void testGetTaskGroupProgress2() {
		mockCriteria([Task])
		
		String taskGroupId = "test"
		String taskGroupId2 = "test2"
		String configString = "{}"
		
		Task g1u1 = new Task("DummyClass", configString, "test", taskGroupId)
		g1u1.progress = 100
		g1u1.save(flush:true, failOnError:true)
		
		Task g1u2 = new Task("DummyClass", configString, "test", taskGroupId)
		g1u2.save(flush:true, failOnError:true)
		
		Task g2u1 = new Task("DummyClass", configString, "test", taskGroupId2)
		g2u1.progress = 50
		g2u1.save(flush:true, failOnError:true)

		expect:
		service.getTaskGroupProgress([taskGroupId, taskGroupId2]) == 50

		when: "Task group is deleted"
		g1u1.delete()
		g1u2.delete()

		then: "Progress must be reported correctly"
		service.getTaskGroupProgress([taskGroupId, taskGroupId2]) == 75
	}

	void "startTaskWorker() must create a TaskWorker, start it, and add it to the list"() {
		def taskWorkerMock = Mock(TaskWorker)
		TaskService service = new TaskService() {
			@Override
			TaskWorker createTaskWorker(GrailsApplication grailsApplication, int id, SecUser priorityUser) {
				return taskWorkerMock
			}
		}

		when:
		TaskWorker tw = service.startTaskWorker()

		then:
		tw != null
		service.getTaskWorkers().size() == 1
		1 * taskWorkerMock.start()
	}

	void "stopTaskWorker(id) must stop the TaskWorker with the given id"() {
		def taskWorkerMock = Mock(TaskWorker)
		TaskService service = new TaskService() {
			@Override
			TaskWorker createTaskWorker(GrailsApplication grailsApplication, int id, SecUser priorityUser) {
				return taskWorkerMock
			}
		}
		TaskWorker tw = service.startTaskWorker()

		when:
		service.stopTaskWorker(tw.getWorkerId())

		then:
		service.getTaskWorkers().size() == 1
		1 * taskWorkerMock.quit()
	}

}

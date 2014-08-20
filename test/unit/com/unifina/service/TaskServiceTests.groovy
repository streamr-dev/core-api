package com.unifina.service



import grails.test.mixin.*

import org.junit.*

import com.unifina.domain.task.Task
import static plastic.criteria.PlasticCriteria.* ; // mockCriteria() method

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(TaskService)
@Mock([Task])
class TaskServiceTests {
	
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
		
		assert Task.count() == 2
		assert Task.countByTaskGroupId(taskGroupId) == 2
		assert Task.countByTaskGroupIdAndComplete(taskGroupId,true) == 0
		
		// Not ready yet
		assert !service.deleteGroupIfComplete(unit1.taskGroupId)

		service.setComplete(unit1)
		assert Task.count() == 2
		assert Task.countByTaskGroupId(taskGroupId) == 2
		assert Task.countByTaskGroupIdAndComplete(taskGroupId,true) == 1
		
		// Not ready yet
		assert !service.deleteGroupIfComplete(unit1.taskGroupId)
		
		service.setComplete(unit2)
		assert Task.count() == 2
		assert Task.countByTaskGroupId(taskGroupId) == 2
		assert Task.countByTaskGroupIdAndComplete(taskGroupId,true) == 2

		// Now it's complete
		assert service.deleteGroupIfComplete(unit1.taskGroupId)
		// Check that the Tasks were deleted
		assert Task.count() == 0
		
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

		assert Task.countByAvailable(true) == 1
		
		unit1.available = false
		unit1.save(flush:true, failOnError:true)
		
		assert Task.countByAvailable(true) == 0
		
		service.skipTask(unit1)
		assert Task.countByAvailable(true) == 1
		
		unit1.available = false
		unit1.save(flush:true, failOnError:true)
		service.setComplete(unit1)
		assert service.deleteGroupIfComplete(unit1.taskGroupId)
		assert Task.count() == 0
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
		
		assert service.getTaskGroupProgress(taskGroupId) == 25
		
		service.setComplete(unit1)
		assert service.getTaskGroupProgress(taskGroupId) == 50
		service.setComplete(unit2)
		assert service.getTaskGroupProgress(taskGroupId) == 100
	}
}

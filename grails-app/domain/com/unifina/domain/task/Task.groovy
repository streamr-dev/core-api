package com.unifina.domain.task

import com.unifina.domain.security.SecUser

class Task {
	Long id
	
	boolean available
	boolean complete
		
	String serverIp
	SecUser user
	
	Date dateCreated
	Date lastUpdated
	
	int complexity
	
	String category
	String config
	
	String status
	String error
	
	String implementingClass
	String taskGroupId
	
	public Task(String implementingClass, String config, String category, String taskGroupId, int complexity = 0, SecUser user = null) {
		this.implementingClass = implementingClass
		this.config = config
		this.category = category
		this.taskGroupId = taskGroupId
		this.complexity = complexity
		this.user = user
		
		available = true
		complete = false
	}
	
	static mapping = {
		available index:'available_idx'
		taskGroupId index:'task_group_id_idx'
	}
	
	static constraints = {
		serverIp(nullable:true)
		user(nullable:true)
		status(nullable:true)
		error(nullable:true, maxSize: 1000)
	}
	
}

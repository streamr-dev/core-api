package com.unifina.domain.task

import com.unifina.domain.security.SecUser

class Task {
	Long id
	
	boolean available = true
	boolean complete = false
	
	Boolean skip
		
	String serverIp
	SecUser user
	
	Date dateCreated
	Date lastUpdated
	
	int complexity = 0
	int progress = 0
	
	String category
	String config
	
	String status
	String error
	
	String implementingClass
	String taskGroupId
	
	Date runAfter
	
	public Task(String implementingClass, String config, String category, String taskGroupId, int complexity = 0, SecUser user = null, Date runAfter = null) {
		this.implementingClass = implementingClass
		this.config = config
		this.category = category
		this.taskGroupId = taskGroupId
		this.complexity = complexity
		this.user = user
		this.runAfter = runAfter
		
		available = true
		complete = false
	}
	
	static mapping = {
		available index:'available_idx'
		taskGroupId index:'task_group_id_idx'
		complexity defaultValue: "0"
		progress defaultValue: "0"
	}
	
	static constraints = {
		serverIp(nullable:true)
		user(nullable:true)
		status(nullable:true)
		error(nullable:true, maxSize: 1000)
		config(maxSize: 1000)
		skip(nullable:true)
		runAfter(nullable:true)
	}
	
}

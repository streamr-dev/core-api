package com.unifina.task;

import java.util.Map;

import org.codehaus.groovy.grails.commons.GrailsApplication;

import com.unifina.domain.task.Task;

public abstract class AbstractTask {

	protected Map<String, Object> config;
	protected GrailsApplication grailsApplication;
	protected Task task;
	
	public AbstractTask(Task task, Map<String, Object> config, GrailsApplication grailsApplication) {
		this.task = task;
		this.config = config;
		this.grailsApplication = grailsApplication;
	}

	public abstract boolean run();
	
	public abstract void onComplete(boolean taskGroupComplete);
	
}

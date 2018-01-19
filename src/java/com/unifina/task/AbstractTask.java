package com.unifina.task;

import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.groovy.grails.commons.GrailsApplication;

import com.unifina.domain.task.Task;

public abstract class AbstractTask {

	protected Map<String, Object> config;
	protected GrailsApplication grailsApplication;
	protected Task task;
	
	private static final Logger log = Logger.getLogger(AbstractTask.class);
	
	public AbstractTask(Task task, Map<String, Object> config, GrailsApplication grailsApplication) {
		this.task = task;
		this.config = config;
		this.grailsApplication = grailsApplication;
	}

	public abstract boolean run();
	public abstract void onComplete(boolean taskGroupComplete);

	/**
	 * Attempts to abort the task. Implementation is optional.
	 * The default implementation logs a warning.
	 */
	public void abort() {
		log.warn("Abort not implemented: "+this);
	}
	
}

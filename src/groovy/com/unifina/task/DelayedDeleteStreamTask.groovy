package com.unifina.task

import com.unifina.domain.data.Stream
import com.unifina.domain.task.Task
import com.unifina.service.StreamService
import org.codehaus.groovy.grails.commons.GrailsApplication

public class DelayedDeleteStreamTask extends AbstractTask {

	private StreamService streamService;

	public DelayedDeleteStreamTask(Task task, Map<String, Object> config,
								GrailsApplication grailsApplication) {
		super(task, config, grailsApplication);
		streamService = grailsApplication.getMainContext().getBean(StreamService);
	}

	@Override
	public boolean run() {
		Stream.withTransaction {
			config.streamIds.collect { Stream.load(it) }.each {
				streamService.deleteStream(it)
			}
		}
		return true
	}

	@Override
	public void onComplete(boolean taskGroupComplete) {

	}

	public static Map<String,Object> getConfig(List<Stream> streams) {
		return [streamIds: streams.collect {it.id}]
	}
}
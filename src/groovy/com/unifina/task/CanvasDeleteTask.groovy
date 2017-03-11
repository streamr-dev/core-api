package com.unifina.task

import com.unifina.domain.data.Stream
import com.unifina.domain.signalpath.Canvas
import com.unifina.domain.task.Task
import com.unifina.service.CanvasService
import org.codehaus.groovy.grails.commons.GrailsApplication

public class CanvasDeleteTask extends AbstractTask {

	private CanvasService canvasService

	public CanvasDeleteTask(Task task, Map<String, Object> config,
							GrailsApplication grailsApplication) {
		super(task, config, grailsApplication);
		canvasService = grailsApplication.getMainContext().getBean(CanvasService);
	}

	@Override
	public boolean run() {
		Canvas.withTransaction {
			canvasService.deleteCanvas(Canvas.get(config.canvasId), task.user, false, Stream.findAllByIdInList(config.uiChannelIds))
		}
		return true
	}

	@Override
	public void onComplete(boolean taskGroupComplete) {

	}

	public static Map<String,Object> getConfig(Canvas canvas, Collection<Stream> uiChannels) {
		return [canvasId: canvas.id, uiChannelIds: uiChannels.collect {it.id}]
	}
}
package com.unifina.task

import com.unifina.domain.signalpath.Canvas
import com.unifina.domain.task.Task
import com.unifina.service.CanvasService
import grails.util.Holders
import org.codehaus.groovy.grails.commons.GrailsApplication

public class CanvasDeleteTask extends AbstractTask {

	private CanvasService canvasService

	public CanvasDeleteTask(Task task, Map<String, Object> config,
							GrailsApplication grailsApplication) {
		super(task, config, grailsApplication);
		canvasService = Holders.getApplicationContext().getBean(CanvasService);
	}

	@Override
	public boolean run() {
		Canvas.withTransaction {
			task = Task.get(task.id) // re-fetch from db, because instance is stale
			canvasService.deleteCanvas(
					Canvas.get(config.canvasId),
					task.user,
					false)
		}
		return true
	}

	@Override
	public void onComplete(boolean taskGroupComplete) {

	}

	public static Map<String,Object> getConfig(Canvas canvas) {
		return [canvasId: canvas.id]
	}
}
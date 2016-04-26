package com.unifina.task

import com.unifina.domain.signalpath.Canvas
import com.unifina.domain.task.Task
import com.unifina.service.CanvasService
import com.unifina.service.KafkaService
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.commons.GrailsApplication

/**
 * Starts a canvas via the task queue. Tries to start it normally, and can optionally
 * reset-and-start if normal starting fails.
 */
public class CanvasStartTask extends AbstractTask {

	private CanvasService canvasService;

	private static final Logger log = Logger.getLogger(CanvasStartTask)

	public CanvasStartTask(Task task, Map<String, Object> config,
						   GrailsApplication grailsApplication) {
		super(task, config, grailsApplication);
		canvasService = (KafkaService) grailsApplication.getMainContext().getBean("canvasService");
	}

	@Override
	public boolean run() {
		Canvas canvas = Canvas.get(config.id)

		try {
			canvasService.start(config.forceReset)
		} catch (Exception e) {
			if (!config.forceReset && config.tryResetting) {
				log.error("Failed to start canvas $canvas.id, trying again by clearing serialization...", e)
				canvasService.start(true)
			} else {
				log.error("Failed to start canvas $canvas.id", e)
			}
		}

		return true
	}

	@Override
	public void onComplete(boolean taskGroupComplete) {
		
	}
	
	public static Map<String,Object> getConfig(Canvas canvas, boolean resetOnFail, boolean forceReset) {
		return [id:canvas.id, resetOnFail: resetOnFail, forceReset: forceReset]
	}
}

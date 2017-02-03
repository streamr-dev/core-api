package com.unifina.task

import com.unifina.domain.signalpath.Canvas
import com.unifina.domain.task.Task
import com.unifina.service.CanvasService
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
		canvasService = (CanvasService) grailsApplication.getMainContext().getBean("canvasService");
	}

	// TODO: used by unit test to set mock CanvasService. Can be removed when upgrading to Grails 2.4+
	public CanvasStartTask(Task task, Map<String, Object> config,
						   GrailsApplication grailsApplication, CanvasService canvasService) {
		super(task, config, grailsApplication);
		this.canvasService = canvasService
	}

	@Override
	public boolean run() {
		Canvas canvas

		try {
			Canvas.withTransaction {
				canvas = Canvas.get(config.id)
				if (!canvas) {
					throw new RuntimeException("Canvas "+config.id+" could not be found anymore!")
				}
				canvasService.start(canvas, config.forceReset)
			}
		} catch (Exception e) {
			if (!config.forceReset && config.resetOnFail) {
				log.error("Failed to start canvas $canvas.id, trying again by clearing serialization...", e)
				Canvas.withTransaction {
					canvas = Canvas.get(config.id)
					canvasService.start(canvas, true)
				}
			} else {
				log.error("Failed to start canvas $canvas.id", e)
			}
		}

		return true
	}

	@Override
	public void onComplete(boolean taskGroupComplete) {
		
	}
	
	public static Map<String,Object> getConfig(Canvas canvas, boolean forceReset, boolean resetOnFail) {
		return [id:canvas.id, resetOnFail: resetOnFail, forceReset: forceReset]
	}
}

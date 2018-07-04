package com.unifina.signalpath.messaging

import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas
import com.unifina.service.CanvasService
import com.unifina.signalpath.*
import grails.util.Holders

import java.text.Format
import java.text.MessageFormat
import java.text.SimpleDateFormat

class EmailModule extends ModuleWithSideEffects {

	StringParameter sub = new StringParameter(this, "subject", "")
	StringParameter message = new StringParameter(this, "message", "")

	String sender

	int emailInputCount = 1

	Long prevTime
	Long emailInterval = 60000
	boolean emailSent
	boolean lastEmailBlocked
	transient CanvasService canvasService

	@Override
	void init() {
		addInput(sub)
		addInput(message)
		sender = Holders.grailsApplication.config.unifina.email.sender
		emailSent = true
	}

	@Override
	void activateWithSideEffects() {
		if (isNotTooOften(emailInterval, globals.time.getTime(), prevTime)) {
			prevTime = globals.time.getTime()
			emailSent = true
			String messageTo = SecUser.get(globals.userId).username
			def mailService = Holders.grailsApplication.getMainContext().getBean("mailService")
			String messageBody = getMessageBody()
			mailService.sendMail {
				from sender
				to messageTo
				subject sub.getValue()
				body messageBody
			}
			lastEmailBlocked = false
		} else if (emailSent) {
			lastEmailBlocked = true
			parentSignalPath.pushToUiChannel(new NotificationMessage("Tried to send emails too often"))
			emailSent = false
		}
	}

	@Override
	void activateWithoutSideEffects() {
		// Show email contents as notifications in the UI
		parentSignalPath?.showNotification(getMessageBody())
	}

	private String linkToCanvas() {
		if (canvasService == null) {
			canvasService = (CanvasService) Holders.grailsApplication.getMainContext().getBean("canvasService")
		}
		final Canvas c = getRootSignalPath().canvas
		return canvasService.getCanvasURL(c)
	}

	private String getMessageBody() {
		// Create String with the input values
		String inputValues = ""
		for (Input i : super.getInputs()){
			if (!(i instanceof Parameter)){
				inputValues += "${i.getDisplayName() ?: i.getName()}: ${i.getValue()}\n"
			}
		}

		String body = """
This email was sent by one of your running Canvases on Streamr.

Message:
{0}

Event Timestamp:
{1,date,yyyy-MM-dd HH:mm:ss.SSS}

Input Values:
{2}
To view, edit, or stop the Canvas that sent this message, click the below link:
{3}
{4}"""

		MessageFormat mf = new MessageFormat(body)
		for (Format format : mf.getFormats()) {
			if (format instanceof SimpleDateFormat && globals.userId != null) {
				((SimpleDateFormat) format).setTimeZone(globals.userTimeZone)
			}
		}
		String warning = ""
		if (lastEmailBlocked) {
			warning = "\nWARNING: Some emails between this and the last mail have not been sent because of trying to send too frequently.\n"
		}
		Object[] data = [
			message.getValue(),
			globals.time,
			inputValues,
			linkToCanvas(),
			warning
		]
		return mf.format(data)
	}

	private Input<Object> createAndAddInput(String name) {

		Input<Object> conn = new Input<Object>(this,name, "Object");

		conn.setDrivingInput(true);

		// Add the input
		if (getInput(name)==null){
			addInput(conn);
		}

		return conn;
	}

	@Override
	Map<String,Object> getConfiguration() {
		Map<String,Object> config = super.getConfiguration();

		// Module options
		ModuleOptions options = ModuleOptions.get(config);
		options.addIfMissing(new ModuleOption("inputs", emailInputCount, "int"));

		return config;
	}

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);

		ModuleOptions options = ModuleOptions.get(config);

		if (options.getOption("inputs")!=null)
			emailInputCount = options.getOption("inputs").getInt();

		for (int i=1;i<=emailInputCount;i++) {
			createAndAddInput("value"+i);
		}
	}

	private static boolean isNotTooOften(interval, newTime, prevTime){
		return prevTime == null || newTime - prevTime > interval
	}

	@Override
	void clearState() {
		prevTime = null
		emailSent = false
		emailInputCount = 1
		lastEmailBlocked = false
	}

	@Override
	protected boolean allowSideEffectsInHistoricalMode() {
		return false
	}
}

package com.unifina.signalpath.messaging

import com.unifina.signalpath.*

import java.text.SimpleDateFormat

class EmailModule extends ModuleWithSideEffects {

	StringParameter sub = new StringParameter(this, "subject", "")
	StringParameter message = new StringParameter(this, "message", "")

	String sender

	int emailInputCount = 1

	transient SimpleDateFormat df

	Long prevTime
	Long emailInterval = 60000
	boolean emailSent
	boolean lastEmailBlocked

	@Override
	public void init() {
		addInput(sub)
		addInput(message)
		sender = globals.grailsApplication.config.unifina.email.sender
		initDf()
		emailSent = true
	}

	@Override
	public void activateWithSideEffects() {
		if (isNotTooOften(emailInterval, globals.time.getTime(), prevTime)) {
			prevTime = globals.time.getTime()
			emailSent = true
			String messageTo = globals.getUser().getUsername()
			def mailService = globals.grailsApplication.getMainContext().getBean("mailService")
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
	public void activateWithoutSideEffects() {
		// Show email contents as notifications in the UI
		parentSignalPath?.showNotification(getMessageBody())
	}

	private String getMessageBody() {
		initDf()

		// Create String with the input values
		String inputValues = ""
		for (Input i : super.getInputs()){
			if (!(i instanceof Parameter)){
				inputValues += "${i.getDisplayName() ?: i.getName()}: ${i.getValue()}\n"
			}
		}

		// Create body for the email
		String messageBody = ("\n" +
				"Message:\n" +
				"${message.getValue()}\n\n" +
				"Event Timestamp:\n" +
				"${df.format(globals.time)}\n\n" +
				"Input Values:\n" +
				"${inputValues}\n" +
				(lastEmailBlocked ? "\nWARNING: Some emails between this and the last mail have not been sent because of trying to send too frequently.\n" : "") +
				"")

		return messageBody
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
	public Map<String,Object> getConfiguration() {
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
	public void clearState() {
		prevTime = null
		emailSent = false
		emailInputCount = 1
		lastEmailBlocked = false
	}

	private def initDf() {
		if (df == null) {
			df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
			if (globals.getUser()!=null)
				df.setTimeZone(TimeZone.getTimeZone(globals.getUser().getTimezone()))
		}
	}

	@Override
	protected boolean allowSideEffectsInHistoricalMode() {
		return false
	}
}

package com.unifina.signalpath.messaging

import java.text.SimpleDateFormat

import com.unifina.signalpath.AbstractSignalPathModule
import com.unifina.signalpath.Input
import com.unifina.signalpath.ModuleOption
import com.unifina.signalpath.ModuleOptions
import com.unifina.signalpath.NotificationMessage
import com.unifina.signalpath.Parameter
import com.unifina.signalpath.StringParameter

class EmailModule extends AbstractSignalPathModule {

	StringParameter sub = new StringParameter(this, "subject", "")
	StringParameter message = new StringParameter(this, "message", "")

	String sender

	int emailInputCount = 1

	transient SimpleDateFormat df

	Long prevTime = 0
	Long prevWarnNotif

	Long emailIntervall = 60000
	boolean emailSent

	@Override
	public void init() {
		addInput(sub)
		addInput(message)
		sender = globals.grailsApplication.config.unifina.email.sender
		initDf()
		emailSent = true
	}

	@Override
	public void sendOutput() {
		initDf()
		//		Create String with the input values
		String inputValues = ""
		for(Input i : super.getInputs()){
			if(!(i instanceof Parameter)){
				inputValues += "${i.getDisplayName() ?: i.getName()}: ${i.getValue()}\n"
			}
		}

		//		Check that the subject is not empty
		String messageSubject
		if(sub.getValue() == ""){
			messageSubject = "no subject"
		} else {
			messageSubject = sub.getValue()
		}

		//		Create body for the email
		String messageBody = """
Message:
${message.getValue()}

Event Timestamp:
${df.format(globals.time)}

Input Values:
$inputValues
"""

		//		Check that the module is running in current time. If not, do not send email, make just a notification

		if (globals.isRealtime()) {
			if (isNotTooOften(emailIntervall, getTime(), prevTime)) {
				emailSent = true
				String messageTo = globals.getUser().getUsername()
				def mailService = globals.grailsApplication.getMainContext().getBean("mailService")
				mailService.sendMail {
					from sender
					to messageTo
					subject messageSubject
					body messageBody
				}
			} else {
				if (emailSent) {
					globals.uiChannel?.push(new NotificationMessage("Tried to send emails too often"), parentSignalPath.uiChannelId)
					emailSent = false
				}
			}
		}
		else {
			globals.uiChannel?.push(new NotificationMessage(messageBody), parentSignalPath.uiChannelId)
		}


	}
	
	public long getTime(){
		return System.currentTimeMillis()
	}

	public Input<Object> createAndAddInput(String name) {

		Input<Object> conn = new Input<Object>(this,name,"Object");

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
		options.add(new ModuleOption("inputs", emailInputCount, "int"));

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

	public boolean isNotTooOften(intervall, time1, time2){
		return Math.abs(time1 - time2) > intervall
	}

	@Override
	public void clearState() {
	}

	private def initDf() {
		if (df == null) {
			df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
			if (globals.getUser()!=null)
				df.setTimeZone(TimeZone.getTimeZone(globals.getUser().getTimezone()))
		}
	}

}

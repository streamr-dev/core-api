package com.unifina.signalpath.time;

import com.unifina.datasource.ITimeListener;
import com.unifina.signalpath.*;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ClockModule extends AbstractSignalPathModule implements ITimeListener {

	private final StringParameter format = new StringParameter(this, "format", "yyyy-MM-dd HH:mm:ss z");
	private final EnumParameter<TimeUnit> tickUnit = new EnumParameter<>(this, "unit", TimeUnit.values());
	private final IntegerParameter tickRate = new NonZeroIntegerParameter(this, "rate", 1);
	
	private final StringOutput date = new StringOutput(this, "date");
	private final TimeSeriesOutput ts = new TimeSeriesOutput(this,"timestamp");

	private SimpleDateFormat df = null;

	public ClockModule() {
		format.setCanToggleDrivingInput(false);
		tickUnit.setCanToggleDrivingInput(false);
		tickRate.setCanToggleDrivingInput(false);
	}

	@Override
	public void sendOutput() {}
	
	@Override
	public void clearState() {}
	
	@Override
	public void setTime(Date timestamp) {
		updateDateFormatIfNecessary(format.getValue());
		date.send(df.format(timestamp));
		ts.send(timestamp.getTime());
	}

	@Override
	public int tickRateInSec() {
		return tickUnit.getValue().tickRateInSec(tickRate.getValue());
	}

	private void updateDateFormatIfNecessary(String format) {
		if (df == null) {
			df = new SimpleDateFormat(format);
			df.setTimeZone(getGlobals().getUserTimeZone());
		} else if (!df.toPattern().equals(format)) {
			df.applyPattern(format);
		}
	}

	static class NonZeroIntegerParameter extends IntegerParameter {

		NonZeroIntegerParameter(AbstractSignalPathModule owner, String name, Integer defaultValue) {
			super(owner, name, defaultValue);
		}

		@Override
		public Integer getValue() {
			Integer v = super.getValue();
			if (v == 0) {
				throw new IllegalArgumentException(getLongName() + " cannot equal 0.");
			}
			return v;
		}
	}

	enum TimeUnit {
		SECOND(1),
		MINUTE(60),
		HOUR(60 * 60),
		DAY(60 * 60 * 24);

		private final int baseRate;

		TimeUnit(int baseRate) {
			this.baseRate = baseRate;
		}

		int tickRateInSec(int tickRate) {
			return tickRate * baseRate;
		}

		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}

}

package com.unifina.signalpath.text;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.IntegerParameter;
import com.unifina.signalpath.StringOutput;
import com.unifina.signalpath.TimeSeriesInput;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class FormatNumber extends AbstractSignalPathModule {
	private final IntegerParameter decimalPlaces = new IntegerParameter(this, "decimalPlaces", 0);
	private final TimeSeriesInput number = new TimeSeriesInput(this, "number");
	private final StringOutput text = new StringOutput(this, "text");

	private final DecimalFormat decimalFormatter = new DecimalFormat();
	private int lastDecimalPlaces = decimalPlaces.getValue();

	public FormatNumber() {
		DecimalFormatSymbols dfs = new DecimalFormatSymbols(Locale.getDefault());
		dfs.setDecimalSeparator('.'); // Replace decimal comma with decimal period
		decimalFormatter.setDecimalFormatSymbols(dfs);
		decimalFormatter.setGroupingSize(0); // Do not group integer part
		refreshFormatter();
	}

	@Override
	public void sendOutput() {
		refreshFormatterIfNeeded();
		text.send(decimalFormatter.format(number.getValue()));
	}

	private void refreshFormatterIfNeeded() {
		if (lastDecimalPlaces != decimalPlaces.getValue()) {
			lastDecimalPlaces = decimalPlaces.getValue();
			refreshFormatter();
		}
	}

	private void refreshFormatter() {
		decimalFormatter.setMinimumFractionDigits(lastDecimalPlaces);
		decimalFormatter.setMaximumFractionDigits(lastDecimalPlaces);
	}

	@Override
	public void clearState() {}
}

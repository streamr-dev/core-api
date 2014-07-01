package com.unifina.signalpath.statistics;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.StorelessUnivariateStatistic;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.IntegerParameter;
import com.unifina.signalpath.TimeSeriesInput;
import com.unifina.signalpath.TimeSeriesOutput;

/**
 * Helper class for implementing the stuff provided by Apache Commons Math
 * DescriptiveStatistics as separate modules. Supports infinite windows by
 * setting windowLength to 0 - in this case stateless implementations are used.
 * @author Henri
 *
 */
public abstract class DescriptiveStatisticsAdapter extends AbstractSignalPathModule {

	protected IntegerParameter windowLength = new IntegerParameter(this, "windowLength", 50);
	protected IntegerParameter minSamples = new IntegerParameter(this, "minSamples", 1);
	
	protected TimeSeriesInput input = new TimeSeriesInput(this, "in");
	protected TimeSeriesOutput out = new TimeSeriesOutput(this, "out");
	
	private Boolean storeless = null;
	
	private DescriptiveStatistics stats;
	private StorelessUnivariateStatistic storelessStats;
	
	@Override
	public void init() {
		addInput(windowLength);
		addInput(minSamples);
		addInput(input);
		addOutput(out);
	}
	
	@Override
	public void sendOutput() {
		// Initialize the storeless/stateful statistic.
		if (storeless==null) {
			// Use the storeless one only if windowLength is zero and unconnected
			storeless = windowLength.getValue()==0 && !windowLength.isConnected();
			if (storeless)
				storelessStats = getStorelessStatistic();
			else {
				stats = new DescriptiveStatistics(windowLength.getValue());
			}
		}
		
		// If we use the windowed stats, check that the window size is correct
		if (!storeless && stats.getWindowSize()!=windowLength.getValue()) {
			stats.setWindowSize(windowLength.getValue());
		}
		
		// Increment stats and produce output if more than minSamples values received
		if (storeless) {
			storelessStats.increment(input.value);
			if (storelessStats.getN()>=minSamples.getValue())
				out.send(storelessStats.getResult());
		}
		else {
			stats.addValue(input.value);
			if (stats.getN()>=minSamples.getValue())
				out.send(getValue(stats));
		}
	}

	/**
	 * Return the StorelessUnivariateStatistic that implements the correct statistic
	 */
	protected abstract StorelessUnivariateStatistic getStorelessStatistic();
	/**
	 * Call the function in DescriptiveStatistics that returns the correct statistic
	 */
	protected abstract Double getValue(DescriptiveStatistics stats);

	@Override
	public void clearState() {
		if (stats!=null)
			stats.clear();
		if (storelessStats!=null)
			storelessStats.clear();
	}

}

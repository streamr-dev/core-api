package com.unifina.signalpath.statistics;

import com.unifina.signalpath.AbstractModuleWithWindow;
import com.unifina.signalpath.TimeSeriesInput;
import com.unifina.signalpath.TimeSeriesOutput;
import com.unifina.utils.window.WindowListener;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.StorelessUnivariateStatistic;

import java.io.Serializable;
import java.util.Map;

/**
 * Helper class for implementing the stuff provided by Apache Commons Math
 * DescriptiveStatistics as separate modules. Uses stateless implementations
 * in case windowLength is 0.
 * @author Henri
 */
public abstract class DescriptiveStatisticsAdapter extends AbstractModuleWithWindow<Double> {

	protected TimeSeriesInput input = new TimeSeriesInput(this, "in");
	protected TimeSeriesOutput out = new TimeSeriesOutput(this, "out");
	
	private Boolean storeless = null;
	
	private DescriptiveStatistics stats;
	private StorelessUnivariateStatistic storelessStats;
	
	@Override
	public void init() {
		super.init();
		addInput(input);
		addOutput(out);
	}

	@Override
	protected void handleInputValues() {
		addToWindow(input.getValue());
	}

	@Override
	protected void doSendOutput() {
		if (storeless) {
			out.send(storelessStats.getResult());
		}
		else {
			Double d = getValue(stats);
			if (!d.equals(Double.NaN))
				out.send(d);
		}
	}

	@Override
	protected WindowListener<Double> createWindowListener(Object key) {
		return new DescriptiveStatisticsAdapterWindowListener();
	}

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);

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
	}

	/**
	 * Return the StorelessUnivariateStatistic that implements the correct statistic
	 */
	protected abstract StorelessUnivariateStatistic getStorelessStatistic();
	/**
	 * Call the function in DescriptiveStatistics that returns the correct statistic
	 */
	protected abstract Double getValue(DescriptiveStatistics stats);

	class DescriptiveStatisticsAdapterWindowListener implements WindowListener<Double>, Serializable {

		@Override
		public void onAdd(Double item) {
			if (storeless) {
				storelessStats.increment(item);
			}
			else {
				resizeStats();
				stats.addValue(item);
			}
		}

		@Override
		public void onRemove(Double item) {
			if (storeless) {
				throw new IllegalStateException("Window is infinite and values should never be removed! There must be a bug!");
			}
			else {
				resizeStats();
			}
		}

		@Override
		public void onClear() {
			if (stats!=null)
				stats.clear();
			if (storelessStats!=null)
				storelessStats.clear();
		}

		private void resizeStats() {
			if (windowByKey.get(0).getLength() != stats.getWindowSize())
				stats.setWindowSize(windowByKey.get(0).getLength());
		}
	}
}

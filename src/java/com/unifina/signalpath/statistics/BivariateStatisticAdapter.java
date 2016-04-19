package com.unifina.signalpath.statistics;

import com.unifina.signalpath.AbstractModuleWithWindow;
import com.unifina.signalpath.TimeSeriesInput;
import com.unifina.signalpath.TimeSeriesOutput;
import com.unifina.utils.window.AbstractWindow;
import com.unifina.utils.window.WindowListener;

/**
 * A common parent class for bivariate statistics (covariance, correlations...) that
 * need to be computed from scratch using two double[] arrays.
 * @param <Statistic>
 */
public abstract class BivariateStatisticAdapter<Statistic> extends AbstractModuleWithWindow<Double> {

	TimeSeriesInput x = new TimeSeriesInput(this, "inX");
	TimeSeriesInput y = new TimeSeriesInput(this, "inY");
	
	TimeSeriesOutput out = new TimeSeriesOutput(this, getOutputName());
	
	transient Statistic stat;
	transient private double[] xValues = null;
	transient private double[] yValues = null;

	@Override
	public void init() {
		super.init();
	}

	@Override
	protected void handleInputValues() {
		addToWindow(x.getValue(), 0);
		addToWindow(y.getValue(), 1);
	}

	@Override
	protected void doSendOutput() {
		AbstractWindow<Double> xWindow = windowByKey.get(0);
		AbstractWindow<Double> yWindow = windowByKey.get(1);

		if (stat == null) {
			stat = createStatistic();
		}
		if (xValues == null || xValues.length != xWindow.getSize()) {
			xValues = new double[xWindow.getSize()];
			yValues = new double[yWindow.getSize()];
		}

		// Goddamn slow. Is there a better way to unbox a bunch of Doubles more effectively?
		int i=0;
		for (Double d : xWindow) {
			xValues[i++] = d;
		}
		i=0;
		for (Double d : yWindow) {
			yValues[i++] = d;
		}

		Double result = calculate(xValues, yValues, stat);
		if (!result.isNaN()) {
			out.send(result);
		}
	}

	@Override
	protected WindowListener<Double> createWindowListener(Object key) {
		// Not interested in window events, because we always need the whole contents of the window to compute Covariance
		return null;
	}

	/**
	 * @return The statistic
     */
	protected abstract Statistic createStatistic();

	/**
	 * Calculates the statistic
	 * @return
     */
	protected abstract Double calculate(double[] xValues, double[] yValues, Statistic stat);

	/**
	 * @return The name of the singleton output
     */
	protected abstract String getOutputName();

}

package com.unifina.signalpath.statistics;

import com.unifina.signalpath.AbstractModuleWithWindow;
import com.unifina.signalpath.TimeSeriesInput;
import com.unifina.signalpath.TimeSeriesOutput;
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
	protected Integer getDimensions() {
		return 2;
	}

	@Override
	protected void handleInputValues() {
		addToWindow(x.getValue(), 0);
		addToWindow(y.getValue(), 1);
	}

	@Override
	protected void doSendOutput() {
		if (stat == null) {
			stat = createStatistic();
		}
		if (xValues == null || xValues.length != windows[0].getSize()) {
			xValues = new double[windows[0].getSize()];
			yValues = new double[windows[0].getSize()];
		}

		// Goddamn slow. Is there a better way to unbox a bunch of Doubles more effectively?
		int i=0;
		for (Double d : windows[0]) {
			xValues[i++] = d;
		}
		i=0;
		for (Double d : windows[1]) {
			yValues[i++] = d;
		}

		Double result = calculate(xValues, yValues, stat);
		if (!result.isNaN()) {
			out.send(result);
		}
	}

	@Override
	protected WindowListener<Double> createWindowListener(int dimension) {
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

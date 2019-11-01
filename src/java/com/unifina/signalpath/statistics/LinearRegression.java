package com.unifina.signalpath.statistics;

import com.unifina.signalpath.AbstractModuleWithWindow;
import com.unifina.signalpath.TimeSeriesInput;
import com.unifina.signalpath.TimeSeriesOutput;
import com.unifina.utils.window.WindowListener;
import org.apache.commons.math3.stat.regression.SimpleRegression;

import java.io.Serializable;

public class LinearRegression extends AbstractModuleWithWindow<com.unifina.signalpath.statistics.LinearRegression.XYPair> {

	TimeSeriesInput input = new TimeSeriesInput(this,"in");
	
	TimeSeriesOutput slope = new TimeSeriesOutput(this,"slope");
	TimeSeriesOutput error = new TimeSeriesOutput(this,"error");
	TimeSeriesOutput rsq = new TimeSeriesOutput(this,"R^2");

	long counter = 0;
	SimpleRegression regression = new SimpleRegression();
	
	@Override
	public void init() {
		super.init();

		// Control ordering of outputs
		addOutput(slope);
		addOutput(error);
		addOutput(rsq);
	}

	@Override
	protected void handleInputValues() {
		addToWindow(new XYPair((double) counter++, input.getValue()));
	}

	@Override
	protected void doSendOutput() {
		slope.send(regression.getSlope());
		error.send(regression.getMeanSquareError());
		rsq.send(regression.getRSquare());
	}

	@Override
	protected WindowListener<XYPair> createWindowListener(Object key) {
		return new LinearRegressionWindowListener();
	}

	class LinearRegressionWindowListener implements WindowListener<XYPair> {

		@Override
		public void onAdd(XYPair item) {
			regression.addData(item.x, item.y);
		}

		@Override
		public void onRemove(XYPair item) {
			regression.removeData(item.x, item.y);
		}

		@Override
		public void onClear() {
			regression.clear();
			counter = 0;
		}
	}

	static class XYPair implements Serializable {
		public double x;
		public double y;

		public XYPair(double x, double y) {
			this.x = x;
			this.y = y;
		}
	}
}

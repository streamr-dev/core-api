package com.unifina.signalpath.statistics;

import com.unifina.signalpath.AbstractModuleWithWindow;
import com.unifina.signalpath.TimeSeriesInput;
import com.unifina.signalpath.TimeSeriesOutput;
import com.unifina.utils.window.WindowListener;
import org.apache.commons.math3.stat.regression.SimpleRegression;

import java.io.Serializable;

public class LinearRegressionXY extends AbstractModuleWithWindow<LinearRegressionXY.XYPair> {
	
	TimeSeriesInput x = new TimeSeriesInput(this,"inX");
	TimeSeriesInput y = new TimeSeriesInput(this,"inY");
	
	TimeSeriesOutput slope = new TimeSeriesOutput(this,"slope");
	TimeSeriesOutput intercept = new TimeSeriesOutput(this,"intercept");
	TimeSeriesOutput error = new TimeSeriesOutput(this,"error");
	TimeSeriesOutput rsq = new TimeSeriesOutput(this,"R^2");

	SimpleRegression regression = new SimpleRegression();
	
	@Override
	public void init() {
		super.init();

		// Control the order of outputs
		addOutput(slope);
		addOutput(intercept);
		addOutput(error);
		addOutput(rsq);
	}

	@Override
	protected void handleInputValues() {
		addToWindow(new XYPair(x.getValue(), y.getValue()));
	}

	@Override
	protected void doSendOutput() {
		double s = regression.getSlope();
		if (!Double.isNaN(s)) {
			slope.send(regression.getSlope());
			intercept.send(regression.getIntercept());
			error.send(regression.getMeanSquareError());
			rsq.send(regression.getRSquare());
		}
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

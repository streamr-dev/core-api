package com.unifina.signalpath.statistics;

public class PearsonsCorrelation extends BivariateStatisticAdapter<org.apache.commons.math3.stat.correlation.PearsonsCorrelation> {

	@Override
	public void init() {
		super.init();
		minSamples.setDefaultValue(2);
	}

	@Override
	protected org.apache.commons.math3.stat.correlation.PearsonsCorrelation createStatistic() {
		return new org.apache.commons.math3.stat.correlation.PearsonsCorrelation();
	}

	@Override
	protected Double calculate(double[] xValues, double[] yValues, org.apache.commons.math3.stat.correlation.PearsonsCorrelation stat) {
		return stat.correlation(xValues, yValues);
	}

	@Override
	protected String getOutputName() {
		return "corr";
	}

}

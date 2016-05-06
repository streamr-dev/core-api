package com.unifina.signalpath.statistics;

public class Covariance extends BivariateStatisticAdapter<org.apache.commons.math3.stat.correlation.Covariance> {

	@Override
	public void init() {
		super.init();
		minSamples.setDefaultValue(2);
	}

	@Override
	protected org.apache.commons.math3.stat.correlation.Covariance createStatistic() {
		return new org.apache.commons.math3.stat.correlation.Covariance();
	}

	@Override
	protected Double calculate(double[] xValues, double[] yValues, org.apache.commons.math3.stat.correlation.Covariance stat) {
		return stat.covariance(xValues, yValues);
	}

	@Override
	protected String getOutputName() {
		return "cov";
	}

}

package com.unifina.signalpath.statistics;

import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

public class SpearmansRankCorrelation extends BivariateStatisticAdapter<SpearmansCorrelation> {

	@Override
	public void init() {
		super.init();
		minSamples.setDefaultValue(2);
	}

	@Override
	protected SpearmansCorrelation createStatistic() {
		return new SpearmansCorrelation();
	}

	@Override
	protected Double calculate(double[] xValues, double[] yValues, SpearmansCorrelation stat) {
		return stat.correlation(xValues, yValues);
	}

	@Override
	protected String getOutputName() {
		return "corr";
	}

}

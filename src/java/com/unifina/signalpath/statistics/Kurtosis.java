package com.unifina.signalpath.statistics;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.StorelessUnivariateStatistic;

public class Kurtosis extends DescriptiveStatisticsAdapter {

	@Override
	public void init() {
		super.init();
		minSamples.setDefaultValue(4);
	}
	
	@Override
	protected StorelessUnivariateStatistic getStorelessStatistic() {
		return new org.apache.commons.math3.stat.descriptive.moment.Kurtosis();
	}

	@Override
	protected Double getValue(DescriptiveStatistics stats) {
		return stats.getKurtosis();
	}

}

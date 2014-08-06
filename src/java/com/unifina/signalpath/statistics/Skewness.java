package com.unifina.signalpath.statistics;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.StorelessUnivariateStatistic;

public class Skewness extends DescriptiveStatisticsAdapter {
	
	@Override
	protected StorelessUnivariateStatistic getStorelessStatistic() {
		return new org.apache.commons.math3.stat.descriptive.moment.Skewness();
	}

	@Override
	protected Double getValue(DescriptiveStatistics stats) {
		return stats.getSkewness();
	}

}

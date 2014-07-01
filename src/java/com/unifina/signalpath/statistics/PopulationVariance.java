package com.unifina.signalpath.statistics;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.StorelessUnivariateStatistic;
import org.apache.commons.math3.stat.descriptive.moment.Variance;

public class PopulationVariance extends DescriptiveStatisticsAdapter {
	
	@Override
	protected StorelessUnivariateStatistic getStorelessStatistic() {
		return new Variance(false);
	}

	@Override
	protected Double getValue(DescriptiveStatistics stats) {
		return stats.getPopulationVariance();
	}

}

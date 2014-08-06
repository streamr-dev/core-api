package com.unifina.signalpath.statistics;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.StorelessUnivariateStatistic;
import org.apache.commons.math3.stat.descriptive.rank.Max;

public class MaxSliding extends DescriptiveStatisticsAdapter {

	@Override
	protected StorelessUnivariateStatistic getStorelessStatistic() {
		return new Max();
	}

	@Override
	protected Double getValue(DescriptiveStatistics stats) {
		return stats.getMax();
	}

}

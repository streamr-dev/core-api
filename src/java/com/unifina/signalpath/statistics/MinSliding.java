package com.unifina.signalpath.statistics;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.StorelessUnivariateStatistic;
import org.apache.commons.math3.stat.descriptive.rank.Min;

public class MinSliding extends DescriptiveStatisticsAdapter {

	@Override
	protected StorelessUnivariateStatistic getStorelessStatistic() {
		return new Min();
	}

	@Override
	protected Double getValue(DescriptiveStatistics stats) {
		return stats.getMin();
	}

}

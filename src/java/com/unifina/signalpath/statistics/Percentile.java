package com.unifina.signalpath.statistics;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.StorelessUnivariateStatistic;

import com.unifina.signalpath.DoubleParameter;

public class Percentile extends DescriptiveStatisticsAdapter {

	DoubleParameter percentage = new DoubleParameter(this, "percentage", 25D);

	@Override
	protected StorelessUnivariateStatistic getStorelessStatistic() {
		throw new IllegalArgumentException("Percentile does not support an infinite window!");
	}

	@Override
	protected Double getValue(DescriptiveStatistics stats) {
		return stats.getPercentile(percentage.getValue());
	}

}

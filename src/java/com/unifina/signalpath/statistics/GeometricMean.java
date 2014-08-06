package com.unifina.signalpath.statistics;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.StorelessUnivariateStatistic;

public class GeometricMean extends DescriptiveStatisticsAdapter {

	@Override
	protected StorelessUnivariateStatistic getStorelessStatistic() {
		return new org.apache.commons.math3.stat.descriptive.moment.GeometricMean();
	}

	@Override
	protected Double getValue(DescriptiveStatistics stats) {
		return stats.getGeometricMean();
	}

}

package com.unifina.signalpath.statistics;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.StorelessUnivariateStatistic;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.IntegerParameter;
import com.unifina.signalpath.TimeSeriesInput;
import com.unifina.signalpath.TimeSeriesOutput;

public class SimpleStatistics extends AbstractSignalPathModule {

	IntegerParameter windowLength = new IntegerParameter(this,"windowLength",60);
	
	TimeSeriesInput input = new TimeSeriesInput(this,"in");
	
	TimeSeriesOutput min = new TimeSeriesOutput(this,"min");
	TimeSeriesOutput max = new TimeSeriesOutput(this,"max");
	TimeSeriesOutput mean = new TimeSeriesOutput(this,"mean");
	TimeSeriesOutput geomMean = new TimeSeriesOutput(this,"geomMean");
	TimeSeriesOutput count = new TimeSeriesOutput(this,"count");
	TimeSeriesOutput sum = new TimeSeriesOutput(this,"sum");
	TimeSeriesOutput sumOfSquares = new TimeSeriesOutput(this,"sumOfSquares");
	TimeSeriesOutput stdDev = new TimeSeriesOutput(this,"stdDev");
	TimeSeriesOutput variance = new TimeSeriesOutput(this,"variance");
	
	Boolean storeless = null;
	
	DescriptiveStatistics stats;
	SummaryStatistics storelessStats;
	
	StorelessUnivariateStatistic dummy = new StorelessUnivariateStatistic() {
		
		@Override
		public double evaluate(double[] arg0, int arg1, int arg2) {
			return 0;
		}
		
		@Override
		public double evaluate(double[] arg0) {
			return 0;
		}
		
		@Override
		public void incrementAll(double[] arg0, int arg1, int arg2) {}
		
		@Override
		public void incrementAll(double[] arg0) {}
		
		@Override
		public void increment(double arg0) {}
		
		@Override
		public double getResult() {
			return 0;
		}
		
		@Override
		public long getN() {
			return 0;
		}
		
		@Override
		public StorelessUnivariateStatistic copy() {
			return null;
		}
		
		@Override
		public void clear() {}
	};
	
	@Override
	public void init() {
		addInput(windowLength);
		addInput(input);
		
		addOutput(min);
		addOutput(max);
		addOutput(mean);
		addOutput(geomMean);
		addOutput(count);
		addOutput(sum);
		addOutput(sumOfSquares);
		addOutput(stdDev);
		addOutput(variance);
	}
	
	private void initStats() {
		// If infinite window, use storeless implementation for memory efficiency
		if (windowLength.getValue().equals(0) && !windowLength.isConnected()) {
			storeless = true;
			storelessStats = new SummaryStatistics();
			// Avoid calculating the values if the output is not connected
			if (!min.isConnected())
				storelessStats.setMinImpl(dummy);
			if (!max.isConnected())
				storelessStats.setMaxImpl(dummy);
			if (!mean.isConnected())
				storelessStats.setMeanImpl(dummy);
			if (!geomMean.isConnected())
				storelessStats.setGeoMeanImpl(dummy);
			if (!sum.isConnected())
				storelessStats.setSumImpl(dummy);
			if (!sumOfSquares.isConnected())
				storelessStats.setSumsqImpl(dummy);
			if (!stdDev.isConnected() && !variance.isConnected())
				storelessStats.setVarianceImpl(dummy);
		}
		else {
			storeless = false;
			stats = new DescriptiveStatistics();
			stats.setWindowSize(windowLength.getValue());
		}
	}
	
	@Override
	public void clearState() {
		if (storelessStats!=null)
			storelessStats.clear();
		if (stats!=null)
			stats.clear();
		
		initStats();
	}
	
	@Override
	public void sendOutput() {
		if (storeless==null) {
			initStats();
		}
		else if (!storeless && stats.getWindowSize()!=windowLength.getValue()) {
			stats.setWindowSize(windowLength.getValue());
		}
		
		if (storeless) {
			storelessStats.addValue(input.value);
			if (min.isConnected())
				min.send(storelessStats.getMin());
			if (max.isConnected())
				max.send(storelessStats.getMax());
			if (mean.isConnected())
				mean.send(storelessStats.getMean());
			if (geomMean.isConnected())
				geomMean.send(storelessStats.getGeometricMean());
			if (count.isConnected())
				count.send(new Double(storelessStats.getN()));
			if (sum.isConnected())
				sum.send(storelessStats.getSum());
			if (sumOfSquares.isConnected())
				sumOfSquares.send(storelessStats.getSumsq());
			if (stdDev.isConnected())
				stdDev.send(storelessStats.getStandardDeviation());
			if (variance.isConnected())
				variance.send(storelessStats.getVariance());
		}
		else {
			stats.addValue(input.value);

			if (min.isConnected())
				min.send(stats.getMin());
			if (max.isConnected())
				max.send(stats.getMax());
			if (mean.isConnected())
				mean.send(stats.getMean());
			if (geomMean.isConnected())
				geomMean.send(stats.getGeometricMean());
			if (count.isConnected())
				count.send(new Double(stats.getN()));
			if (sum.isConnected())
				sum.send(stats.getSum());
			if (sumOfSquares.isConnected())
				sumOfSquares.send(stats.getSumsq());
			if (stdDev.isConnected())
				stdDev.send(stats.getStandardDeviation());
			if (variance.isConnected())
				variance.send(stats.getVariance());
		}
	}

}

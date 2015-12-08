package com.unifina.signalpath.filtering;

import java.util.ArrayList;
import java.util.Map;

import com.unifina.math.MODWT;
import com.unifina.math.WTFilter;
import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.IntegerParameter;
import com.unifina.signalpath.PossibleValue;
import com.unifina.signalpath.StringParameter;
import com.unifina.signalpath.TimeSeriesInput;
import com.unifina.signalpath.TimeSeriesOutput;
import com.unifina.utils.SlidingDoubleArray;

public class FastMODWT extends AbstractSignalPathModule {

	StringParameter wavelet = new WaveletParameter(this, "wavelet", "d4");
	IntegerParameter level = new IntegerParameter(this,"level",5);

	TimeSeriesInput input = new TimeSeriesInput(this,"in");

	TimeSeriesOutput smooth = new TimeSeriesOutput(this,"smooth");
	TimeSeriesOutput details = new TimeSeriesOutput(this,"details");
	TimeSeriesOutput energy = new TimeSeriesOutput(this,"energy");


	int N;

	double[] zeroV;
	SlidingDoubleArray values;

	SlidingDoubleArray W = null;

	WTFilter filter;

	@Override
	public void init() {
		addInput(wavelet);
		addInput(level);
		addInput(input);
		addOutput(details);
		addOutput(energy);
		addOutput(smooth);
	}

	@Override
	public void initialize() {
		super.initialize();

		filter = new WTFilter(wavelet.getValue(),true,level.getValue());
		N = filter.getH().length;

		zeroV = new double[N];
		values = new SlidingDoubleArray(N);
	}

	@Override
	public void sendOutput() {
		values.add(input.value);

		if (values.isFull()) {

			double S;
			double D;
			double E;

			if (W==null) {
				W = new SlidingDoubleArray(N);

				double[] V = values.getValues();
				double[][] WV = MODWT.stepForwardMODWT(V, N, 1, filter.getH(), filter.getG());

				for (int i=0;i<WV[0].length;i++)
					W.add(WV[0][i]);

				S = WV[1][N-1];
				E = Math.pow(WV[0][N-1],2);

				WV[1] = zeroV;
				double[] Dt = MODWT.stepInverseMODWT(WV, N, 1, filter.getH(), filter.getG());
				D = Dt[N-1];
			}
			else {
				double[] WV = MODWT.incrementMODWTForward(values.getValues(), N, filter.getH(), filter.getG());
				// Insert new W into rolling W
				W.add(WV[0]);

				S = WV[1];
				E = Math.pow(WV[0],2);

				D = MODWT.incrementMODWTInverse(W.getValues(), N, filter.getH(), filter.getG());
			}

			smooth.send(S);
			energy.send(E);
			details.send(D);

		}
	}

	@Override
	public void clearState() {
		values = new SlidingDoubleArray(N);
		W = null;
	}

	private static class WaveletParameter extends StringParameter {

		public WaveletParameter(AbstractSignalPathModule owner, String name, String defaultValue) {
			super(owner, name, defaultValue);
		}

		@Override
		public Map<String,Object> getConfiguration() {
			Map<String,Object> config = super.getConfiguration();
			ArrayList<PossibleValue> possibleValues = new ArrayList<>();
			config.put("possibleValues",possibleValues);
			possibleValues.add(new PossibleValue("Haar","haar"));
			possibleValues.add(new PossibleValue("Daub 4","d4"));
			possibleValues.add(new PossibleValue("Daub 6","d6"));
			possibleValues.add(new PossibleValue("Daub 8","d8"));
			possibleValues.add(new PossibleValue("Daub 10","d10"));
			possibleValues.add(new PossibleValue("Daub 20","d20"));
			possibleValues.add(new PossibleValue("Daub 30","d30"));
			return config;
		}
	}
}

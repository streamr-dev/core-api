package com.unifina.signalpath.modeling;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.DoubleParameter;
import com.unifina.signalpath.TimeSeriesInput;
import com.unifina.signalpath.TimeSeriesOutput;
import com.unifina.utils.MapTraversal;

public class ARIMA extends AbstractSignalPathModule {

	int AR = 0;
	int MA = 0;
	int I = 0;
	
	DiffChain diffChain = null;
	DiffChain intChain = null;
	
	List<DoubleParameter> ARparams = new ArrayList<>();
	List<DoubleParameter> MAparams = new ArrayList<>();
	
	List<Double> prevValues = new ArrayList<>();
	List<Double> prevErrors = new ArrayList<>();
	Double prevPrediction = null;
	
	TimeSeriesInput input = new TimeSeriesInput(this,"in");
	TimeSeriesOutput pred = new TimeSeriesOutput(this,"pred");
	
	@Override
	public void init() {
		addInput(input);
		addOutput(pred);
	}
	
	@Override
	public void initialize() {
		super.initialize();
		
		// Initial values for prediction errors are zero
		for (int i=0;i<MA;i++)
			prevErrors.add(0.0);
	}
	
	@Override
	public void sendOutput() {

		Double originalValue = input.value;

		// Diff the value I times if I>0
		Double diffValue;
		if (diffChain!=null) {
			diffValue = diffChain.diff(originalValue);
			if (diffValue==null)
				return;
		}
		else diffValue = originalValue;
		
		// Add new values (tail of list) and remove old ones (head of list)
		prevValues.add(diffValue);
		
		if (prevValues.size()>AR)
			prevValues.remove(0);
		
		if (prevPrediction!=null) {
			prevErrors.add(originalValue - prevPrediction);
			prevErrors.remove(0);
		}
		
		// Enough previous values to calculate AR?
		if (prevValues.size()==AR) {
			double prediction = 0;
			
			ListIterator<Double> iter = prevValues.listIterator();
			for (int i=0;i<AR;i++)
				prediction += iter.next() * ARparams.get(ARparams.size()-1-i).value;

			iter = prevErrors.listIterator();
			for (int i=0;i<MA;i++)
				prediction += iter.next() * MAparams.get(MAparams.size()-1-i).value;
				
			if (diffChain!=null)
				prediction = intChain.integrate(prediction);
			
			pred.send(prediction);
			prevPrediction = prediction;
		}
	}

	@Override
	public void clearState() {
		prevValues.clear();
		prevErrors.clear();
		prevPrediction = null;
		
		// Initial values for prediction errors are zero
		for (int i=0;i<MA;i++)
			prevErrors.add(0.0);

		// Re-calculate initial diff chain
		if (I>0) {
			diffChain = new DiffChain();
			DiffChain dc = diffChain;
			for (int i=1;i<I;i++) {
				dc.next = new DiffChain();
				dc.next.previous = dc;
				dc = dc.next;
			}
			intChain = dc;
		}
	}

	@Override
	public Map<String,Object> getConfiguration() {
		Map<String,Object> config = super.getConfiguration();
		Map<String,Object> options = new LinkedHashMap<>();
		config.put("options",options);
		
		Map<String,Object> ARoptions = new LinkedHashMap<>();
		options.put("AR(p)",ARoptions);
		ARoptions.put("value", AR);
		ARoptions.put("type","int");
		
		Map<String,Object> Ioptions = new LinkedHashMap<>();
		options.put("I(d)",Ioptions);
		Ioptions.put("value", I);
		Ioptions.put("type","int");
		
		Map<String,Object> MAoptions = new LinkedHashMap<>();
		options.put("MA(q)",MAoptions);
		MAoptions.put("value", MA);
		MAoptions.put("type","int");

		return config;
	}
	
	@Override
	public void onConfiguration(Map config) {
		super.onConfiguration(config);
		
		if (config.containsKey("options")) {
			// Add a DoubleParameter for each AR param
			
			AR = MapTraversal.getInteger(config, "options.AR(p).value");
			for (int p=1;p<=AR;p++) {
				DoubleParameter param = new DoubleParameter(this,"ar"+p,0D);
				addInput(param);
				ARparams.add(param);
			}

			if (MapTraversal.getInteger(config, "options.I(d).value")!=null) {
				I = MapTraversal.getInteger(config, "options.I(d).value");
				if (I>0) {
					diffChain = new DiffChain();
					DiffChain dc = diffChain;
					for (int i=1;i<I;i++) {
						dc.next = new DiffChain();
						dc.next.previous = dc;
						dc = dc.next;
					}
					intChain = dc;
				}
			}
			
			// Add a DoubleParameter for each MA param
			MA = MapTraversal.getInteger(config, "options.MA(q).value");
			for (int p=1;p<=MA;p++) {
				DoubleParameter param = new DoubleParameter(this,"ma"+p,0D);
				addInput(param);
				MAparams.add(param);
			}
		}
	}
	
	public class DiffChain implements Serializable {
		Double previousInput = null;
		Double input = null;
		Double output = null;
		DiffChain next = null;
		DiffChain previous = null;
		
		public Double diff(Double d) {
			DiffChain i = this;
			i.input = d;
			Double result = null;
			while (i!=null) {
				if (i.previousInput==null) {
					i.previousInput = i.input;
					return null;
				}
				else {
					i.output = i.input - i.previousInput;
					result = i.output;
					i.previousInput = i.input;
					i = i.next;
					if (i!=null)
						i.input = result;
				}
			}
			return result;
		}
		
		public Double integrate(Double d) {
			DiffChain i = this;
			i.output = d;
			Double result = null;
			while (i!=null) {
				i.input = i.input + i.output;
				result = i.input;
				i = i.previous;
				if (i!=null)
					i.output = result;
			}
			return result;
		}
	}
	
}

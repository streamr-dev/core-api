package com.unifina.signalpath.modeling

import java.util.Map;

import com.unifina.signalpath.DoubleParameter
import com.unifina.signalpath.GroovySignalPathModule
import com.unifina.signalpath.IntegerParameter;
import com.unifina.signalpath.TimeSeriesInput
import com.unifina.signalpath.TimeSeriesOutput
import com.unifina.utils.SlidingDoubleArray;

class ARMA extends GroovySignalPathModule {

	int AR = 0
	int MA = 0
	
	List<IntegerParameter> ARparams = []
	List<IntegerParameter> MAparams = []
	
	LinkedList prevValues = new LinkedList()
	LinkedList prevErrors = new LinkedList()
	Double prevPrediction = null
	
	TimeSeriesInput input = new TimeSeriesInput(this,"in")
	TimeSeriesOutput pred = new TimeSeriesOutput(this,"pred")
	
	@Override
	public void init() {
		addInput(input)
		addOutput(pred)
	}
	
	@Override
	public void initialize() {
		super.initialize();
		
		// Initial values for prediction errors are zero
		for (int i=0;i<MA;i++)
			prevErrors.add(0)
	}
	
	@Override
	public void sendOutput() {
		// Add new values (tail of list) and remove old ones (head of list)
		prevValues.add(input.value)
		
		if (prevValues.size()>AR)
			prevValues.removeFirst()
		
		if (prevPrediction!=null) {
			prevErrors.add(input.value - prevPrediction)
			prevErrors.removeFirst()
		}
		
		// Enough previous values to calculate AR?
		if (prevValues.size()==AR) {
			double prediction = 0
			
			ListIterator iter = prevValues.listIterator()
			for (int i=0;i<AR;i++)
				prediction += iter.next() * ARparams[ARparams.size()-1-i].value

			iter = prevErrors.listIterator()
			for (int i=0;i<MA;i++)
				prediction += iter.next() * MAparams[MAparams.size()-1-i].value
				
			pred.send(prediction)
			prevPrediction = prediction
		}
	}

	@Override
	public void clearState() {
		prevValues = new LinkedList()
		prevErrors = new LinkedList()
		prevPrediction = null
		
		// Initial values for prediction errors are zero
		for (int i=0;i<MA;i++)
			prevErrors.add(0)
	}

	@Override
	public Map<String,Object> getConfiguration() {
		Map<String,Object> config = super.getConfiguration();
		config.put("options",
			[
				"AR(p)": [value:AR,type:"int"], 
				"MA(q)": [value:MA,type:"int"]
			])
		return config
	}
	
	@Override
	public void onConfiguration(Map config) {
		super.onConfiguration(config);
		
		Map options = config["options"]
		
		if (options) {
			// Add a DoubleParameter for each AR param
			AR = options["AR(p)"].value
			for (int p=1;p<=AR;p++) {
				DoubleParameter param = new DoubleParameter(this,"ar"+p,0)
				addInput(param)
				ARparams << param
			}

			// Add a DoubleParameter for each MA param
			MA = options["MA(q)"].value
			for (int p=1;p<=MA;p++) {
				DoubleParameter param = new DoubleParameter(this,"ma"+p,0)
				addInput(param)
				MAparams << param
			}
		}
	}
	
}

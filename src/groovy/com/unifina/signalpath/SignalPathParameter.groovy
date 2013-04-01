package com.unifina.signalpath

import java.util.Map;

import groovy.transform.CompileStatic


class SignalPathParameter extends Parameter<SavedSignalPath> {

	Closure criteria
	
	public SignalPathParameter(AbstractSignalPathModule owner, String name) {
		super(owner, name, null, "SignalPath");
		def proj = {
			projections {
				property 'id', 'id'
				property 'name', 'name'
			}
		}

		// Combine closures
		criteria = proj << getCriteria()
	}
	
	def getCriteria() {
		return {
			or {
				gt("exportedStringParameters",0)
				gt("exportedBooleanParameters",0)
				gt("exportedIntegerParameters",0)
				gt("exportedDoubleParameters",0)
				gt("exportedSignalPathParameters",0)
				gt("exportedTimeSeriesInputs",0)
				gt("exportedTradesInputs",0)
				gt("exportedOrderbookInputs",0)
				gt("exportedTimeSeriesOutputs",0)
				gt("exportedTradesOutputs",0)
				gt("exportedOrderbookOutputs",0)
			}
		}
	}
	
	public Map<String,Object> getConfiguration() {
		Map<String,Object> config = super.getConfiguration()
		
		if (value!=null) {
			config.put("value", getValue().getId());
			config.put("defaultValue", getValue().getId());
		}

		Collection signalPaths = SavedSignalPath.createCriteria().list(criteria)
		config.put("possibleValues",signalPaths.collect {[value:it[0], name:it[1]]})
		return config
	}

	@Override
	SavedSignalPath parseValue(String s) {
		try {
			return SavedSignalPath.get(Long.parseLong(s));
		} catch (NumberFormatException e) {
			return null
		}
	}
	
}

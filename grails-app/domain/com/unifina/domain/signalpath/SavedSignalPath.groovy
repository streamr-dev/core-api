package com.unifina.domain.signalpath

import com.unifina.domain.security.SecUser;

class SavedSignalPath {
	
	Long id
	SecUser user
	String name
	String json
	
	Boolean hasExports
	
//	int exportedStringParameters
//	int exportedBooleanParameters
//	int exportedIntegerParameters
//	int exportedDoubleParameters
//	int exportedSignalPathParameters
//	int exportedTimeSeriesInputs
//	int exportedTradesInputs
//	int exportedOrderbookInputs
//	int exportedTimeSeriesOutputs
//	int exportedTradesOutputs
//	int exportedOrderbookOutputs
	
	Date dateCreated
	Date lastUpdated
	
    static constraints = {
    }
	
	static mapping = {
		json type: 'text'
	}

	@Override
	public String toString() {
		return "$name $id"
	}
		
}

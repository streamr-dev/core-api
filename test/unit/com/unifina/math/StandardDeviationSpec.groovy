package com.unifina.math

import spock.lang.Specification

class StandardDeviationSpec extends Specification {

    void testGetValue() {
		when:
        StandardDeviation sd = new StandardDeviation(3)
		sd.add(-1)
		sd.add(1)
		then:
		Math.round(sd.getValue()*10000)==Math.round(1.4142135623730951*10000)

		when:
		sd.add(1)
		then:
		Math.round(sd.getValue()*10000)==Math.round(1.1547*10000)

		when:
		sd.setLength(2)
		then:
		sd.getValue()==0
    }
	
	void testClear() {
		when:
		StandardDeviation sd = new StandardDeviation(3)
		sd.add(-1)
		sd.add(1)
		then:
		Math.round(sd.getValue()*10000)==Math.round(1.4142135623730951*10000)

		when:
		sd.clear()
		sd.add(1)
		sd.add(1)
		then:
		sd.getValue()==0
	}
}

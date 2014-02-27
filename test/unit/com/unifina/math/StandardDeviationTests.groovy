package com.unifina.math

import static org.junit.Assert.*
import grails.test.mixin.*
import grails.test.mixin.support.*

import org.junit.*

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
class StandardDeviationTests {

    void setUp() {
        // Setup logic here
    }

    void tearDown() {
        // Tear down logic here
    }

    void testGetValue() {
        StandardDeviation sd = new StandardDeviation(3)
		sd.add(-1)
		sd.add(1)
		assert Math.round(sd.getValue()*10000)==Math.round(1.4142135623730951*10000)
		sd.add(1)
		assert Math.round(sd.getValue()*10000)==Math.round(1.1547*10000)
		sd.setLength(2)
		assert sd.getValue()==0
    }
	
	void testClear() {
		StandardDeviation sd = new StandardDeviation(3)
		sd.add(-1)
		sd.add(1)
		assert Math.round(sd.getValue()*10000)==Math.round(1.4142135623730951*10000)
		sd.clear()
		sd.add(1)
		sd.add(1)
		assert sd.getValue()==0
	}
}

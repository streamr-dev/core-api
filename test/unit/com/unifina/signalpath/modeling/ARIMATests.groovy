package com.unifina.signalpath.modeling

import grails.test.mixin.*
import grails.test.mixin.support.*

import org.junit.*

import com.unifina.signalpath.modeling.ARIMA.DiffChain

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
class ARIMATests {

	DiffChain diffChain
	DiffChain intChain
	int I
	
    void setUp() {
        // Setup logic here
    }

    void tearDown() {
        // Tear down logic here
    }

    void testDiffChain1() {
		I = 1;
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
		
		assert diffChain.diff(1) == null
		assert diffChain.diff(2) == 1
		assert intChain.integrate(1) == 3
		assert diffChain.diff(1) == -1
		assert intChain.integrate(-1) == 0
    }
	
	void testDiffChain2() {
		I = 2;
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
		
		assert diffChain.diff(1) == null
		assert diffChain.diff(2) == null // 1st diff is 1 
		assert diffChain.diff(4) == 1 // 1st diff is 2, 2nd diff is 1
		assert intChain.integrate(1) == 7 // 2nd diff is predicted to be 1, 1st diff is 3, value should be 7
	}
}

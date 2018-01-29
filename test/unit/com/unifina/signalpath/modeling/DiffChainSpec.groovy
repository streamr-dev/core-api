package com.unifina.signalpath.modeling

import com.unifina.signalpath.modeling.ARIMA.DiffChain
import spock.lang.Specification

class DiffChainSpec extends Specification {

	DiffChain diffChain
	DiffChain intChain
	int I

    void "test example diffChain (1)"() {
		I = 1
		if (I > 0) {
			diffChain = new DiffChain()
			DiffChain dc = diffChain
			for (int i=1;i<I;i++) {
				dc.next = new DiffChain()
				dc.next.previous = dc
				dc = dc.next
			}
			intChain = dc
		}

		expect:
		diffChain.diff(1) == null
		diffChain.diff(2) == 1
		intChain.integrate(1) == 3
		diffChain.diff(1) == -1
		intChain.integrate(-1) == 0
    }
	
	void "test example diffChain (2)"() {
		I = 2
		if (I > 0) {
			diffChain = new DiffChain()
			DiffChain dc = diffChain
			for (int i=1; i<I; i++) {
				dc.next = new DiffChain()
				dc.next.previous = dc
				dc = dc.next
			}
			intChain = dc
		}

		expect:
		diffChain.diff(1) == null
		diffChain.diff(2) == null // 1st diff is 1
		diffChain.diff(4) == 1 // 1st diff is 2, 2nd diff is 1
		intChain.integrate(1) == 7 // 2nd diff is predicted to be 1, 1st diff is 3, value should be 7
	}
}

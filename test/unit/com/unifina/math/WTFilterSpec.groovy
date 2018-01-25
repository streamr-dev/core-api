package com.unifina.math

import spock.lang.Specification

class WTFilterSpec extends Specification {
    void "test something"() {
		when:
		WTFilter d41 = new WTFilter("d4",true,1)
		double[] h = d41.getH()
		double[] g = d41.getG()

		then:
		Math.abs(h[0]-(-0.09150635)) < 0.001
		Math.abs(h[1]-(-0.15849365)) < 0.001
		Math.abs(g[2]-(0.15849365)) < 0.001
		Math.abs(g[3]-(-0.09150635)) < 0.001

		when:
		WTFilter d43 = new WTFilter("d4",true,3)
		h = d43.getH()
		g = d43.getG()

		then:
		Math.abs(h[15]-(-0.149409029)) < 0.001
		Math.abs(h[21]-(-0.002859573)) < 0.001
		Math.abs(g[15]-(-0.0400340285)) < 0.001
		Math.abs(g[21]-(-0.0007662204)) < 0.001
    }
}

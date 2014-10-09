package com.unifina.math

import static org.junit.Assert.*
import grails.test.mixin.support.*

import spock.lang.Specification


class WeightedMovingAverageSpec extends Specification {

    def "equal weights must produce the same output as MovingAverage"() {
		MovingAverage ma = new MovingAverage(10)
		WeightedMovingAverage wma = new WeightedMovingAverage([1D] * 10)
		def values = (1..10)
		
		when: "values are added to moving averages"
			values.each {
				ma.add(it)
				wma.add(it)
			}
		
		then: "size must be 10 and moving averages must match"
			ma.size()==10
			wma.size()==10
			ma.getValue() == wma.getValue()
    }
	
	def "wma must be correct with non-equal weights"() {
		WeightedMovingAverage wma = new WeightedMovingAverage((1..10).collect {it.doubleValue()})
		def values = (1..10)
		
		when: "values are added"
			values.each { wma.add(it) }
			
		then: "output must be correct"
			wma.getValue() == 7
				
	}
	
	def "wma must be able to change weights and length on the fly"() {
		WeightedMovingAverage wma = new WeightedMovingAverage((1..10).collect {it.doubleValue()})
		def values = (1..10)
		
		when: "intial values are added"
			values.each { wma.add(it) }
			
		then: "output must be correct"
			wma.getValue() == 7
			
		when: "weights are changed"
			wma.setWeights((1..4).collect {it.doubleValue()})
			
		then: "output must be correct"
			wma.getValue() == 9
	}

}

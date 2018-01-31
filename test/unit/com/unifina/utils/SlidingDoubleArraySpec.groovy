package com.unifina.utils

import spock.lang.Specification

class SlidingDoubleArraySpec extends Specification {

    void "basic operation"() {
		when:
        SlidingDoubleArray arr = new SlidingDoubleArray(2)
		then:
		arr.size() == 0
		!arr.isFull()
		arr.getValues().length == 0

		when:
		arr.add(0)
		then:
		arr.size() == 1
		!arr.isFull()
		arr.getValues().length == 1
		arr.getValues()[0] == 0

		when:
		arr.add(1)
		then:
		arr.size() == 2
		arr.isFull()
		arr.getValues().length == 2
		arr.getValues()[0] == 0
		arr.getValues()[1] == 1

		when:
		arr.add(2)
		then:
		arr.size() == 2
		arr.isFull()
		arr.getValues().length == 2
		arr.getValues()[0] == 1
		arr.getValues()[1] == 2
    }
	
	void "change size"() {
		when:
		SlidingDoubleArray arr = new SlidingDoubleArray(3)
		arr.add(1)
		arr.add(2)
		arr.add(3)
		arr.add(4)
		then:
		arr.size()==3
		arr.maxSize()==3

		when: "resize down"
		arr.changeSize(2)
		then:
		arr.size()==2
		arr.maxSize()==2
		arr.getValues()[0] == 3
		arr.getValues()[1] == 4

		when: "resize up"
		arr.changeSize(4)
		then:
		arr.size()==2
		arr.maxSize()==4
		arr.getValues().length==2
		arr.getValues()[0] == 3
		arr.getValues()[1] == 4

		when:
		arr.add(5)
		arr.add(6)
		then:
		arr.size()==4
		arr.maxSize()==4
		arr.getValues().length==4
		arr.getValues()[0] == 3
		arr.getValues()[1] == 4
		arr.getValues()[2] == 5
		arr.getValues()[3] == 6

		when: "resize while not full"
		arr = new SlidingDoubleArray(3)
		arr.add(1)
		arr.changeSize(5)
		then:
		arr.getValues().length==1
		arr.getValues()[0] == 1

		when:
		arr.add(2)
		arr.changeSize(4)
		then:
		arr.getValues().length==2
		arr.getValues()[0] == 1
		arr.getValues()[1] == 2

		when:
		arr.changeSize(1)
		then:
		arr.getValues().length==1
		arr.getValues()[0] == 2
	}
	
	void "clear"() {
		when:
		SlidingDoubleArray arr = new SlidingDoubleArray(3)
		arr.add(1)
		arr.add(2)
		then:
		arr.size()==2

		when:
		arr.clear()
		then:
		arr.size()==0
		arr.getValues().length==0
	}
}

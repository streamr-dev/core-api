package com.unifina.utils

import static org.junit.Assert.*

import grails.test.mixin.*
import grails.test.mixin.support.*
import org.junit.*

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
class SlidingDoubleArrayTests {

    void setUp() {
        // Setup logic here
    }

    void tearDown() {
        // Tear down logic here
    }

    void testBasicOperation() {
        SlidingDoubleArray arr = new SlidingDoubleArray(2)
		assert arr.size() == 0
		assert !arr.isFull()
		assert arr.getValues().length == 0
		
		arr.add(0)
		assert arr.size() == 1
		assert !arr.isFull()
		assert arr.getValues().length == 1
		assert arr.getValues()[0] == 0
		
		arr.add(1)
		assert arr.size() == 2
		assert arr.isFull()
		assert arr.getValues().length == 2
		assert arr.getValues()[0] == 0
		assert arr.getValues()[1] == 1
		
		arr.add(2)
		assert arr.size() == 2
		assert arr.isFull()
		assert arr.getValues().length == 2
		assert arr.getValues()[0] == 1
		assert arr.getValues()[1] == 2
    }
	
	void testChangeSize() {
		SlidingDoubleArray arr = new SlidingDoubleArray(3)
		arr.add(1)
		arr.add(2)
		arr.add(3)
		arr.add(4)
		assert arr.size()==3
		assert arr.maxSize()==3
		
		// Resize down
		arr.changeSize(2)
		assert arr.size()==2
		assert arr.maxSize()==2
		assert arr.getValues()[0] == 3
		assert arr.getValues()[1] == 4
		
		// Resize up
		arr.changeSize(4)
		assert arr.size()==2
		assert arr.maxSize()==4
		assert arr.getValues().length==2
		assert arr.getValues()[0] == 3
		assert arr.getValues()[1] == 4
		arr.add(5)
		arr.add(6)
		assert arr.size()==4
		assert arr.maxSize()==4
		assert arr.getValues().length==4
		assert arr.getValues()[0] == 3
		assert arr.getValues()[1] == 4
		assert arr.getValues()[2] == 5
		assert arr.getValues()[3] == 6
		
		// Resize while not full
		arr = new SlidingDoubleArray(3)
		arr.add(1)
		arr.changeSize(5)
		assert arr.getValues().length==1
		assert arr.getValues()[0] == 1
		
		arr.add(2)
		arr.changeSize(4)
		assert arr.getValues().length==2
		assert arr.getValues()[0] == 1
		assert arr.getValues()[1] == 2
		
		arr.changeSize(1)
		assert arr.getValues().length==1
		assert arr.getValues()[0] == 2
	}
	
	void testClear() {
		SlidingDoubleArray arr = new SlidingDoubleArray(3)
		arr.add(1)
		arr.add(2)
		assert arr.size()==2
		arr.clear()
		assert arr.size()==0
		assert arr.getValues().length==0
	}
}

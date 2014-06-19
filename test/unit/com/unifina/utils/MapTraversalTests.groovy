package com.unifina.utils;

import grails.test.mixin.*

import org.junit.After
import org.junit.Before
import org.junit.Test

class MapTraversalTests {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testFlatten() {
		assert MapTraversal.flatten([:]).isEmpty()
		assert MapTraversal.flatten([foo: "foo", bar: "bar"]) == [foo: "foo", bar: "bar"]
		Map m = MapTraversal.flatten([map: [foo:"foo", xyz:"xyz"], bar: "bar"])
		assert m["map.foo"] == "foo"
		assert m["map.xyz"] == "xyz"
		assert m["bar"] == "bar"
		assert m.size() == 3
	}
	
}

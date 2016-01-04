package com.unifina.signalpath.custom

import com.unifina.signalpath.Input
import com.unifina.signalpath.Output
import spock.lang.Specification

class StoredEndpointFieldsSpec extends Specification {

	private static class A {
		private int a;
		private Input b;
		protected Output c;
		public String d;

		A(int a, Input b, Output c, String d) {
			this.a = a
			this.b = b
			this.c = c
			this.d = d
		}
	}

	A instance
	Input input
	Output output

	def setup() {
		input = new Input(null, "", "")
		output = new Output(null, "", "")
		instance = new A(0, input, output, "")
	}

	void "clearAndCollect() nulls fields that are or inherit from Endpoint"() {
		when:
		StoredEndpointFields.clearAndCollect(instance)

		then:
		instance.b == null
		instance.c == null
	}

	void "clearAndCollect() keeps other fields intact"() {
		when:
		StoredEndpointFields.clearAndCollect(instance)

		then:
		instance.a == 0
		instance.d == ""
	}

	void "method restoreFields() restores endpoint fields"() {
		A a = new A(1, null, null, "test")
		StoredEndpointFields sef = StoredEndpointFields.clearAndCollect(instance)

		when:
		sef.restoreFields(a)

		then:
		a.b == input
		a.c == output
	}

	void "method restoreFields() keeps other fields intact"() {
		A a = new A(1, null, null, "test")
		StoredEndpointFields sef = StoredEndpointFields.clearAndCollect(instance)

		when:
		sef.restoreFields(a)

		then:
		a.a == 1
		a.d == "test"
	}
}

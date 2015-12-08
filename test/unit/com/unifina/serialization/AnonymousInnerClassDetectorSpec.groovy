package com.unifina.serialization

import spock.lang.Specification

class AnonymousInnerClassDetectorSpec extends Specification {
	def AnonymousInnerClassDetector detector = new AnonymousInnerClassDetector()

	class A {
		public String a = "a"
		private Object b = null; // Null-field test
	}
	def "returns false given object with no inner classes"() {
		expect:
		!detector.detect(new A())
	}

	class B {
		NamedInnerClass instance = new NamedInnerClass()
		static class NamedInnerClass {}
	}
	def "returns false given object with named inner classes"() {
		expect:
		!detector.detect(new B())
	}

	class C {
		private def instance = new Object(){}
	}
	def "returns true given object with anonymous inner classes"() {
		expect:
		detector.detect(new C())
	}


	class D {
		private transient def instance = new Object(){}
	}
	def "returns false given object with anonymous inner classes but that is transient"() {
		expect:
		!detector.detect(new D())
	}

	class E {
		static def instance = new Object(){}
	}
	def "returns false given object with anonymous inner classes but as static field"() {
		expect:
		!detector.detect(new E())
	}
}

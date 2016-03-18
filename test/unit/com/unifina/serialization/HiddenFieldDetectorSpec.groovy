package com.unifina.serialization

import spock.lang.Specification

class HiddenFieldDetectorSpec extends Specification {

	static class Base {
		String a = "string"
		boolean b = true
		public Map<String, String> c = [:]
	}

	static class SyntheticFieldsHidden extends Base {
		// Groovy automatically injects synthetic fields (e.g. "metaClass") that are hidden by subclasses
	}

	static class NonHiding extends Base {
		private String d = "d"
		protected def e = new Object()
	}

	static class StaticHiding extends Base {
		static String a = "yo"
		static boolean b = true
		static def c = new Object()
	}

	static class HidingOneField extends Base {
		String a = "hiding"
		private boolean d = true
	}

	static class HidingThreeFields extends Base {
		private def a = new Object()
		boolean b = false
		public Double c = 0.0
		private boolean d = true
	}

	static class NonHiding2 extends NonHiding {}
	static class HidingDeep extends NonHiding2 {
		public def a = new Object()
		boolean d = true
	}

	static class Middle extends HidingOneField {
		private def a = "yo"
		protected def b = false
		def d = "DDD"
		def e = 1
	}
	static class Last extends Middle {
		private def a = new Object()
		def c = true
		def e = 6
		def f = "final"
	}


	def "it doesn't detect hidden fields in some of Java's built-in classes"() {
		when:
		def objectDetector = new HiddenFieldDetector(Object)
		def stringDetector = new HiddenFieldDetector(String)

		then:
		!objectDetector.anyHiddenFields()
		!stringDetector.anyHiddenFields()
		objectDetector.hiddenFields() == [:]
		stringDetector.hiddenFields() == [:]
	}

	def "it doesn't detect hidden fields when Groovy's synthetic field shadowing occurs"() {
		expect:
		!new HiddenFieldDetector(SyntheticFieldsHidden).anyHiddenFields()
	}

	def "it doesn't detect field hiding when none in present (false positive)"() {
		expect:
		!new HiddenFieldDetector(Base).anyHiddenFields()
		!new HiddenFieldDetector(NonHiding).anyHiddenFields()
	}

	def "it ignores static fields"() {
		expect:
		!new HiddenFieldDetector(StaticHiding).anyHiddenFields()
	}

	def "it detects a field hidden by an immediate subclass"() {
		when:
		def detector = new HiddenFieldDetector(HidingOneField)

		then:
		detector.anyHiddenFields()
		detector.hiddenFields() == ["a": [HidingOneField, Base]]
	}

	def "it detects fields hidden by an immediate subclass"() {
		when:
		def detector = new HiddenFieldDetector(HidingThreeFields)

		then:
		detector.anyHiddenFields()
		detector.hiddenFields() == [
			"a": [HidingThreeFields, Base],
			"b": [HidingThreeFields, Base],
			"c": [HidingThreeFields, Base],
		]
	}

	def "it detects fields hidden by a descendant subclass"() {
		when:
		def detector = new HiddenFieldDetector(HidingDeep)

		then:
		detector.anyHiddenFields()
		detector.hiddenFields() == [
			"a": [HidingDeep, Base],
			"d": [HidingDeep, NonHiding],
		]
	}

	def "it detects fields hidden by multiple descendant subclasses"() {
		when:
		def detector = new HiddenFieldDetector(Last)

		then:
		detector.anyHiddenFields()
		detector.hiddenFields() == [
			"a": [Last, Middle, HidingOneField, Base],
			"b": [Middle, Base],
			"c": [Last, Base],
			"d": [Middle, HidingOneField],
			"e": [Last, Middle]
		]
	}
}

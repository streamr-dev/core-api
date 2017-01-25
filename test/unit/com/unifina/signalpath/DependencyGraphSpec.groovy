package com.unifina.signalpath

import spock.lang.Specification

class DependencyGraphSpec extends Specification {
	def graph = new DependencyGraph<String>()

	void "topological sort works on empty graph"() {
		expect:
		graph.topologicalSort() == []
	}

	void "topological sort works on single node graph"() {
		when:
		graph.put("a", [] as Set, [] as Set)

		then:
		graph.topologicalSort() == ["a"]
	}

	void "topological sort works on totally disconnected graph"() {
		when:
		graph.put("a", [] as Set, [] as Set)
		graph.put("b", [] as Set, [] as Set)
		graph.put("c", [] as Set, [] as Set)
		graph.put("d", [] as Set, [] as Set)
		graph.put("e", [] as Set, [] as Set)
		graph.put("f", [] as Set, [] as Set)

		then:
		graph.topologicalSort() as Set == ["a", "b", "c", "d", "e", "f"] as Set
	}

	void "topological sort works on graph with A depending on B and C"() {
		when:
		graph.put("a", ["c", "b"] as Set, [] as Set)
		graph.put("b", [] as Set, ["a"] as Set)
		graph.put("c", [] as Set, ["a"] as Set)

		then:
		graph.topologicalSort() == ["c", "b", "a"]
	}

	void "topological sort works on graph with A depending on B and C, and B depending on C"() {
		when:
		graph.put("a", ["c", "b"] as Set, [] as Set)
		graph.put("b", ["c"] as Set, ["a"] as Set)
		graph.put("c", [] as Set, ["b", "a"] as Set)

		then:
		graph.topologicalSort() == ["c", "b", "a"]
	}

	void "topological sort works on rather involved graph"() {
		when:
		graph.put("a", []              as Set, ["b", "c", "e", "h"] as Set)
		graph.put("b", ["a"]           as Set, ["c","e"]            as Set)
		graph.put("c", ["a", "b"]      as Set, ["d", "e", "g"]      as Set)
		graph.put("d", ["c", "e"]      as Set, ["f", "g"]           as Set)
		graph.put("e", ["a", "b", "c"] as Set, ["d", "f", "g"]      as Set)
		graph.put("f", ["d", "e"]      as Set, []                   as Set)
		graph.put("g", ["c", "d", "e"] as Set, ["h"]                as Set)
		graph.put("h", ["a", "g"]      as Set, ["l"]                as Set)
		graph.put("i", ["l"]           as Set, ["j", "k"]           as Set)
		graph.put("j", ["i"]           as Set, ["k"]                as Set)
		graph.put("k", ["i", "j"]      as Set, []                   as Set)
		graph.put("l", ["h"]           as Set, ["i"]                as Set)

		then:
		graph.topologicalSort() == ["a", "b", "c", "e", "d", "g", "h", "l", "i", "j", "k", "f"]
	}

	// TODO: test error cases
}

package com.unifina.utils;

import spock.lang.Specification

class MapTraversalSpec extends Specification {

	def flatten() {
		Map m = MapTraversal.flatten([map: [foo:"foo", xyz:"xyz"], bar: "bar"])

		expect:
		MapTraversal.flatten([:]).isEmpty()
		MapTraversal.flatten([foo: "foo", bar: "bar"]) == [foo: "foo", bar: "bar"]
		m["map.foo"] == "foo"
		m["map.xyz"] == "xyz"
		m["bar"] == "bar"
		m.size() == 3
	}

	def getProperty() {
		Map m = [name: "Adventure Time", episodes: 10,
				 cast: [pals: [[name: "Jake", species: "Dog", selected: true], [name: "Finn", species: "Human"]], type: "bff"]]

		expect: "typed getters"
		MapTraversal.getInt(m, "episodes") == 10
		MapTraversal.getInt(m, "foo", 123) == 123
		MapTraversal.getString(m, "cast.type") == "bff"
		MapTraversal.getString(m, "cast.pals[0].species") == "Dog"
		MapTraversal.getBoolean(m, "cast.pals[0].selected")
		!MapTraversal.getBoolean(m, "cast.pals[1].selected")

		and: "nonexistent items return null"
		MapTraversal.getProperty(m, "cast.pals[3]") == null
		MapTraversal.getProperty(m, "cast.size") == null
		MapTraversal.getProperty(m, "cast.pals[1].selected") == null

		and: "malformed queries return null"
		MapTraversal.getProperty(m, "foo") == null
		MapTraversal.getProperty(m, "cast.") == null
		MapTraversal.getProperty(m, "cast..pals") == null
		MapTraversal.getProperty(m, "cast.pals[") == null
		MapTraversal.getProperty(m, "cast.pals[zero]") == null
		MapTraversal.getProperty(m, "cast.pals[[0]]") == null
		MapTraversal.getProperty(m, "cast.pals[2-1]") == null
		MapTraversal.getProperty(m, "cast.pals[1.0]") == null
		MapTraversal.getProperty(m, ".") == null
		MapTraversal.getProperty(m, "[1]") == null
		MapTraversal.getProperty(m, ".[1]") == null
		MapTraversal.getProperty(m, "name.length") == null
	}
	
}

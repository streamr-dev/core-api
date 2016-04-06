package com.unifina.signalpath.map

import groovy.transform.CompileStatic
import org.apache.commons.lang3.RandomStringUtils
import spock.lang.Specification

class ValueSortedMapSpec extends Specification {
	def seededRandom = new Random(1234)
	Map valueSortedMap = new ValueSortedMap<String, Double>(true)

	void "values are maintained correctly"() {
		Map<String, Double> expected = [:]

		when:
		25000.times {

			// Put random letter with random value
			def letter = generateLetter()
			def value = seededRandom.nextDouble() * 100 - 50 // r \in [-50, 50]
			expected[letter] = expected.get(letter, 0) + value
			valueSortedMap.put(letter, (valueSortedMap.get(letter) ?: 0d) + value)

			// Remove random letter 10% of the time
			if (seededRandom.nextDouble() < 0.1) {
				letter = generateLetter()
				valueSortedMap.remove(letter)
				expected.remove(letter)
			}

			// Assert equality
			assert valueSortedMap == expected
		}

		then:
		valueSortedMap == expected
	}

	@CompileStatic
	private static String generateLetter() {
		return RandomStringUtils.random(1, ('A'..'Z').join("").toCharArray())
	}
}

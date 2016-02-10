package com.unifina

class TestHelper {
	static boolean waitFor(throwOnFail = false, numOfTries = 1000, sleepBetweenTries = 10, Closure<Boolean> condition) {
		for (int j = 0; j < numOfTries; ++j) {
			if (condition.call()) {
				return true
			}
			sleep(sleepBetweenTries)
		}

		if (throwOnFail) {
			throw new RuntimeException("Test failed")
		} else {
			return false
		}
	}
}

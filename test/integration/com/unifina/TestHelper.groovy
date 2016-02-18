package com.unifina

class TestHelper {
	static boolean waitFor(Map args=[:], Closure<Boolean> condition) {
		return waitFor(
			args.throw ?: args.throwIfTriesExceeded ?: true,
			args.tries ?: args.numOfTries ?: 1000,
			args.sleep ?: args.sleepMillis ?: args.sleepBetweenTries ?: 10,
			condition
		)
	}
	static boolean waitFor(throwOnFail, numOfTries, sleepBetweenTries, Closure<Boolean> condition) {
		for (int j = 0; j < numOfTries; ++j) {
			if (condition.call()) {
				return true
			}
			sleep(sleepBetweenTries)
		}

		if (throwOnFail) {
			throw new RuntimeException("Test failed, tried $numOfTries times with $sleepBetweenTries millisecond intervals")
		} else {
			return false
		}
	}
}

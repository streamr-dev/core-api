package com.streamr.core.utils

/**
 * ThreadUtil's sleep methods handle <code>InterruptedException</code>.
 *
 * @see https://stackoverflow.com/questions/1087475/when-does-javas-thread-sleep-throw-interruptedexception
 */
class ThreadUtil {
	static void sleep(long millis) {
		try {
			Thread.sleep(millis)
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt()
		}
	}

	static void sleep(long millis, int nanos) {
		try {
			Thread.sleep(millis, nanos)
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt()
		}
	}
}

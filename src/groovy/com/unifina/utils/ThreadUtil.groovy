package com.unifina.utils

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

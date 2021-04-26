package com.unifina.domain

enum ExampleType {
	// NOT_SET marks stream that is not suitable for sharing or copying.
	NOT_SET(0),
	// SHARE marks stream as a shareable example resource.
	SHARE(1),
	// COPY marks stream as a copyable example resource.
	COPY(2)

	private final Integer value

	ExampleType(Integer value) {
		this.value = value
	}
}

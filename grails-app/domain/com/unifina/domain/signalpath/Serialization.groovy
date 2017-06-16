package com.unifina.domain.signalpath

class Serialization {
	Date date
	byte[] bytes

	static belongsTo = [canvas: Canvas]

	static mapping = {
		bytes sqlType: "longblob"
	}
}

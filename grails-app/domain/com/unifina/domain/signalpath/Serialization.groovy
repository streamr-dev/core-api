package com.unifina.domain.signalpath

import grails.persistence.Entity

@Entity
class Serialization {
	Date date
	byte[] bytes

	static belongsTo = [canvas: Canvas]

	static mapping = {
		bytes sqlType: "longblob"
	}
}

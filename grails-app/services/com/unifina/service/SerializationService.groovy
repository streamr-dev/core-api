package com.unifina.service

import com.unifina.serialization.SerializerImpl
import grails.transaction.Transactional

class SerializationService {

	def serializer = new SerializerImpl()

	def serialize(Object object, String filename) {
		serializer.serializeToFile(object, filename)
	}

	def deserialize(String filename) {
		serializer.deserializeFromFile(filename)
	}
}

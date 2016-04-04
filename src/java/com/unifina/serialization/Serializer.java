package com.unifina.serialization;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;

public interface Serializer {
	byte[] serializeToByteArray(Object object) throws SerializationException;
	void serializeToFile(Object object, String filename) throws SerializationException, FileNotFoundException;
	void serialize(Object object, OutputStream out) throws SerializationException;

	Object deserializeFromByteArray(byte[] bytes) throws SerializationException;
	Object deserializeFromFile(String filename) throws SerializationException, FileNotFoundException;
	Object deserialize(InputStream in) throws SerializationException;

}

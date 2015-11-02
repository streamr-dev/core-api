package com.unifina.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface Serializer {
	void serialize(Object object, OutputStream out) throws IOException;
	Object deserialize(InputStream in) throws IOException, ClassNotFoundException;

	void serializeToFile(Object object, String filename) throws IOException;
	Object deserializeFromFile(String filename) throws IOException, ClassNotFoundException;
}

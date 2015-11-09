package com.unifina.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface Serializer {
	String serializeToString(Object object) throws IOException;
	void serializeToFile(Object object, String filename) throws IOException;
	void serialize(Object object, OutputStream out) throws IOException;

	Object deserializeFromString(String string) throws IOException, ClassNotFoundException;
	Object deserializeFromFile(String filename) throws IOException, ClassNotFoundException;
	Object deserialize(InputStream in) throws IOException, ClassNotFoundException;

}

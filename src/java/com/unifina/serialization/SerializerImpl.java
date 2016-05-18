package com.unifina.serialization;

import com.unifina.domain.data.Feed;
import com.unifina.domain.data.Stream;
import com.unifina.domain.security.SecUser;
import com.unifina.domain.signalpath.Canvas;
import com.unifina.domain.signalpath.Module;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.log4j.Logger;
import org.codehaus.groovy.grails.web.json.JSONArray;
import org.codehaus.groovy.grails.web.json.JSONObject;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.nustaq.serialization.*;
import org.nustaq.serialization.coders.Unknown;
import org.nustaq.serialization.serializers.FSTBigNumberSerializers;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Pattern;

public class SerializerImpl implements Serializer {

	private static final Logger logger = Logger.getLogger(SerializerImpl.class);

	private final FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();

	public SerializerImpl() {
		conf.registerSerializer(PearsonsCorrelation.class, new PearsonsCorrelationSerializer(), false);
		conf.registerSerializer(Pattern.class, new PatternSerializer(), false);
		conf.registerSerializer(DescriptiveStatistics.class, new DescriptiveStatisticsSerializer(), false);
		conf.registerSerializer(JSONObject.class, new JSONObjectSerializer(), true);
		conf.registerSerializer(JSONArray.class, new JSONArraySerializer(), true);
		conf.registerSerializer(JSONObject.Null.class, new JSONNullSerializer(), true);

		conf.registerSerializer(Canvas.class, new DomainClassSerializer(), false);
		conf.registerSerializer(Feed.class, new DomainClassSerializer(), false);
		conf.registerSerializer(Module.class, new DomainClassSerializer(), false);
		conf.registerSerializer(SecUser.class, new DomainClassSerializer(), false);
		conf.registerSerializer(Stream.class, new DomainClassSerializer(), false);
	}

	public SerializerImpl(ClassLoader classLoader) {
		this();
		conf.setClassLoader(classLoader);
	}

	@Override
	public byte[] serializeToByteArray(Object object) throws SerializationException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		serialize(object, out);
		return out.toByteArray();

	}

	@Override
	public void serializeToFile(Object object, String filename) throws SerializationException, FileNotFoundException {
		serialize(object, new FileOutputStream(filename));
	}

	@Override
	public void serialize(Object object, OutputStream out) throws SerializationException {
		FSTObjectOutput fstOutput = conf.getObjectOutput(out);
		try {
			fstOutput.writeObject(object);
			fstOutput.flush();
			fstOutput.close();
		} catch (IOException e) {
			throw new SerializationException("Failed to serialize ", e);
		}
	}

	@Override
	public Object deserializeFromByteArray(byte[] bytes) throws SerializationException {
		return deserialize(new ByteArrayInputStream(bytes));
	}

	@Override
	public Object deserializeFromFile(String filename) throws SerializationException, FileNotFoundException {
		return deserialize(new FileInputStream(filename));
	}

	@Override
	public Object deserialize(InputStream in) throws SerializationException {
		try {
			FSTObjectInput fstInput = conf.getObjectInput(in);
			Object object = fstInput.readObject();
			in.close();
			if (object instanceof Unknown) {
				throw new ClassNotFoundException("Deserialization failed");
			}
			return object;
		} catch (ClassNotFoundException | IOException | NullPointerException e) {
			throw new SerializationException("Failed to deserialize", e);
		}
	}


	// Custom serializers below
	private static class DescriptiveStatisticsSerializer extends FSTBasicObjectSerializer {

		@Override
		public void writeObject(FSTObjectOutput out,
								Object toWrite,
								FSTClazzInfo clzInfo,
								FSTClazzInfo.FSTFieldInfo referencedBy,
								int streamPosition) throws IOException {
			DescriptiveStatistics descriptiveStatistics = (DescriptiveStatistics) toWrite;
			out.writeObject(descriptiveStatistics.getValues());
			out.writeInt(descriptiveStatistics.getWindowSize());
		}

		@Override
		public Object instantiate(Class objectClass,
								  FSTObjectInput in,
								  FSTClazzInfo serializationInfo,
								  FSTClazzInfo.FSTFieldInfo referencee,
								  int streamPosition) throws Exception {
			DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics((double[]) in.readObject());
			descriptiveStatistics.setWindowSize(in.readInt());
			return descriptiveStatistics;
		}
	}

	private static class PatternSerializer extends FSTBasicObjectSerializer {

		@Override
		public void writeObject(FSTObjectOutput out,
								Object toWrite,
								FSTClazzInfo clzInfo,
								FSTClazzInfo.FSTFieldInfo referencedBy,
								int streamPosition) throws IOException {
			Pattern pattern = (Pattern) toWrite;
			out.writeStringUTF(pattern.pattern());
			out.writeInt(pattern.flags());
		}

		@Override
		public Object instantiate(Class objectClass,
								  FSTObjectInput in,
								  FSTClazzInfo serializationInfo,
								  FSTClazzInfo.FSTFieldInfo referencee,
								  int streamPosition) throws Exception {
			return Pattern.compile(in.readStringUTF(), in.readInt());
		}
	}

	private static class PearsonsCorrelationSerializer extends FSTBasicObjectSerializer {

		@Override
		public void writeObject(FSTObjectOutput out,
								Object toWrite,
								FSTClazzInfo clzInfo,
								FSTClazzInfo.FSTFieldInfo referencedBy,
								int streamPosition) throws IOException {
			org.apache.commons.math3.stat.correlation.PearsonsCorrelation pearsonsCorrelation =
					(org.apache.commons.math3.stat.correlation.PearsonsCorrelation) toWrite;

			if (pearsonsCorrelation.getCorrelationMatrix() == null) {
				out.writeObject(null);
			} else {
				out.writeObject(pearsonsCorrelation.getCorrelationMatrix().getData());
			}
		}

		@Override
		public Object instantiate(Class objectClass,
								  FSTObjectInput in,
								  FSTClazzInfo serializationInfo,
								  FSTClazzInfo.FSTFieldInfo referencee,
								  int streamPosition) throws Exception {
			double[][] matrix = (double[][]) in.readObject();
			if (matrix == null) {
				return new org.apache.commons.math3.stat.correlation.PearsonsCorrelation();
			} else {
				return new org.apache.commons.math3.stat.correlation.PearsonsCorrelation(matrix);
			}
		}
	}

	private static class JSONObjectSerializer extends FSTBasicObjectSerializer {
		@Override
		public void writeObject(FSTObjectOutput out,
								Object toWrite,
								FSTClazzInfo clzInfo,
								FSTClazzInfo.FSTFieldInfo referencedBy,
								int streamPosition) throws IOException {
			out.writeUTF(toWrite.toString());
		}

		@Override
		public Object instantiate(Class objectClass,
								  FSTObjectInput in,
								  FSTClazzInfo serializationInfo,
								  FSTClazzInfo.FSTFieldInfo referencee,
								  int streamPosition) throws Exception {
			return new JSONObject(in.readStringUTF());
		}
	}

	private static class JSONNullSerializer extends FSTBasicObjectSerializer {
		@Override
		public void writeObject(FSTObjectOutput out,
								Object toWrite,
								FSTClazzInfo clzInfo,
								FSTClazzInfo.FSTFieldInfo referencedBy,
								int streamPosition) throws IOException {
			out.writeUTF("null");
		}

		@Override
		public Object instantiate(Class objectClass,
								  FSTObjectInput in,
								  FSTClazzInfo serializationInfo,
								  FSTClazzInfo.FSTFieldInfo referencee,
								  int streamPosition) throws Exception {
			in.readStringUTF();
			return JSONObject.NULL;
		}
	}

	private static class JSONArraySerializer extends FSTBasicObjectSerializer {

		@Override
		public void writeObject(FSTObjectOutput out,
								Object toWrite,
								FSTClazzInfo clzInfo,
								FSTClazzInfo.FSTFieldInfo referencedBy,
								int streamPosition) throws IOException {
			out.writeUTF(toWrite.toString());
		}

		@Override
		public Object instantiate(Class objectClass,
								  FSTObjectInput in,
								  FSTClazzInfo serializationInfo,
								  FSTClazzInfo.FSTFieldInfo referencee,
								  int streamPosition) throws Exception {
			return new JSONArray(in.readStringUTF());
		}
	}

	private static class DomainClassSerializer extends FSTBasicObjectSerializer {

		@Override
		public void writeObject(FSTObjectOutput out,
								Object toWrite,
								FSTClazzInfo clzInfo,
								FSTClazzInfo.FSTFieldInfo referencedBy,
								int streamPosition) throws IOException {
			try {
				Method method = toWrite.getClass().getMethod("getId");
				Object id = method.invoke(toWrite);
				out.writeObject(id);
			} catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}

		@Override
		public Object instantiate(Class objectClass,
										   FSTObjectInput in,
										   FSTClazzInfo serializationInfo,
										   FSTClazzInfo.FSTFieldInfo referencee,
										   int streamPosition) throws Exception {
			return InvokerHelper.invokeMethod(objectClass, "get", in.readObject());
		}
	}
}
package com.unifina.serialization;

import com.unifina.domain.data.Feed;
import com.unifina.domain.security.SecUser;
import com.unifina.domain.signalpath.Module;
import com.unifina.domain.signalpath.RunningSignalPath;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.codehaus.groovy.grails.web.json.JSONObject;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.grails.datastore.gorm.GormStaticApi;
import org.nustaq.serialization.*;
import org.nustaq.serialization.serializers.FSTBigNumberSerializers;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Pattern;

public class SerializerImpl implements Serializer {

	private static final FSTConfiguration conf = FSTConfiguration.createJsonConfiguration();

	static {
		//((JsonFactory) conf.getCoderSpecific()).configure(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS, true);
		conf.registerSerializer(PearsonsCorrelation.class, new PearsonsCorrelationSerializer(), false);
		conf.registerSerializer(Pattern.class, new PatternSerializer(), false);
		conf.registerSerializer(DescriptiveStatistics.class, new DescriptiveStatisticsSerializer(), false);
		conf.registerSerializer(Double.class, new DoubleSerializer(), true);
		conf.registerSerializer(SpecialValueDouble.class, new SpecialValueDoubleSerializer(), true);
		conf.registerSerializer(JSONObject.class, new JSONObjectSerializer(), true);
		conf.registerSerializer(Feed.class, new DomainClassSerializer(), true);
		conf.registerSerializer(Module.class, new DomainClassSerializer(), true);
		conf.registerSerializer(RunningSignalPath.class, new DomainClassSerializer(), true);
		conf.registerSerializer(SecUser.class, new DomainClassSerializer(), true);
	}

	public SerializerImpl() {}

	@Override
	public String serializeToString(Object object) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		serialize(object, out);
		return out.toString("UTF-8");
	}

	@Override
	public void serializeToFile(Object object, String filename) throws IOException {
		serialize(object, new FileOutputStream(filename));
	}

	@Override
	public void serialize(Object object, OutputStream out) throws IOException {
		FSTObjectOutput fstOutput = conf.getObjectOutput(out);
		fstOutput.writeObject(object);
		fstOutput.flush();
		fstOutput.close();
	}

	@Override
	public Object deserializeFromString(String string) throws IOException, ClassNotFoundException {
		return deserialize(new ByteArrayInputStream(string.getBytes()));
	}

	@Override
	public Object deserializeFromFile(String filename) throws IOException, ClassNotFoundException {
		return deserialize(new FileInputStream(filename));
	}

	@Override
	public Object deserialize(InputStream in) throws IOException, ClassNotFoundException {
		FSTObjectInput fstInput = SerializerImpl.conf.getObjectInput(in);
		Object object = fstInput.readObject();
		in.close();
		return object;
	}


	// Custom serializers below

	// TODO: hack for getting NaN Double values serialized, definitely not optimal
	private static class SpecialValueDoubleSerializer extends FSTBasicObjectSerializer {

		@Override
		public void writeObject(FSTObjectOutput out,
								Object toWrite,
								FSTClazzInfo clzInfo,
								FSTClazzInfo.FSTFieldInfo referencedBy,
								int streamPosition) throws IOException {
			out.writeStringUTF(((SpecialValueDouble)toWrite).d);
		}

		@Override
		public Object instantiate(Class objectClass,
								  FSTObjectInput in,
								  FSTClazzInfo serializationInfo,
								  FSTClazzInfo.FSTFieldInfo referencee,
								  int streamPosition) throws Exception {
			return Double.parseDouble(in.readStringUTF());
		}
	}

	// TODO: hack for getting NaN Double values serialized, definitely not optimal
	static class DoubleSerializer extends FSTBigNumberSerializers.FSTDoubleSerializer {
		@Override
		public void writeObject(FSTObjectOutput out,
								Object toWrite,
								FSTClazzInfo clzInfo,
								FSTClazzInfo.FSTFieldInfo referencedBy,
								int streamPosition) throws IOException {
			Double d = (Double) toWrite;
			if (d.isNaN() || d.isInfinite()) {
				out.writeObjectInternal(new SpecialValueDouble(d), conf.getClazzInfo(SpecialValueDouble.class));
			} else {
				super.writeObject(out, toWrite, clzInfo, referencedBy, streamPosition);
			}
		}
	}

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

	private static class DomainClassSerializer extends FSTBasicObjectSerializer {

		@Override
		public void writeObject(FSTObjectOutput out,
								Object toWrite,
								FSTClazzInfo clzInfo,
								FSTClazzInfo.FSTFieldInfo referencedBy,
								int streamPosition) throws IOException {
			try {
				Method method = toWrite.getClass().getMethod("getId");
				Long id = (Long) method.invoke(toWrite);
				out.writeLong(id);
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
			return InvokerHelper.invokeMethod(objectClass, "get", in.readLong());
		}
	}
}

package com.unifina.serialization;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.nustaq.serialization.*;
import org.nustaq.serialization.serializers.FSTBigNumberSerializers;

import java.io.*;
import java.util.regex.Pattern;

public class Serializer {

    private static final FSTConfiguration conf = FSTConfiguration.createJsonConfiguration();

    static {
		//((JsonFactory) conf.getCoderSpecific()).configure(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS, true);
		conf.registerSerializer(PearsonsCorrelation.class, new PearsonsCorrelationSerializer(), false);
		conf.registerSerializer(Pattern.class, new PatternSerializer(), false);
		conf.registerSerializer(DescriptiveStatistics.class, new DescriptiveStatisticsSerializer(), false);
		conf.registerSerializer(Double.class, new DoubleSerializer(), true);
		conf.registerSerializer(SpecialValueDouble.class, new SpecialValueDoubleSerializer(), true);
	}

    public static void serializeToFile(Object object, String filename) throws IOException {
        serialize(object, new FileOutputStream(filename));
    }

    public static void serialize(Object object, OutputStream out) throws IOException {
        FSTObjectOutput fstOutput = conf.getObjectOutput(out);
        fstOutput.writeObject(object);
        fstOutput.flush();
        fstOutput.close();
    }

    public static Object deserializeFromFile(String filename) throws IOException, ClassNotFoundException {
        return deserialize(new FileInputStream(filename));
    }

    public static Object deserialize(InputStream in) throws IOException, ClassNotFoundException {
        FSTObjectInput fstInput = Serializer.conf.getObjectInput(in);
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
}

package com.unifina.serialization;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.nustaq.serialization.*;

import java.io.*;
import java.util.regex.Pattern;

public class Serializer {

    private static final FSTConfiguration conf = FSTConfiguration.createJsonConfiguration();

    static {
        conf.registerSerializer(PearsonsCorrelation.class, new PearsonsCorrelationSerializer(), false);
		conf.registerSerializer(Pattern.class, new PatternSerializer(), false);
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

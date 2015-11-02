package com.unifina.serialization;

import com.unifina.signalpath.filtering.SimpleMovingAverageEvents;
import com.unifina.signalpath.simplemath.Subtract;
import com.unifina.signalpath.statistics.LinearRegression;
import com.unifina.signalpath.statistics.PearsonsCorrelation;

import java.io.IOException;


public class SerializerExample {

	private static Serializer serializer = new SerializerImpl();

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		Subtract subtract = new Subtract();
		subtract.init();
		subtract.getInput("A").receive(Double.valueOf(10));
		subtract.getInput("B").receive(Double.valueOf(6));

		LinearRegression linearRegression = new LinearRegression();
		linearRegression.init();
		for (int i = 0; i < 100; ++i) {
			linearRegression.getInput("in").receive(Double.valueOf(i + Math.random()));
			linearRegression.sendOutput();
		}

		PearsonsCorrelation pearsonsCorrelation = new PearsonsCorrelation();
		pearsonsCorrelation.init();

		for (int i = 0; i < 100; ++i) {
			pearsonsCorrelation.getInput("inX").receive(Double.valueOf(Math.random() * 100));
			pearsonsCorrelation.getInput("inY").receive(Double.valueOf(Math.random() * 100));
			pearsonsCorrelation.sendOutput();
		}

		SimpleMovingAverageEvents ma = new SimpleMovingAverageEvents();
		ma.init();
		for (int i = 0; i < 100; ++i) {
			ma.getInput("in").receive(Double.valueOf(Math.random() * 100));
			ma.sendOutput();
		}

		serializer.serializeToFile(subtract, "subtract.json");
		serializer.serializeToFile(linearRegression, "linear-regression.json");
		serializer.serializeToFile(pearsonsCorrelation, "pearson-correlation.json");
		serializer.serializeToFile(ma, "ma.json");
	}
}

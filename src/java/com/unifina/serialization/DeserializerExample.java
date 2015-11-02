package com.unifina.serialization;

import com.unifina.signalpath.filtering.SimpleMovingAverageEvents;
import com.unifina.signalpath.simplemath.Subtract;
import com.unifina.signalpath.statistics.LinearRegression;
import com.unifina.signalpath.statistics.PearsonsCorrelation;

import java.io.IOException;
import java.util.Arrays;

public class DeserializerExample {

	private static Serializer serializer = new SerializerImpl();

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Subtract subtract = (Subtract) serializer.deserializeFromFile("subtract.json");
        subtract.sendOutput();

        LinearRegression linearRegression = (LinearRegression) serializer.deserializeFromFile("linear-regression.json");
        linearRegression.getInput("in").receive(115.0);
        linearRegression.sendOutput();

        PearsonsCorrelation pearsonsCorrelation = (PearsonsCorrelation) serializer.deserializeFromFile("pearson-correlation.json");

        pearsonsCorrelation.getInput("inX").receive(Double.valueOf(Math.random() * 100));
        pearsonsCorrelation.getInput("inY").receive(Double.valueOf(Math.random() * 100));
        pearsonsCorrelation.sendOutput();

        SimpleMovingAverageEvents ma = (SimpleMovingAverageEvents) serializer.deserializeFromFile("ma.json");

        System.out.println(subtract.getOutput("A-B"));
        System.out.println(Arrays.toString(linearRegression.getOutputs()));
        System.out.println(Arrays.toString(pearsonsCorrelation.getOutputs()));
        System.out.println(Arrays.toString(ma.getOutputs()));
    }
}

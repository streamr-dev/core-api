package com.unifina.signalpath.simplemath;

import com.unifina.service.SerializationService;
import com.unifina.signalpath.*;
import org.apache.log4j.Logger;

import java.math.MathContext;
import java.util.HashSet;
import java.util.Map;

public class Expression extends AbstractSignalPathModule {
	private final StringParameter expressionParam = new StringParameter(this, "expression", "x+y");
	private final TimeSeriesOutput out = new TimeSeriesOutput(this, "out");
	private final StringOutput error = new StringOutput(this, "error");

	transient private com.udojava.evalex.Expression expression;
	private String expressionAsString;
	private Iterable<String> variables;

	private static final Logger log = Logger.getLogger(Expression.class);

	@Override
	public void init() {
		expressionParam.setUpdateOnChange(true);
		expressionParam.setCanConnect(false);

		addInput(expressionParam);
		addOutput(out);
		addOutput(error);
	}

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);

		expressionAsString = expressionParam.getValue();
		initExpressionAndVariables();
		for (String variable : variables) {
			addInput(new TimeSeriesInput(this, variable));
		}
	}

	@Override
	public void sendOutput() {
		for (String variable : variables) {
			Double value = (Double) getInput(variable).getValue();
			expression.with(variable, value.toString());
		}

		try {
			double value = expression.eval().doubleValue();
			out.send(value);
		} catch (RuntimeException e) {
			log.error("Exception while evaluating expression! Param: "+expressionParam+", Variables: "+variables+", Expression: "+expression);
			// Null-safe error sending
			error.send(e.getMessage() != null ? e.getMessage() : e.toString());
		}
	}

	@Override
	public void clearState() {}

	@Override
	public void afterDeserialization(SerializationService serializationService) {
		super.afterDeserialization(serializationService);
		initExpressionAndVariables();
	}

	private void initExpressionAndVariables() throws com.udojava.evalex.Expression.ExpressionException {
		expression = new com.udojava.evalex.Expression(expressionAsString, MathContext.DECIMAL64);
		variables = new HashSet<>(expression.getUsedVariables());
	}
}

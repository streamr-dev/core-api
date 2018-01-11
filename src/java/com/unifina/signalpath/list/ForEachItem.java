package com.unifina.signalpath.list;

import com.mongodb.util.JSON;
import com.unifina.domain.signalpath.Canvas;
import com.unifina.service.SignalPathService;
import com.unifina.signalpath.*;
import grails.util.Holders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ForEachItem extends AbstractSignalPathModule {

	private final SignalPathParameter signalPathParameter = new SignalPathParameter(this, "canvas");
	private final BooleanParameter keepState = new BooleanParameter(this, "keepState", false);
	private final Map<ListInput, Input> listInputToExportedInput = new HashMap<>();
	private final Map<ListOutput, Output> listOutputToExportedOutput = new HashMap<>();
	private final TimeSeriesOutput numOfItems = new TimeSeriesOutput(this, "numOfItems");
	private SignalPath subCanvas;
	private Propagator subCanvasPropagator;

	public ForEachItem() {
		signalPathParameter.setUpdateOnChange(true);
		canRefresh = true;
	}

	@Override
	public void init() {
		addInput(signalPathParameter);
		addInput(keepState);
	}

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);

		// Load canvas
		Canvas canvas = signalPathParameter.getCanvas();
		if (canvas == null) {
			return;
		}

		// Construct signal path
		Map signalPathMap = (Map) JSON.parse(canvas.getJson());
		SignalPathService signalPathService = Holders.getApplicationContext().getBean(SignalPathService.class);
		subCanvas = signalPathService.mapToSignalPath(signalPathMap, true, getGlobals(), new SignalPath(false));

		// Find and validate exported endpoints
		List<Input> exportedInputs = subCanvas.getExportedInputs();
		List<Output> exportedOutputs = subCanvas.getExportedOutputs();
		if (exportedInputs.isEmpty()) {
			throw new RuntimeException("No exported inputs in canvas '" + canvas.getId() + "'. Need at least one.");
		}

		// Make list versions of exported endpoints
		for (Input exportedInput : exportedInputs) {
			ListInput input = new ListInput(this, exportedInput.getEffectiveName());
			listInputToExportedInput.put(input, exportedInput);
			addInput(input);
		}
		for (Output exportedOutput: exportedOutputs) {
			ListOutput output = new ListOutput(this, exportedOutput.getEffectiveName());
			listOutputToExportedOutput.put(output, exportedOutput);
			addOutput(output);
		}

		subCanvasPropagator = new Propagator(exportedInputs);
	}

	@Override
	public void sendOutput() {
		int numOfItemsToProcess = smallestReceivedListSize();

		Map<ListOutput, List> collectedValues = new HashMap<>();
		for (ListOutput listOutput : listOutputToExportedOutput.keySet()) {
			collectedValues.put(listOutput, new ArrayList());
		}

		for (int i=0; i < numOfItemsToProcess; ++i) {

			// Feed inputs
			for (Map.Entry<ListInput, Input> entry : listInputToExportedInput.entrySet()) {
				entry.getValue().receive(entry.getKey().getValue().get(i));
			}

			subCanvasPropagator.propagate();

			// Collect output values
			for (Map.Entry<ListOutput, Output> entry : listOutputToExportedOutput.entrySet()) {
				collectedValues.get(entry.getKey()).add(entry.getValue().getValue());
			}
		}

		// Send collected values to list outputs
		for (Map.Entry<ListOutput, List> entry : collectedValues.entrySet()) {
			entry.getKey().send(entry.getValue());
		}
		numOfItems.send(numOfItemsToProcess);

		if (!keepState.getValue()) {
			clearState();
		}
	}

	@Override
	public void clearState() {
		subCanvas.clearState();
		for (AbstractSignalPathModule module : subCanvas.getModules()) {
			module.clearState();
		}
	}

	private int smallestReceivedListSize() {
		int minSize = Integer.MAX_VALUE;
		for (ListInput listInput : listInputToExportedInput.keySet()) {
			int size = listInput.getValue().size();
			if (size < minSize) {
				minSize = size;
			}
		}
		return minSize;
	}
}

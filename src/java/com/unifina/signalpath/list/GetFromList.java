package com.unifina.signalpath.list;

import com.unifina.signalpath.*;

import java.util.List;

/**
 * Get item from list by index. Negative index counts from end of list
 */
public class GetFromList extends AbstractSignalPathModule {
	private IntegerParameter index = new IntegerParameter(this, "index", 0);
	private ListInput in = new ListInput(this, "in");
	private StringOutput error = new StringOutput(this, "error");
	private Output<Object> out = new Output<>(this, "out", "Object");

	@Override
	public void sendOutput() {
		List source = in.getValue();
		int listLength = source.size();
		if (listLength == 0) {
			error.send(emptyError);
			return;
		}

		int i = index.getValue();
		if (i >= 0 && i < listLength) {
			Object item = source.get(i);
			out.send(item);
		} else if (i < 0 && i >= -listLength) {
			Object item = source.get(listLength + i);
			out.send(item);
		} else {
			error.send(getOutOfBoundsErrorMessage(i, listLength));
		}
	}

	// re-used in unit test
	public static final String emptyError = "List is empty";
	public static String getOutOfBoundsErrorMessage(int i, int listLength) {
		return "Index " + i + " is out of bounds! List length: " + listLength;
	}

	@Override
	public void clearState() {}
}

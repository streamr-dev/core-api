package com.unifina.utils.testutils;

import com.unifina.signalpath.utils.ExportCSV;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

public class FakeExportCSVContext extends ExportCSV.Context {
	private transient StringWriter stringWriter;
	private int currentTimeInvocationNo = 0;
	private List<Long> currentTimes = Arrays.asList(
		0l, 5l, 																			// 1st sendOutput
		ExportCSV.MIN_MS_BETWEEN_UI_UPDATES - 6,											// 2nd sendOutput
		2 * ExportCSV.MIN_MS_BETWEEN_UI_UPDATES, 2 * ExportCSV.MIN_MS_BETWEEN_UI_UPDATES	// 3rd sendOutput
	);

	@Override
	public void openNewFile() {
		stringWriter = new StringWriter();
	}

	@Override
	public Writer getWriter() {
		return stringWriter;
	}

	@Override
	public String getFileName() {
		return "test.csv";
	}

	@Override
	public long getFileSizeInKilobytes() {
		return 666;
	}

	@Override
	public long currentTimeInMillis() {
		return currentTimes.get(currentTimeInvocationNo++);
	}

	public String resolveStringsAndReset() {
		String s = stringWriter.toString();
		stringWriter = null;
		currentTimeInvocationNo = 0;
		return s;
	}

	public boolean isFileOpened() {
		return stringWriter != null;
	}
}

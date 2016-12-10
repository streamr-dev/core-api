package com.unifina.utils.testutils;

import com.unifina.signalpath.utils.WriteToCsvFile;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

public class FakeFileHolder extends WriteToCsvFile.FileHolder {
	private transient StringWriter stringWriter;

	@Override
	public void openNewFile() throws IOException {
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

	public String resolveStringsAndReset() {
		String s = stringWriter.toString();
		stringWriter = null;
		return s;
	}
}

package com.unifina.utils;

import org.apache.commons.lang.StringUtils;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class CSVWriter2 implements Closeable {
	private static final int BUFFER_BYTES = 8192 * 100;

	private final String separator;
	private final boolean quoteColumns;
	private BufferedWriter writer;
	private Integer numOfCols;
	private long numOfRowsWritten = 0;

	public CSVWriter2(Writer writer, String separator, boolean quoteColumns) throws IOException {
		this.writer = new BufferedWriter(writer, BUFFER_BYTES);
		this.separator = separator;
		this.quoteColumns = quoteColumns;
	}
	
	public void writeRow(List row) throws IOException {
		if (numOfCols == null) {
			numOfCols = row.size();
		} else if (numOfCols != row.size()) {
			throw new RuntimeException("Expected " + numOfCols + " columns but was " + row.size() + " for row: " + row);
		}
		writer.write(StringUtils.join(handleRow(row), separator) + "\n");
		++numOfRowsWritten;
	}

	public long getNumOfRowsWritten() {
		return numOfRowsWritten;
	}

	@Override
	public void close() throws IOException {
		writer.flush();
		writer.close();
	}

	private List handleRow(List row) {
		if (quoteColumns) {
			List<String> newRow = new ArrayList<>(row.size());
			for (Object o : row) {
				newRow.add('"' + o.toString().replaceAll("\"", "\"\"") + '"');
			}
			return newRow;
		} else {
			return row;
		}
	}
}

package com.unifina.utils;

import com.opencsv.CSVWriter;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class RFC4180CSVWriter implements Closeable {
	private static final String RFC_4180_LINE_BREAK = "\r\n";

	private com.opencsv.CSVWriter writer;
	private Integer numOfCols;
	private long numOfRowsWritten = 0;

	public RFC4180CSVWriter(Writer writer) {
		this.writer = new CSVWriter(new BufferedWriter(writer),
			CSVWriter.DEFAULT_SEPARATOR,
			CSVWriter.DEFAULT_QUOTE_CHARACTER,
			CSVWriter.DEFAULT_ESCAPE_CHARACTER,
			RFC_4180_LINE_BREAK);
	}

	public void writeRow(List row) {
		if (numOfCols == null) {
			numOfCols = row.size();
		} else if (numOfCols != row.size()) {
			throw new RuntimeException("Expected " + numOfCols + " columns but was " + row.size() + " for row: " + row);
		}
		writer.writeNext(escapeColumns(row).toArray(new String[0]), false);
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

	private List<String> escapeColumns(List row) {
		List<String> newRow = new ArrayList<>(row.size());
		for (Object o : row) {
			newRow.add(o.toString());
		}
		return newRow;
	}
}

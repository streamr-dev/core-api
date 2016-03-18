package com.unifina.feed.mongodb;

import com.unifina.domain.data.Stream;
import org.bson.Document;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * A wrapper for Document that also has a reference to the Stream
 * the Document belongs to.
 */
public class DocumentFromStream extends Document {

	private final Stream stream;

	public DocumentFromStream(Document doc, Stream stream) {
		super(doc);
		this.stream = stream;
	}

	public Stream getStream() {
		return stream;
	}
}

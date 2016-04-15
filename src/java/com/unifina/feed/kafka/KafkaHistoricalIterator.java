package com.unifina.feed.kafka;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.unifina.feed.util.RawMessageIterator;
import com.unifina.kafkaclient.UnifinaKafkaMessageFactory;

public class KafkaHistoricalIterator implements Iterator<KafkaMessage> {

	private RawMessageIterator rawIterator;
	private KafkaMessageParser parser;
	private String topic;

	private int msgLength;
	private ByteBuffer raw;
	private byte[] arr;
	
	private static final Logger log = Logger.getLogger(KafkaHistoricalIterator.class);
	long counter = 0;
	
	public KafkaHistoricalIterator(InputStream inputStream, String topic) throws IOException {
		log.debug("Iterator created for "+topic);
		rawIterator = new RawMessageIterator(inputStream, 4, 65536, ByteOrder.BIG_ENDIAN);
		parser = new KafkaMessageParser();
		this.topic = topic;
	}
	
	@Override
	public boolean hasNext() {
		return rawIterator.hasNext();
	}

	@Override
	public KafkaMessage next() {
		msgLength = rawIterator.nextMessageLength();
		raw = rawIterator.next();
		if (raw==null)
			return null;

		arr = new byte[msgLength];
		raw.get(arr);
		counter++;
		return parser.parse(UnifinaKafkaMessageFactory.parse(topic, new byte[0], arr));
	}

	@Override
	public void remove() {
		throw new RuntimeException("Remove operation not supported!");
	}

	public void close() throws IOException {
		rawIterator.close();
		log.debug("Iterator closed for "+topic+". Read "+counter+" messages.");
	}

}

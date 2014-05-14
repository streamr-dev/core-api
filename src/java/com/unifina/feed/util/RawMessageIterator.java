package com.unifina.feed.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Iterator;

/**
 * Helper class for iterating byte array streams with variable-length messages.
 * This class assumes that the first lengthBytes bytes contain a message length L1, after which
 * the next L1 bytes contain the message content. Then the next lengthBytes bytes contain message
 * length L2, etc.
 * 
 * lengthBytes is a constant: 2 for short lengths, 4 for integer lengths.
 * 
 * @author Henri
 *
 */
public class RawMessageIterator implements Iterator<ByteBuffer> {

	private InputStream in;
	
	protected int msgLength = 0;
	protected boolean msgLengthRead = false;
	private int expectedBufferPos = 0;
	private int read = 0;
	
	private ReadableByteChannel gzc;
	protected ByteBuffer buffer;

	private int lengthBytes;
	private LengthReader lengthReader;
	
	public RawMessageIterator(InputStream in, int lengthBytes, int bufferSize, ByteOrder order) throws IOException {
		this.in = in;
		this.lengthBytes = lengthBytes;
		if (lengthBytes==2) {
			lengthReader = new LengthReader() {
				@Override
				int read(ByteBuffer buf) {
					return buf.getShort();
				}
			};
		}
		else if (lengthBytes==4) {
			lengthReader = new LengthReader() {
				@Override
				int read(ByteBuffer buf) {
					return buf.getInt();
				}
			};
		}
		else throw new IllegalArgumentException("Only short (2) or int (4) message lenghts are supported!");
		
		gzc = Channels.newChannel(in);

		buffer = ByteBuffer.allocateDirect(bufferSize);
		buffer.order(order);
//		buffer.order(ByteOrder.BIG_ENDIAN);
		buffer.limit(0);
		refillBuffer();
	}
	
	protected boolean ensureBuffer(int length) throws IOException {
		// Check that there is enough content in the buffer for reading the specified length
		if (buffer.remaining()<length) {
			refillBuffer();
		}
		return buffer.remaining()>=length;
	}

	protected void readMsgLength() throws IOException {
		if (!ensureBuffer(lengthBytes)) {
			msgLength = 0;
		}
		else {
		// Length of next message
			msgLength = lengthReader.read(buffer);
			expectedBufferPos += lengthBytes;
			msgLengthRead = true;
		}
	}
	
	private void refillBuffer() throws IOException {
		buffer.compact();
		read = gzc.read(buffer);
		buffer.flip(); // Does the same as the below commented lines
//		buffer.limit(buffer.position());
//		buffer.position(0);
		expectedBufferPos = 0;
	}
	
	@Override
	public boolean hasNext() {
		if (!msgLengthRead)
			try {
				readMsgLength();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		
		return msgLength > 0;
	}

	public int nextMessageLength() {
		if (!msgLengthRead)
			try {
				readMsgLength();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		
		return msgLength;
	}
	
	@Override
	public ByteBuffer next() {
		// Check that the buffer position is not being screwed up outside this class
		if (expectedBufferPos!=buffer.position())
			throw new RuntimeException("Buffer position unexpected! Someone read too much or too little from buffer?");
		
		if (!msgLengthRead)
			try {
				readMsgLength();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		
		if (msgLength==0)
			return null;
		
		try {
			ensureBuffer(msgLength);
			// It is expected that msgLength bytes will be read from the buffer, no more, no less
			expectedBufferPos = buffer.position()+msgLength;
			msgLengthRead = false;
			return buffer;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void remove() {
		throw new RuntimeException("Remove operation not supported!");
	}

	public void close() throws IOException {
		gzc.close();
	}

	abstract class LengthReader {
		abstract int read(ByteBuffer buf);
	}
}

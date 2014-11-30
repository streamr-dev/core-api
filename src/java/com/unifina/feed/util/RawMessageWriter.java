package com.unifina.feed.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.zip.GZIPOutputStream;

/**
 * Utility class for writing message streams that can be read using RawMessageIterator.
 * The message streams contain 2 bytes message length, then the message, then again
 * 2 bytes message, and so on.
 * @author Henri
 */
public class RawMessageWriter {
	
	private LengthWriter lengthWriter;
	private OutputStream fileOut;
	private WritableByteChannel channel;
	private ByteBuffer outBuf;
	private int origLimit;
	ByteBuffer secBuf;
	private boolean useCompression;
	private int lengthBytes;
	
	public RawMessageWriter(File file, boolean useCompression) throws IOException {
		this(file, useCompression, 2, 65536, ByteOrder.BIG_ENDIAN);
	}
	
	public RawMessageWriter(File file, boolean useCompression, int lengthBytes, int bufferSize, ByteOrder order) throws IOException {
		this.useCompression = useCompression;
		this.lengthBytes = lengthBytes;
		
		if (lengthBytes==2) {
			lengthWriter = new LengthWriter() {
				@Override
				void write(ByteBuffer buf, short length) {
					buf.putShort(length);
				}
			};
		}
		else if (lengthBytes==4) {
			lengthWriter = new LengthWriter() {
				@Override
				void write(ByteBuffer buf, short length) {
					buf.putInt(length);
				}
			};
		}
		else throw new RuntimeException("Only support 2 or 4 byte lengths. Given: "+lengthBytes);
		
		fileOut = new FileOutputStream(file, false);
		if (useCompression)
			fileOut = new GZIPOutputStream(fileOut, true);
		
		channel = Channels.newChannel(fileOut);
		
		outBuf = ByteBuffer.allocateDirect(bufferSize);
		outBuf.order(order);
	}
	
	public void write(short msgLength, ByteBuffer src) throws IOException {
		// Write msgLength to outBuf and then msgLength bytes from src buffer
		if (outBuf.remaining()<lengthBytes+msgLength)
			flush();
		
		origLimit = src.limit();
		src.limit(src.position()+msgLength);
		
		lengthWriter.write(outBuf, msgLength);
		
		outBuf.put(src);
		src.limit(origLimit);
	}
	
	public void flush() throws IOException {
		outBuf.limit(outBuf.position());
		outBuf.position(0);
		channel.write(outBuf);
		outBuf.clear();
	}
	
	public void close() throws IOException {
		flush();
		
		if (useCompression)
			((GZIPOutputStream)fileOut).finish();
			
		fileOut.close();
	}
	
	abstract class LengthWriter {
		abstract void write(ByteBuffer buf, short length);
	}
}
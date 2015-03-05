package com.unifina.feed.kafka;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;
import java.util.zip.GZIPOutputStream;

public class KafkaFeedFileWriter {
	private File dir;
	private File file;
	private GZIPOutputStream gzos;
	ByteBuffer lengthBuffer;
	
	public KafkaFeedFileWriter(String filename) throws IOException {
		this.dir = Files.createTempDirectory(filename, new FileAttribute[0]).toFile();
		this.file = new File(dir, filename);
		this.gzos = new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
	}
	
	public void write(byte[] rawMsg) throws IOException {
		lengthBuffer = ByteBuffer.allocate(4);
		lengthBuffer.order(ByteOrder.BIG_ENDIAN);
		byte[] length = lengthBuffer.putInt(rawMsg.length).array();
		gzos.write(length);
		gzos.write(rawMsg);
	}
	
	public void close() throws IOException {
		gzos.finish();
		gzos.close();
	}
	
	public File getFile() {
		return file;
	}
	
	public void deleteFile() {
		file.delete();
		dir.delete();
	}
}

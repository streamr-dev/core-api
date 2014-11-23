package com.unifina.feed.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.zip.GZIPInputStream;

/**
 * Copies a raw stream of messages
 */
public class RawMessageCopyUtil {
	
	public static void main(String[] args) throws Exception {
		if (args.length<3) {
			System.out.println("Usage: RawMessageCopyUtil <infile> <outfile> <messageCount>");
			return;
		}
		
		File inFile = new File(args[0]);
		if (!inFile.exists() || !inFile.canRead()) {
			throw new Exception("File not readabile: "+inFile);
		}
		
		File outFile = new File(args[1]);
		final int count = Integer.parseInt(args[2]);
		InputStream is = new FileInputStream(inFile);
		
		if (inFile.getName().endsWith(".gz"))
			is = new GZIPInputStream(is);
			
		RawMessageIterator iterator = new RawMessageIterator(is);
		RawMessageWriter writer = new RawMessageWriter(outFile, outFile.getName().endsWith(".gz"));
		ByteBuffer buf;
		int msgLength;
		int i;
		for (i=0;i<count;i++) {
			if (!iterator.hasNext())
				throw new Exception("File only contains "+i+" messages!");
			
			msgLength = iterator.nextMessageLength();
			buf = iterator.next();
			writer.write((short)msgLength, buf);
		}
		
		writer.close();
		iterator.close();
		
		System.out.println(i+" messages written to "+outFile);
	}
}

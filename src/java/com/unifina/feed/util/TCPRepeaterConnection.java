package com.unifina.feed.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.log4j.Logger;

import com.unifina.feed.Catchup;
import com.unifina.feed.Message;
import com.unifina.feed.MessageHub;
import com.unifina.feed.MessageRecipient;

public class TCPRepeaterConnection extends Thread implements MessageRecipient {

	Socket socket;
	MessageHub hub;
	BufferedReader reader;
	OutputStream out;
	
	private static final int QUEUE_SIZE = 100000;
	ArrayBlockingQueue<Message> queue = new ArrayBlockingQueue<>(QUEUE_SIZE);
	
	private static final Logger log = Logger.getLogger(TCPRepeaterConnection.class);
	
	long counter = 1;
	Timer secTimer = new Timer();
	
	// Allow 10 seconds of silence in the beginning
	long clientLastActive = System.currentTimeMillis() + 10*1000;
	
	public TCPRepeaterConnection(Socket socket, MessageHub hub) throws IOException {
		this.socket = socket;
		this.hub = hub;
		
		reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = socket.getOutputStream();
		
		final MessageHub rrqHub = hub;
		Thread readerThread = new Thread() {
			@Override
			public void run() {
				try {
					String line;
					while ((line = reader.readLine()) != null) {
						clientLastActive = System.currentTimeMillis();
						if (line.startsWith("RRQ")) {
							String[] req = line.split(" ");
							int from = Integer.parseInt(req[1]);
							int count = Integer.parseInt(req[2]);
							int to = from+count-1;
							
							log.info("Client request: "+line);
							
							// This call also adds this MessageRecipient to the hub recipient list
							Catchup catchup = rrqHub.startCatchup(TCPRepeaterConnection.this);
							Object next;
							int c = catchup.getNextCounter();
							
							while ((next = catchup.getNext())!=null) {
								if (c >= from && c<=to) {
									send(new Message(c,next));
									c = catchup.getNextCounter();
								}
								else if (c>to) break;
							}
						}
					}
				} catch (IOException e) {
					log.error("Error reading from client!",e);
				}
			}
		};
		readerThread.start();

		Date now = new Date();
		secTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				if (queue.isEmpty()) {
					try {
						send(new Message(counter,""));
					} catch (IOException e) {
						log.error("Failed to send heartbeat, exiting heartbeat thread!");
						this.cancel();
					}
				}
			}
		},
		new Date(now.getTime() + (1000 - (now.getTime()%1000))), // Time till next even second
		1000);   // Repeat every second
	}

	private synchronized void send(Message msg) throws IOException {
		byte[] msgBytes = msg.message.toString().getBytes();
		ByteBuffer buf = ByteBuffer.allocate(8+msgBytes.length);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		buf.putInt((int)msg.counter);
		buf.putInt(msgBytes.length);
		buf.put(msgBytes);
		out.write(buf.array());
	}
	
	@Override
	public void run() {
		while(true) {
			Message msg=null;
			try {
				msg = queue.take();
			} catch (InterruptedException e) {
				log.error("Interrupted!",e);
			}
			
			if (msg!=null) try {
				send(msg);
			} catch (IOException e) {
				log.error("Error writing to socket! Quitting this thread.",e);
				break;
			}
		}
		try {
			out.close();
			reader.close();
		} catch (IOException e) {
			log.error("Error cleaning up",e);
		}
	}
	
	@Override
	public void receive(Message message) {
		counter = message.counter;
		if(!queue.offer(message))
			log.error("Queue is full! Message "+message.counter+" will be dropped.");
	}

	@Override
	public void sessionBroken() {
		// TODO Auto-generated method stub

	}

	@Override
	public void sessionRestored() {
		// TODO Auto-generated method stub

	}

	@Override
	public void sessionTerminated() {
		// TODO Auto-generated method stub

	}

	@Override
	public int getReceivePriority() {
		return 1000;
	}

}

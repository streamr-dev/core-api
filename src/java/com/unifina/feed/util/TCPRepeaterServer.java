package com.unifina.feed.util;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;

import com.unifina.feed.MessageHub;

public class TCPRepeaterServer extends Thread {

	ServerSocket serverSocket;

	private int port;

	private MessageHub hub;
	
	private static final Logger log = Logger.getLogger(TCPRepeaterServer.class);
	
	public TCPRepeaterServer(int port, MessageHub hub) {
		this.port = port;
		this.hub = hub;
		setName("TCPRepeaterServer "+port);
	}
	
	@Override
	public void run() {
		try {
		    serverSocket = new ServerSocket(port);
		    log.error("TCPRepeaterServer listening on tcp port "+port);
		} 
		catch (IOException e) {
		    log.error("Could not listen on port: "+port,e);
		}
		
		while (true) {
			try {
				Socket clientSocket = serverSocket.accept();
				TCPRepeaterConnection conn = new TCPRepeaterConnection(clientSocket,hub);
				conn.start();
			} 
			catch (IOException e) {
			    log.error("Failed to accept incoming connection on port "+port,e);
			}
		}
	}

}

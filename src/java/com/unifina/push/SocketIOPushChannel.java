package com.unifina.push;

import grails.converters.JSON;

import java.net.URISyntaxException;
import java.util.Map;

import org.apache.log4j.Logger;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

public class SocketIOPushChannel extends PushChannel {

	private Socket socket;
	private JSON json = new JSON();
	private boolean connected = false;

	private static final Logger log = Logger.getLogger(SocketIOPushChannel.class);
	
	public SocketIOPushChannel() {
		super();
		
		synchronized(this) {
			try {
				IO.Options opts = new IO.Options();
				opts.forceNew = true;
				socket = IO.socket("http://localhost:8090", opts);
			} catch (URISyntaxException e) {
				throw new RuntimeException("Bad URL for socket.io server!");
			}
			
			socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
			  @Override
			  public void call(Object... args) {
				  connected = true;
				  log.info("Connected to socket.io");
				  synchronized(SocketIOPushChannel.this) {
					  SocketIOPushChannel.this.notify();
				  }
			  }
			}).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
			  @Override
			  public void call(Object... args) {
				  connected = false;
				  log.info("Disconnected from socket.io");
				  synchronized(SocketIOPushChannel.this) {
					  SocketIOPushChannel.this.notify();
				  }
			  }
			}).on("client-disconnect", new Emitter.Listener() {
			  @Override
			  public void call(Object... args) {
				  // This won't get called -- what channel should we subscribe to?
				  log.info("Client disconnected!");
				  for (PushChannelEventListener l : eventListeners)
					  l.onClientDisconnected();
			  }
			});
			
			socket.connect();
			
			try {
				wait(10000);
			} catch (InterruptedException e1) {
				throw new RuntimeException("Failed to connect to socket.io server!");
			}
		}
	}

	@Override
	public void destroy() {
		super.destroy();
		synchronized(this) {
			socket.disconnect();
			try {
				wait(10000);
			} catch (InterruptedException e1) {
				throw new RuntimeException("Failed to disconnect from socket.io server!");
			}
		}
	}
	
	@Override
	protected void doPush(PushChannelMessage msg) {
		((Map) msg.getContent()).put("channel", msg.getChannel());
		String str = msg.toJSON(json);
		socket.emit("ui", str);
	}
	
	public boolean isConnected() {
		return connected;
	}

}

package com.unifina.signalpath.blockchain;

import com.google.gson.JsonParser;
import org.apache.log4j.Logger;
import javax.websocket.*;
import javax.websocket.Session;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import com.google.gson.JsonObject;
import org.json.JSONArray;
import org.json.JSONObject;

public class WebsocketEthereumJsonRpc extends EthereumJsonRpc {
	private static final Logger log = Logger.getLogger(WebsocketEthereumJsonRpc.class);
	private EthereumRpcEndpoint wsEndpoint;
	Session userSession = null;

	@ClientEndpoint
	protected class EthereumRpcEndpoint{

		@OnClose
		public void onClose(Session session) throws IOException {
			log.info("session "+session + "was closed");
		}
		@OnError
		public void onError(Session session, Throwable t){
			log.error("session "+session+ " reported error "+t.getMessage());
		}

		@OnOpen
		public void onOpen(Session session) {
			log.info("opening websocket "+session);
		}

		@OnMessage
		public void onMessage(String message) {
			JSONObject jso = new JSONObject(message);
			handler.processResponse(jso);
		}


		public void sendMessage(String message) {
			userSession.getAsyncRemote().sendText(message);
		}
	}

	public WebsocketEthereumJsonRpc(String url, JsonRpcResponseHandler handler) throws URISyntaxException, IOException, DeploymentException {
		super(url,handler);
		wsEndpoint = new EthereumRpcEndpoint();
		open();
	}

	@Override
	public void rpcCall(String method, List params, int callId){
		wsEndpoint.sendMessage(formRequestBody(method, params, callId));
	}


	public void open() throws URISyntaxException, IOException, DeploymentException {
		WebSocketContainer container = ContainerProvider.getWebSocketContainer();
		userSession = container.connectToServer(wsEndpoint, new URI(url));
	}

	public void close() throws IOException {
		userSession.close();
	}
}


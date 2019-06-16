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
			session.close();
		}

		@OnOpen
		public void onOpen(Session session) {
			log.info("opening websocket");
		//	userSession = session;
		}

		@OnMessage
		public void onMessage(String message) {
			JSONObject jso = new JSONObject((message));
			handler.processResponse(jso);
		}


		public void sendMessage(String message) {
			userSession.getAsyncRemote().sendText(message);
		}
	}

	public WebsocketEthereumJsonRpc(String url, JsonRpcResponseHandler handler) throws URISyntaxException, IOException, DeploymentException {
		super(url,handler);
		wsEndpoint = new EthereumRpcEndpoint();
	}

	public void rpcCall(String method, List params, int callId){
		wsEndpoint.sendMessage(formRequestBody(method, params, callId));
	}


	public void open() throws URISyntaxException, IOException, DeploymentException {
		WebSocketContainer container = ContainerProvider.getWebSocketContainer();
		userSession = container.connectToServer(wsEndpoint, new URI(url));
		wsEndpoint.onOpen(userSession);
	}

	public void close() throws IOException {
		wsEndpoint.onClose(userSession);
	}
}


package com.unifina.service

import grails.converters.JSON
import grails.transaction.Transactional
import groovy.transform.CompileStatic

import java.nio.charset.StandardCharsets
import java.security.AccessControlException
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

import org.apache.log4j.Logger
import org.codehaus.groovy.grails.web.converters.exceptions.ConverterException
import org.codehaus.groovy.grails.web.json.JSONObject

import com.mashape.unirest.http.HttpResponse
import com.mashape.unirest.http.Unirest
import com.unifina.datasource.BacktestDataSource
import com.unifina.datasource.DataSource
import com.unifina.datasource.RealtimeDataSource
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Module
import com.unifina.domain.signalpath.RunningSignalPath
import com.unifina.domain.signalpath.UiChannel
import com.unifina.push.KafkaPushChannel
import com.unifina.push.PushChannelEventListener
import com.unifina.signalpath.RuntimeRequest
import com.unifina.signalpath.RuntimeResponse
import com.unifina.signalpath.SignalPath
import com.unifina.signalpath.SignalPathRunner
import com.unifina.utils.Globals
import com.unifina.utils.GlobalsFactory
import com.unifina.utils.IdGenerator
import com.unifina.utils.NetworkInterfaceUtils

class SignalPathService {

    static transactional = false
	
	def servletContext
	def grailsApplication
	def grailsLinkGenerator
	def kafkaService
	def serializationService
	
	private static final Logger log = Logger.getLogger(SignalPathService.class)
	
	public SignalPath jsonToSignalPath(Map signalPathData, boolean connectionsReady, Globals globals, boolean isRoot) {
		SignalPath sp = new SignalPath(isRoot)
		sp.globals = globals
		sp.init()		
		sp.configure(signalPathData)
		
		if (connectionsReady)
			sp.connectionsReady()
		return sp
	}
	
	public Map signalPathToJson(SignalPath sp) {
		return  [name: sp.name, modules:sp.modules.collect {it.getConfiguration()}]
	}
	
	/**
	 * Rebuilds a saved representation of a SignalPath along with its context.
	 * Potentially modifies the map given as parameter.
	 * @param json
	 * @return
	 */
	public Map reconstruct(Map json, Globals globals) {
		SignalPath sp = jsonToSignalPath(json.signalPathData, true, globals, true)
		
		// TODO: remove backwards compatibility
		if (json.timeOfDayFilter)
			json.signalPathContext.timeOfDayFilter = json.timeOfDayFilter
		
		json.signalPathData = signalPathToJson(sp)
		
		return json
	}
	
	public byte[] compress(String s) {
		byte[] stringBytes = s.getBytes(StandardCharsets.UTF_8)
		return compressBytes(stringBytes)
	}
	
	private byte[] compressBytes(byte[] uncompressed) {
		ByteArrayOutputStream targetStream = new ByteArrayOutputStream()
		GZIPOutputStream zipStream = new GZIPOutputStream(targetStream)
		zipStream.write(uncompressed)
		zipStream.close()
		byte[] zipped = targetStream.toByteArray()
		targetStream.close()
		return zipped
	}
	
	private byte[] uncompressBytes(byte[] gzipped) {
		byte[] ungzipped = new byte[0];
		final GZIPInputStream inputStream = new GZIPInputStream(new ByteArrayInputStream(gzipped));
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(gzipped.length);
		final byte[] buffer = new byte[10240];
		int bytesRead = 0;
		while (bytesRead != -1) {
			bytesRead = inputStream.read(buffer, 0, 10240);
			if (bytesRead != -1) {
				byteArrayOutputStream.write(buffer, 0, bytesRead);
			}
		}
		ungzipped = byteArrayOutputStream.toByteArray();
		inputStream.close();
		byteArrayOutputStream.close();
		return ungzipped;
	}
	
	public String uncompress(byte[] zipped) {
		byte[] unzipped = uncompressBytes(zipped)
		return new String(unzipped,StandardCharsets.UTF_8)
	}
	
	public DataSource createDataSource(Map signalPathContext, Globals globals) {
		// Read the DataSource class from signalPathContext

		// Return the historical DataSource by default
		if (signalPathContext.live==null || !signalPathContext.live)
			return new BacktestDataSource(globals)
		else return new RealtimeDataSource(globals)
		
	}
	
	public List<RunningSignalPath> launch(List<Map> signalPathData, Map signalPathContext, SecUser user, boolean adhoc) {
		
		// Create Globals
		Globals globals = GlobalsFactory.createInstance(signalPathContext, grailsApplication)
		globals.uiChannel = new KafkaPushChannel(kafkaService)
		
		// Create the runner thread
		SignalPathRunner runner = new SignalPathRunner(signalPathData, globals, adhoc)
		String runnerId = runner.runnerId
		
		// Abort on client disconnect
		globals.uiChannel.addEventListener(new PushChannelEventListener() {
			public void onClientDisconnected() {
				runner.abort()
			}
		})
		
		// Start the runner thread
		runner.start()
		
		// Save reference to running SignalPaths to the database
		return createRunningSignalPathReferences(runner, user, adhoc)
	}
	
	/**
	 * Creates RunningSignalPath domain objects into the database with initial state "starting".
	 * The RunningSignalPaths can be subsequently started by calling startLocal() or startRemote().
	 * @param signalPathData
	 * @param user
	 * @param adhoc
	 * @param resetUiChannelIds
	 * @return
	 */
	@Transactional
	public RunningSignalPath createRunningSignalPath(Map sp, SecUser user, boolean adhoc, boolean resetUiChannelIds) {
		if (resetUiChannelIds) {
			sp.uiChannel = [id:IdGenerator.get(), name: "Notifications"]
			sp.modules.each {
				if (it.uiChannel)
					it.uiChannel.id = IdGenerator.get()
			}
		}
		
		RunningSignalPath rsp = new RunningSignalPath()
		rsp.name = sp.name ?: "(unsaved canvas)"
		rsp.user = user
		rsp.json = (sp as JSON)
		rsp.state = "starting"
		rsp.adhoc = adhoc
		
		UiChannel rspUi = new UiChannel()
		rspUi.id = sp.uiChannel.id
		rsp.addToUiChannels(rspUi)
		
		for (Map it : sp.modules) { 
			if (it.uiChannel) {
				UiChannel ui = new UiChannel()
				ui.id = it.uiChannel.id
				ui.hash = it.hash.toString()
				ui.module = Module.load(it.id)
				ui.name = it.uiChannel.name
				
				rsp.addToUiChannels(ui)
			}
		}
		
		rsp.save(flush:true, failOnError:true)
		
		return rsp
	}
	
	@Transactional
	public void deleteRunningSignalPathReferences(SignalPathRunner runner) {
		// Delayed-delete the topics in one hour
		List<UiChannel> channels = UiChannel.findAll { runningSignalPath.runner == runner.getRunnerId()}
		kafkaService.createDeleteTopicTask(channels.collect{it.id}, 60*60*1000)
		
		List uiIds = UiChannel.executeQuery("select ui.id from UiChannel ui where ui.runningSignalPath.runner = ?", [runner.getRunnerId()])
		if (!uiIds.isEmpty())
			UiChannel.executeUpdate("delete from UiChannel ui where ui.id in (:list)", [list:uiIds])
			
		RunningSignalPath.executeUpdate("delete from RunningSignalPath r where r.runner = ?", [runner.getRunnerId()])
	}
	
    def runSignalPaths(List<SignalPath> signalPaths) {
		// Check that all the SignalPaths share the same Globals object
		Globals globals = null
		for (SignalPath sp : signalPaths) {
			if (globals==null)
				globals = sp.globals
			else if (globals!=sp.globals)
				throw new RuntimeException("All SignalPaths don't share the same Globals!")
		}
		
		// Check that a DataSource is created
		if (globals.dataSource==null)
			throw new RuntimeException("No DataSource created! First call createDataSource()!")
			
		// Start feed, blocks until feed is complete
		globals.dataSource.startFeed()
		
		// Stop the feed, cleanup
		globals.dataSource.stopFeed()
    }
	
	@Deprecated
	def stopSignalPath(SignalPath signalPath) {
		// Stop feed
		signalPath.globals.dataSource.stopFeed()
	}
	
	void startLocal(RunningSignalPath rsp, Map signalPathContext) {		
		// Create Globals
		Globals globals = GlobalsFactory.createInstance(signalPathContext, grailsApplication)
		globals.uiChannel = new KafkaPushChannel(kafkaService, rsp.adhoc)

		SignalPathRunner runner
		// Create the runner thread
		if (rsp.serialized == null || rsp.serialized.empty) {
			runner = new SignalPathRunner([JSON.parse(rsp.json)], globals, rsp.adhoc)
			log.info("Creating new signalPath connections " + rsp.id)
		} else {
			SignalPath sp = serializationService.deserialize(rsp.serialized)
			runner = new SignalPathRunner(sp, globals, rsp.adhoc)
			log.info("De-serializing existing signalPath " + rsp.id + " " + rsp.serialized)
		}

		runner.addStartListener({

			if (!servletContext["signalPathRunners"]) {
				servletContext["signalPathRunners"] = [:]
			}
			servletContext["signalPathRunners"].put(runner.runnerId, runner)
		})

		runner.addStopListener({
			servletContext["signalPathRunners"].remove(runner.runnerId)
		})

		runner.signalPaths.each {
			it.runningSignalPath = rsp
		}
		String runnerId = runner.runnerId
		
		// Start the runner thread
		runner.start()
		
		// Wait for runner to be in running state
		runner.waitRunning(true)
		if (!runner.getRunning())
			log.error("Timed out while waiting for runner $runnerId to start!")
		
		rsp.runner = runnerId
		rsp.state = "running"
		
		// Use the link generator to get the protocol and port, but use network IP address
		// as the host to get the address of this individual server
		String link = grailsLinkGenerator.link(controller:'live', action:'request', absolute:true)
		URL url = new URL(link)
		
		rsp.server = NetworkInterfaceUtils.getIPAddress(grailsApplication.config.streamr.ip.address.prefixes ?: []).getHostAddress()
		rsp.requestUrl = url.protocol+"://"+rsp.server+":"+(url.port>0 ? url.port : url.defaultPort)+grailsLinkGenerator.link(uri:"/api/live/request")
		
		rsp.save()
	}
	
	boolean stopLocal(RunningSignalPath rsp) {
		SignalPathRunner runner = servletContext["signalPathRunners"]?.get(rsp.runner)
		if (runner!=null && runner.isAlive()) {
			runner.abort()
			
			// Wait for runner to be stopped state
			runner.waitRunning(false)
			if (runner.getRunning()) {
				log.error("Timed out while waiting for runner $rsp.runner to stop!")
				return false
			}
			else return true
		}
		else {
			log.error("stopLocal: could not find runner $rsp.runner!")
			updateState(rsp.runner, "stopped")
			return false
		}
	}
	
	@CompileStatic
	RuntimeResponse stopRemote(RunningSignalPath rsp, SecUser user) {
		return runtimeRequest([type:"stopRequest"], rsp, null, user)
	}
	
	@CompileStatic
	boolean ping(RunningSignalPath rsp, SecUser user) {
		RuntimeResponse response = runtimeRequest([type:'ping'], rsp, null, user)
		return response.isSuccess()
	}
	
	@CompileStatic
	RuntimeResponse sendRemoteRequest(Map msg, RunningSignalPath rsp, Integer hash, SecUser user) {
		def req = Unirest.post(rsp.requestUrl)
		def json = [
			local: true,
			msg: msg,
			id: rsp.id
		]
		
		if (hash)
			json.hash = hash
			
		if (user) {
			json.key = user.apiKey
			json.secret = user.apiSecret
		}

		req.header("Content-Type", "application/json")
		
		log.info("sendRemoteRequest: $json")
		
		HttpResponse<String> response = req.body((json as JSON).toString()).asString()

		try {
			Map map = (JSONObject) JSON.parse(response.getBody())
			return new RuntimeResponse(map)
		} catch (ConverterException e) {
			log.error("sendRemoteRequest: Failed to parse JSON response: "+response.getBody())
			throw new RuntimeException("Failed to parse JSON response", e)
		}
	}
	
	RuntimeResponse runtimeRequest(Map msg, RunningSignalPath rsp, Integer hash, SecUser user, boolean localOnly = false) {
		SignalPathRunner spr = servletContext["signalPathRunners"]?.get(rsp.runner)
		
		log.info("runtimeRequest: $msg, RunningSignalPath: $rsp.id, module: $hash, localOnly: $localOnly")
		
		// Give an error if the runner was not found locally although it should have been
		if (localOnly && !spr) {
			log.error("runtimeRequest: $msg, runner not found with localOnly=true, responding with error")
			return new RuntimeResponse([success:false, error: "Canvas does not appear to be running!"])
		}
		// May be a remote runner, check server and send a message
		else if (!localOnly && !spr) {
			try {
				return sendRemoteRequest(msg, rsp, hash, user)
			} catch (Exception e) {
				log.error("Unable to contact remote RunningSignalPath id $rsp.id at $rsp.requestUrl")
				return new RuntimeResponse([success:false, error: "Unable to communicate with remote server!"])
			}
		}
		// If runner found
		else {
			SignalPath sp = spr.signalPaths.find {
				it.runningSignalPath.id == rsp.id
			}
			
			if (!sp) {
				log.error("runtimeRequest: $msg, runner found but canvas not found. This should not happen. RSP: $rsp, module: $hash")
				return new RuntimeResponse([success:false, error: "Canvas not found in runner. This should not happen."])
			}
			else {				
				RuntimeRequest request = new RuntimeRequest(msg)
				request.setAuthenticated(user != null)
				
				/**
				 * Requests for the runner thread
				 */
				if (request.type=="stopRequest") {
					if (!request.isAuthenticated())
						throw new AccessControlException("stopRequest requires authentication!");
		
					stopLocal(rsp);
					
					return new RuntimeResponse(true, [request:request])
				}
				else if (request.type=="ping") {
					if (!rsp.shared && !request.isAuthenticated())
						throw new AccessControlException("ping requires authentication!");
					
					return new RuntimeResponse(true, [request:request])
				}
				/**
				 * Requests for SignalPaths and modules
				 */
				else {
					// Handle module-specific message
					Future<RuntimeResponse> future
					if (hash!=null) {
						future = sp.getModule(hash).onRequest(request)
					}
					// Handle signalpath-specific message
					else {
						future = sp.onRequest(request)
					}
					
					try {
						RuntimeResponse resp = future.get(30, TimeUnit.SECONDS)
						log.info("runtimeRequest: responding with $resp")
						return resp
					} catch (TimeoutException e) {
						return new RuntimeResponse([success:false, error: "Timed out while waiting for response."])
					}
				}
				
			}
		}
		
	}
	
	void updateState(String runnerId, String state) {
		RunningSignalPath.executeUpdate("update RunningSignalPath rsp set rsp.state = ? where rsp.runner = ?", [state, runnerId])
	}
	
	@Deprecated
	void setLiveRunner(SecUser user, SignalPathRunner runner) {
		setLiveRunner("live-$user.id",runner)
	}
	
	@Deprecated
	void setLiveRunner(String sessionId, SignalPathRunner runner) {
		if (runner==null)
			servletContext.removeAttribute(sessionId)
		else servletContext[sessionId] = runner
	}
	
	@Deprecated
	SignalPathRunner getLiveRunner(SecUser user) {
		return getLiveRunner("live-$user.id")
	}
	
	@Deprecated
	SignalPathRunner getLiveRunner(String sessionId) {
		return servletContext[sessionId]
	}
	
	List getUpdateableParameters(Map signalPathData, Closure c=null) {
		List parameters = []
		signalPathData.modules.each {module->
			module.params.each {
				String key = "${module.hash}_${module.id}_${it.name}_${it.value}"
				if (!it.connected) {
					it.parameterUpdateKey = key
					
					if (c) 
						c(module,it)
					
					parameters << it
				}
			}
		}
		return parameters
	}
	
	/**
	 * 
	 * @param paramMap
	 * @param signalPathData
	 * @return true if any parameter was changed
	 */
	boolean updateParameters(Map paramMap, Map signalPathData) {
		boolean changed = false
		signalPathData.modules.each {module->
			module.params.each {
				String key = "${module.hash}_${module.id}_${it.name}_${it.value}"
				if (!it.connected && paramMap[key]!=null && it.value.toString()!=paramMap[key].toString()) {
					it.value = paramMap[key]
					changed = true
				}
			}
		}
		return changed
	}
}
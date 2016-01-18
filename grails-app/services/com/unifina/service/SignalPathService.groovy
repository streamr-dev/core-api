package com.unifina.service

import com.unifina.domain.signalpath.Canvas
import com.unifina.serialization.SerializationException
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
import com.unifina.domain.signalpath.UiChannel
import com.unifina.push.KafkaPushChannel
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
	
	public SignalPath mapToSignalPath(Map signalPathMap, boolean connectionsReady, Globals globals, boolean isRoot) {

		SignalPath sp = new SignalPath(isRoot)
		sp.globals = globals
		sp.init()		
		sp.configure(signalPathMap)
		
		if (connectionsReady)
			sp.connectionsReady()
		return sp
	}
	
	public Map signalPathToMap(SignalPath sp) {
		return  [
			name: sp.name,
			modules: sp.modules.collect { it.getConfiguration() },
			settings: sp.globals.signalPathContext,
			hasExports: sp.hasExports()
		]
	}
	
	/**
	 * Rebuilds a saved representation of a SignalPath along with its context.a
	 * Potentially modifies the map given as parameter.
	 * @param json
	 * @return
	 */
	public Map reconstruct(Map signalPathMap, Globals globals) {
		SignalPath sp = mapToSignalPath(signalPathMap, true, globals, true)
		
		// TODO: remove backwards compatibility
		if (signalPathMap.timeOfDayFilter)
			signalPathMap.signalPathContext.timeOfDayFilter = signalPathMap.timeOfDayFilter

		return signalPathToMap(sp)
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
	
	/**
	 * Creates Canvas domain objects into the database with initial state "starting".
	 * The Canvas can be subsequently started by calling startLocal() or startRemote().
	 * @param signalPathData
	 * @param user
	 * @param adhoc
	 * @param resetUiChannelIds
	 * @return
	 */
	@Transactional
	public Canvas createRunningCanvas(Map sp, SecUser user, boolean adhoc, boolean resetUiChannelIds) {
		if (resetUiChannelIds) {
			sp.uiChannel = [id:IdGenerator.get(), name: "Notifications"]
			sp.modules.each {
				if (it.uiChannel)
					it.uiChannel.id = IdGenerator.get()
			}
		}
		
		Canvas canvas = new Canvas()
		canvas.type = Canvas.Type.RUNNING
		canvas.name = sp.name ?: "(unsaved canvas)"
		canvas.user = user
		canvas.json = (sp as JSON)
		canvas.state = Canvas.State.STARTING
		canvas.adhoc = adhoc

		UiChannel rspUi = new UiChannel()
		rspUi.id = sp.uiChannel.id
		canvas.addToUiChannels(rspUi)
		
		for (Map it : sp.modules) { 
			if (it.uiChannel) {
				UiChannel ui = new UiChannel()
				ui.id = it.uiChannel.id
				ui.hash = it.hash.toString()
				ui.module = Module.load(it.id)
				ui.name = it.uiChannel.name
				
				canvas.addToUiChannels(ui)
			}
		}
		
		canvas.save(flush:true, failOnError:true)
		return canvas
	}
	
	@Transactional
	public void deleteRunningSignalPathReferences(SignalPathRunner runner) {
		// Delayed-delete the topics in one hour
		List<UiChannel> channels = UiChannel.findAll { canvas.runner == runner.getRunnerId()}
		kafkaService.createDeleteTopicTask(channels.collect{it.id}, 60*60*1000)
		
		List uiIds = UiChannel.executeQuery("select ui.id from UiChannel ui where ui.canvas.runner = ?", [runner.getRunnerId()])
		if (!uiIds.isEmpty()) {
			UiChannel.executeUpdate("delete from UiChannel ui where ui.id in (:list)", [list:uiIds])
		}

		Canvas.executeUpdate("delete from Canvas c where c.runner = ?", [runner.getRunnerId()])
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

	/**
	 * @throws SerializationException if de-serialization fails when resuming from existing state
     */
	void startLocal(Canvas canvas, Map signalPathContext) throws SerializationException {
		// Create Globals
		Globals globals = GlobalsFactory.createInstance(signalPathContext, grailsApplication)
		globals.uiChannel = new KafkaPushChannel(kafkaService, canvas.adhoc)

		SignalPathRunner runner
		// Create the runner thread
		if (canvas.isNotSerialized() || canvas.adhoc) {
			runner = new SignalPathRunner([JSON.parse(canvas.json)], globals, canvas.adhoc)
			log.info("Creating new signalPath connections (canvasId=$canvas.id)")
		} else {
			SignalPath sp = serializationService.deserialize(canvas.serialized)
			runner = new SignalPathRunner(sp, globals, canvas.adhoc)
			log.info("De-serializing existing signalPath (canvasId=$canvas.id)")
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
			it.canvas = canvas
		}
		String runnerId = runner.runnerId
		
		// Start the runner thread
		runner.start()
		
		// Wait for runner to be in running state
		runner.waitRunning(true)
		if (!runner.getRunning()) {
			log.error("Timed out while waiting for runner $runnerId to start!")
		}

		canvas.runner = runnerId
		canvas.state = Canvas.State.RUNNING
		
		// Use the link generator to get the protocol and port, but use network IP address
		// as the host to get the address of this individual server
		String link = grailsLinkGenerator.link(controller: "liveApi", action: "request", absolute: true)
		URL url = new URL(link)
		
		canvas.server = NetworkInterfaceUtils.getIPAddress(grailsApplication.config.streamr.ip.address.prefixes ?: []).getHostAddress()
		canvas.requestUrl = url.protocol+"://"+canvas.server+":"+(url.port>0 ? url.port : url.defaultPort)+grailsLinkGenerator.link(uri:"/api/live/request")
		
		canvas.save()
	}
	
	boolean stopLocal(Canvas canvas) {
		SignalPathRunner runner = servletContext["signalPathRunners"]?.get(canvas.runner)
		if (runner!=null && runner.isAlive()) {
			runner.abort()
			
			// Wait for runner to be stopped state
			runner.waitRunning(false)
			if (runner.getRunning()) {
				log.error("Timed out while waiting for runner $canvas.runner to stop!")
				return false
			} else {
				return true
			}
		}
		else {
			log.error("stopLocal: could not find runner $canvas.runner!")
			updateState(canvas.runner, Canvas.State.STOPPED)
			return false
		}
	}
	
	@CompileStatic
	RuntimeResponse stopRemote(Canvas canvas, SecUser user) {
		return runtimeRequest([type:"stopRequest"], canvas, null, user)
	}
	
	@CompileStatic
	boolean ping(Canvas canvas, SecUser user) {
		RuntimeResponse response = runtimeRequest([type:'ping'], canvas, null, user)
		return response.isSuccess()
	}
	
	@CompileStatic
	RuntimeResponse sendRemoteRequest(Map msg, Canvas canvas, Integer hash, SecUser user) {
		def req = Unirest.post(canvas.requestUrl)
		def json = [
			local: true,
			msg: msg,
			id: canvas.id
		]

		if (hash) {
			json.hash = hash
		}
		if (user) {
			json.key = user.apiKey
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
	
	RuntimeResponse runtimeRequest(Map msg, Canvas canvas, Integer hash, SecUser user, boolean localOnly = false) {
		SignalPathRunner spr = servletContext["signalPathRunners"]?.get(canvas.runner)
		
		log.info("runtimeRequest: $msg, Canvas: $canvas.id, module: $hash, localOnly: $localOnly")
		
		// Give an error if the runner was not found locally although it should have been
		if (localOnly && !spr) {
			log.error("runtimeRequest: $msg, runner not found with localOnly=true, responding with error")
			return new RuntimeResponse([success:false, error: "Canvas does not appear to be running!"])
		}
		// May be a remote runner, check server and send a message
		else if (!localOnly && !spr) {
			try {
				return sendRemoteRequest(msg, canvas, hash, user)
			} catch (Exception e) {
				log.error("Unable to contact remote Canvas id $canvas.id at $canvas.requestUrl")
				return new RuntimeResponse([success:false, error: "Unable to communicate with remote server!"])
			}
		}
		// If runner found
		else {
			SignalPath sp = spr.signalPaths.find {
				it.canvas.id == canvas.id
			}
			
			if (!sp) {
				log.error("runtimeRequest: $msg, runner found but canvas not found. This should not happen. RSP: $canvas, module: $hash")
				return new RuntimeResponse([success:false, error: "Canvas not found in runner. This should not happen."])
			}
			else {				
				RuntimeRequest request = new RuntimeRequest(msg)
				request.setAuthenticated(user != null)
				
				/**
				 * Requests for the runner thread
				 */
				if (request.type=="stopRequest") {
					if (!request.isAuthenticated()) {
						throw new AccessControlException("stopRequest requires authentication!");
					}

					stopLocal(canvas);

					return new RuntimeResponse(true, [request: request])
				} else if (request.type=="ping") {
					if (!canvas.shared && !request.isAuthenticated())
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
	
	void updateState(String runnerId, Canvas.State state) {
		Canvas.executeUpdate("update Canvas c set c.state = ? where c.runner = ?", [state, runnerId])
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

	@Transactional
	def saveState(SignalPath sp) {
		Canvas canvas = sp.canvas
		canvas = canvas.attach()

		try {
			canvas.serialized = serializationService.serialize(sp)
			canvas.serializationTime = sp.globals.time
			Canvas.executeUpdate("update Canvas c set c.serialized = ?, c.serializationTime = ? where c.id = ?",
				[canvas.serialized, canvas.serializationTime, canvas.id])
			log.info("Canvas " + canvas.id + " serialized.")
		} catch (SerializationException ex) {
			log.error("Serialization of canvas " + canvas.id + " failed.")
			throw ex
		}
	}

	@Transactional
	def clearState(Canvas canvas) {
		canvas.serialized = null
		canvas.save(failOnError: true)
		log.info("Canvas " + canvas.id + " serialized state cleared.")
	}
}
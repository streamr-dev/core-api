package com.unifina.service

import com.unifina.api.CanvasCommunicationException
import com.unifina.datasource.DataSource
import com.unifina.datasource.HistoricalDataSource
import com.unifina.datasource.IStartListener
import com.unifina.datasource.IStopListener
import com.unifina.datasource.RealtimeDataSource
import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas
import com.unifina.domain.signalpath.Serialization
import com.unifina.exceptions.CanvasUnreachableException
import com.unifina.serialization.SerializationException
import com.unifina.signalpath.AbstractSignalPathModule
import com.unifina.signalpath.RuntimeRequest
import com.unifina.signalpath.RuntimeResponse
import com.unifina.signalpath.SignalPath
import com.unifina.signalpath.SignalPathRunner
import com.unifina.utils.Globals
import com.unifina.utils.GlobalsFactory
import com.unifina.utils.NetworkInterfaceUtils
import grails.converters.JSON
import grails.transaction.NotTransactional
import grails.transaction.Transactional
import groovy.transform.CompileStatic
import org.apache.log4j.Logger

import java.nio.charset.StandardCharsets
import java.security.AccessControlException
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

class SignalPathService {

    static transactional = false
	
	def servletContext
	def grailsApplication
	def grailsLinkGenerator
	def serializationService
	StreamService streamService
	PermissionService permissionService
	CanvasService canvasService
	ApiService apiService

	private static final Logger log = Logger.getLogger(SignalPathService.class)

	/**
	 * Creates and configures a root SignalPath instance with the given config and Globals. You
	 * can pass an optional SignalPath instance to configure if you want (eg. to configure non-root
	 * SignalPaths or subclasses of SignalPath).
	 *
	 * If connectionsReady==true, instance.connectionsReady() is called.
     */
	@CompileStatic
	public SignalPath mapToSignalPath(Map config, boolean connectionsReady, Globals globals, SignalPath instance = new SignalPath(true)) {
		instance.globals = globals
		instance.init()
		instance.configure(config)
		if (connectionsReady) {
			instance.connectionsReady()
		}

		return instance
	}

	@CompileStatic
	public Map signalPathToMap(SignalPath sp) {
		return  [
			name: sp.name,
			modules: sp.modules.collect { AbstractSignalPathModule it -> it.getConfiguration() },
			settings: sp.globals.signalPathContext,
			hasExports: sp.hasExports(),
			uiChannel: sp.getUiChannel().toMap()
		]
	}
	
	/**
	 * Rebuilds a saved representation of a root SignalPath along with its config.
	 * Potentially modifies the config given as parameter.
	 */
	@CompileStatic
	public Map reconstruct(Map config, Globals globals) {
		SignalPath sp = mapToSignalPath(config, true, globals, new SignalPath(true))
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

	@CompileStatic
	public DataSource createDataSource(boolean adhoc, Globals globals) {
		if (adhoc) {
			return new HistoricalDataSource(globals)
		} else {
			return new RealtimeDataSource(globals)
		}
	}

	@Transactional
	public void deleteReferences(SignalPath signalPath, boolean delayed = false) {
		canvasService.deleteCanvas(signalPath.canvas, signalPath.getGlobals().getUser(), delayed)
	}
	
    def runSignalPaths(List<SignalPath> signalPaths) {
		// Check that all the SignalPaths share the same Globals object
		Globals globals = null
		for (SignalPath sp : signalPaths) {
			if (globals==null)
				globals = sp.globals
			else if (globals!= sp.globals)
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
		Globals globals = GlobalsFactory.createInstance(signalPathContext, grailsApplication, canvas.user)

		SignalPathRunner runner
		// Create the runner thread
		if (canvas.serialization == null || canvas.adhoc) {
			runner = new SignalPathRunner([JSON.parse(canvas.json)], globals, canvas.adhoc)
			log.info("Creating new signalPath connections (canvasId=$canvas.id)")
		} else {
			SignalPath sp = serializationService.deserialize(canvas.serialization.bytes)
			runner = new SignalPathRunner(sp, globals, canvas.adhoc)
			log.info("De-serializing existing signalPath (canvasId=$canvas.id)")
		}

		runner.addStartListener(new IStartListener() {
			@Override
			void onStart() {
				if (!servletContext["signalPathRunners"]) {
					servletContext["signalPathRunners"] = [:]
				}
				servletContext["signalPathRunners"].put(runner.runnerId, runner)
			}
		})

		runner.addStopListener(new IStopListener() {
			@Override
			void onStop() {
				servletContext["signalPathRunners"].remove(runner.runnerId)
			}
		})

		runner.signalPaths.each {
			it.canvas = canvas
		}

		String runnerId = runner.runnerId
		canvas.runner = runnerId

		// Use the link generator to get the protocol and port, but use network IP address
		//   as the host to get the address of this individual server
		String root = grailsLinkGenerator.link(uri:"/", absolute: true)
		URL url = new URL(root)

		canvas.server = NetworkInterfaceUtils.getIPAddress(grailsApplication.config.streamr.ip.address.prefixes ?: []).getHostAddress()
		canvas.requestUrl = url.protocol+"://"+canvas.server+":"+(url.port>0 ? url.port : url.defaultPort)+grailsLinkGenerator.link(uri:"/api/v1/canvases/$canvas.id", absolute: false)
		canvas.state = Canvas.State.RUNNING

		canvas.save()

		// Start the runner thread
		runner.start()

		// Wait for runner to be in running state
		runner.waitRunning(true)
		if (!runner.getRunning()) {
			runner.abort()
			def msg = "Timed out while waiting for canvas $canvas.id to start."
			throw new CanvasCommunicationException(msg)
		}
	}

	List<Canvas> stopAllLocalCanvases() {
		// Copy list to prevent ConcurrentModificationException
		Map runners = [:]
		runners.putAll(servletContext["signalPathRunners"])
		List canvases = []
		runners.each { String key, SignalPathRunner runner ->
			if (stopLocalRunner(key)) {
				canvases.addAll(runner.getSignalPaths().collect {it.getCanvas()})
			}
		}
		return canvases
	}

	boolean stopLocalRunner(String runnerId) {
		SignalPathRunner runner = servletContext["signalPathRunners"]?.get(runnerId)
		if (runner!=null) {
			runner.abort()

			// Wait for runner to be in stopped state
			runner.waitRunning(false)
			if (runner.getRunning()) {
				log.error("Timed out while waiting for runner $runnerId to stop!")
				return false
			} else {
				return true
			}
		}
		else {
			log.error("stopLocal: could not find runner $runnerId!")
			updateState(runnerId, Canvas.State.STOPPED)
			return false
		}
	}

	boolean stopLocal(Canvas canvas) {
		return stopLocalRunner(canvas.runner)
	}

	@NotTransactional
	@CompileStatic
	Map stopRemote(Canvas canvas, SecUser user) {
		return runtimeRequest(buildRuntimeRequest([type:"stopRequest"], "canvases/$canvas.id", user))
	}

	@CompileStatic
	boolean ping(Canvas canvas, SecUser user) {
		runtimeRequest(buildRuntimeRequest([type:'ping'], "canvases/$canvas.id", user))
		return true
	}

	@CompileStatic
	private Map sendRemoteRequest(RuntimeRequest req) {
		// Require the request to be local to the receiving server to avoid redirect loops in case of invalid data
		String url = req.getCanvas().getRequestUrl().replace("canvases/${req.getCanvas().id}", req.getOriginalPath() + "/request?local=true")
		return apiService.post(url, req, req.getUser().keys.iterator().next())
	}

	private SignalPathRunner getLocalRunner(Canvas canvas) {
		return servletContext["signalPathRunners"]?.get(canvas.runner)
	}

	@CompileStatic
	RuntimeRequest buildRuntimeRequest(Map msg, String path, String originalPath = path, SecUser user) {
		RuntimeRequest.PathReader pathReader = RuntimeRequest.getPathReader(path)

		// All runtime requests require at least read permission
		Canvas canvas = canvasService.authorizedGetById(pathReader.readCanvasId(), user, Permission.Operation.READ)
		Set<Permission.Operation> checkedOperations = new HashSet<>()
		checkedOperations.add(Permission.Operation.READ)

		RuntimeRequest request = new RuntimeRequest(msg, user, canvas, path, originalPath, checkedOperations)
		return request
	}

	@CompileStatic
	Map runtimeRequest(RuntimeRequest req, boolean localOnly = false) {
		SignalPathRunner spr = getLocalRunner(req.getCanvas())
		
		log.info("runtimeRequest: $req, path: ${req.getPath()}, localOnly: $localOnly")
		
		// Give an error if the runner was not found locally although it should have been
		if (localOnly && !spr) {
			log.error("runtimeRequest: $req, runner not found with localOnly=true, responding with error")
			throw new CanvasUnreachableException("Canvas does not appear to be running!")
		}
		// May be a remote runner, check server and send a message
		else if (!localOnly && !spr) {
			try {
				return sendRemoteRequest(req)
			} catch (Exception e) {
				log.error("Unable to contact remote Canvas id ${req.getCanvas().id} at ${req.getCanvas().requestUrl}")
				throw new CanvasUnreachableException("Unable to communicate with remote server!")
			}
		}
		// If runner found
		else {
			SignalPath sp = spr.signalPaths.find {SignalPath it->
				it.canvas.id == req.getCanvas().id
			}
			
			if (!sp) {
				log.error("runtimeRequest: $req, runner found but canvas not found. This should not happen. Canvas: ${req.canvas}, path: ${req.path}")
				throw new CanvasUnreachableException("Canvas not found in runner. This should not happen.")
			}
			else {
				/**
				 * Special handling for runner thread stop request
				 */
				if (req.type=="stopRequest") {
					if (!permissionService.canWrite(req.getUser(), req.getCanvas())) {
						throw new AccessControlException("stopRequest requires write permission!");
					}

					if (stopLocal(req.getCanvas())) {
						return req
					}
					else {
						throw new CanvasUnreachableException("Canvas could not be stopped.")
					}
				}
				/**
				 * Requests for SignalPaths and modules within them
				 */
				else {
					RuntimeRequest.PathReader pathReader = req.getPathReader()

					// Consume the already-processed parts of the path and double-sanity-check canvas id
					if (pathReader.readCanvasId() != req.getCanvas().getId()) {
						throw new IllegalStateException("Unexpected path: ${req.getPath()}")
					}

					Future<RuntimeResponse> future = sp.onRequest(req, pathReader)
					
					try {
						RuntimeResponse resp = future.get(30, TimeUnit.SECONDS)
						log.debug("runtimeRequest: responding with $resp")
						return resp
					} catch (TimeoutException e) {
						throw new CanvasUnreachableException("Timed out while waiting for response.")
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
		long startTime = System.currentTimeMillis()
		Canvas canvas = Canvas.get(sp.canvas.id)

		try {
			boolean isFirst = (canvas.serialization == null)
			Serialization serialization = isFirst ? new Serialization(canvas: canvas) : canvas.serialization

			// Serialize
			byte[] bytes = serializationService.serialize(sp)
			boolean notTooBig = bytes.length <= serializationService.serializationMaxBytes()

			if (notTooBig) {
                serialization.bytes = serializationService.serialize(sp)
                serialization.date = sp.globals.time
                serialization.save(failOnError: true, flush: true)
                canvas.serialization = serialization

                if (isFirst) {
                    Canvas.executeUpdate("update Canvas c set c.serialization = ? where c.id = ?", [serialization, canvas.id])
                }
			}

			long timeTaken = System.currentTimeMillis() - startTime
			String stats = "(size: ${bytes.length} bytes, processing time: ${timeTaken} ms)"
			if (notTooBig) {
				log.info("Canvas " + canvas.id + " serialized " + stats)
			} else {
				log.info("Canvas " + canvas.id + " serialization skipped because too large " + stats)
			}
		} catch (SerializationException ex) {
			log.error("Serialization of canvas " + canvas.id + " failed.")
			throw ex
		} finally {
			// Save memory by removing reference to the bytes to get them gc'ed
			canvas.serialization?.bytes = null
		}
	}

	@Transactional
	def clearState(Canvas canvas) {
		canvas.serialization?.delete()
		canvas.serialization = null
		canvas.save(failOnError: true)
		log.info("Canvas $canvas.id serialized state cleared.")
	}
}

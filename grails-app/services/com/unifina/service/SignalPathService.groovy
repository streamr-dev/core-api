package com.unifina.service

import grails.converters.JSON
import grails.transaction.Transactional

import java.nio.charset.StandardCharsets
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

import org.apache.log4j.Logger

import com.unifina.datasource.BacktestDataSource
import com.unifina.datasource.DataSource
import com.unifina.datasource.RealtimeDataSource
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Module
import com.unifina.domain.signalpath.RunningSignalPath
import com.unifina.domain.signalpath.UiChannel
import com.unifina.push.IHasPushChannel
import com.unifina.push.KafkaPushChannel
import com.unifina.push.PushChannelEventListener
import com.unifina.signalpath.AbstractSignalPathModule
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
	def kafkaService
	
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
		
		// TODO: remove backwards compatibility
		Map signalPathData = json.signalPathData ? json.signalPathData : json
		
		SignalPath sp = jsonToSignalPath(signalPathData,true,globals,true)
		
		Map context = [:]
		if (json.signalPathContext)
			context = json.signalPathContext
		// TODO: remove backwards compatibility
		else {
			if (json.timeOfDayFilter)
				context["timeOfDayFilter"] = json.timeOfDayFilter
		}
		
		json.signalPathContext = context
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
		globals.uiChannel = new KafkaPushChannel(kafkaService)
		
		// Create the runner thread
		SignalPathRunner runner = new SignalPathRunner([JSON.parse(rsp.json)], globals, rsp.adhoc)
		String runnerId = runner.runnerId
		
		// Start the runner thread
		runner.start()
		
		rsp.runner = runnerId
		rsp.state = "running"
		rsp.save()
	}
	
	void stopLocal(RunningSignalPath rsp) {
		SignalPathRunner runner = servletContext["signalPathRunners"]?.get(rsp.runner)
		if (runner!=null && runner.isAlive()) {
			runner.abort()
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

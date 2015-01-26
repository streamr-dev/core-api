package com.unifina.service

import grails.converters.JSON
import grails.transaction.Transactional

import java.nio.charset.StandardCharsets
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

import org.apache.log4j.Logger

import com.unifina.datasource.BacktestDataSource
import com.unifina.datasource.DataSource
import com.unifina.domain.security.SecUser
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
import com.unifina.utils.NetworkInterfaceUtils

class SignalPathService {

    static transactional = false
	
	def servletContext
	def grailsApplication
	def kafkaService
	
	private static final Logger log = Logger.getLogger(SignalPathService.class)
	
	public SignalPath jsonToSignalPath(Map signalPathData, boolean connectionsReady, Globals globals, boolean isRoot) {
		SignalPath sp = new SignalPath(signalPathData,isRoot,globals)
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
		if (signalPathContext.dataSource==null)
			return new BacktestDataSource(globals)
			
		String dataSourceClass = signalPathContext.dataSource

		// Instantiate DataSource dynamically
		def dsObject = this.getClass().getClassLoader().loadClass(dataSourceClass).newInstance(globals)
		DataSource ds = (DataSource)dsObject

		return ds
	}
	
	public List<RunningSignalPath> launch(List<Map> signalPathData, Map signalPathContext, SecUser user) {
		
		// Create Globals
		Globals globals = GlobalsFactory.createInstance(signalPathContext, grailsApplication)
		globals.uiChannel = new KafkaPushChannel(kafkaService)
		
		// Create the runner thread
		SignalPathRunner runner = new SignalPathRunner(signalPathData, globals)
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
		return createRunningSignalPathReferences(runner, user)
	}
	
	@Transactional
	public List<RunningSignalPath> createRunningSignalPathReferences(SignalPathRunner runner, SecUser user) {
		
		return runner.getSignalPaths().collect {SignalPath sp->
			RunningSignalPath rsp = new RunningSignalPath()
			rsp.name = sp.name
			rsp.user = user
			rsp.runner = runner.getRunnerId()
			rsp.server = NetworkInterfaceUtils.getIPAddress()
			rsp.json = (signalPathToJson(sp) as JSON)
			
			UiChannel rspUi = new UiChannel()
			rspUi.id = sp.getUiChannelId()
			rsp.addToUiChannels(rspUi)
			
			for (AbstractSignalPathModule it : sp.getModules()) {
				if (it instanceof IHasPushChannel) {
					UiChannel ui = new UiChannel()
					ui.id = ((IHasPushChannel) it).getUiChannelId()
					ui.hash = it.getHash().toString()
					ui.module = it.getDomainObject()
					
					rsp.addToUiChannels(ui)
				}
			}
			
			rsp.save(flush:true, failOnError:true)
			
			return rsp
		}
	}
	
	@Transactional
	public void deleteRunningSignalPathReferences(SignalPathRunner runner) {
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
	
	def stopSignalPath(SignalPath signalPath) {
		// Stop feed
		signalPath.globals.dataSource.stopFeed()
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

package com.unifina.signalpath;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import com.unifina.utils.HibernateHelper;
import org.apache.log4j.Logger;

import com.unifina.data.FeedEvent;
import com.unifina.data.IEventRecipient;
import com.unifina.datasource.IDayListener;
import com.unifina.domain.signalpath.Module;
import com.unifina.utils.Globals;
import com.unifina.utils.MapTraversal;

/**
 * The usual init procedure:
 * - Construct the module
 * - Call module.init()
 * - Call module.setName()
 * - Call module.setConfiguration()
 * - Call module.connectionsReady()
 */
public abstract class AbstractSignalPathModule implements IEventRecipient, IDayListener, Serializable {

	private Map<String,Object> json;

	protected SignalPath parentSignalPath;
	transient private SignalPath cachedTopParentSignalPath;
	protected Integer initPriority = 50;
	
	protected ArrayList<Input> inputs = new ArrayList<Input>();
	protected Map<String,Input> inputsByName = new HashMap<String,Input>();
	
	protected ArrayList<Output> outputs = new ArrayList<Output>();
	protected Map<String,Output> outputsByName = new HashMap<String,Output>();
	
	private boolean wasReady = false;
	
	private int readyInputs = 0;
	private int inputCount = 0;
	
	boolean sendPending = false;
	
	// If this is set to true, this module will be a leaf in all Propagator trees
	protected boolean propagationSink = false;
	
	Module domainObject;
	
	protected HashSet<Input> drivingInputs = new HashSet<Input>();
	
	Date lastCleared = null;
	
	/**
	 * Module params
	 */
	protected boolean canClearState = true;
	boolean clearState = false;
	
	protected boolean canRefresh = false;
	
	protected String name;
	protected Integer hash;
	
	transient public Globals globals;

	private boolean initialized;
	
	private static final Logger log = Logger.getLogger(AbstractSignalPathModule.class);
	
	/**
	 * This propagator is created and used if an UI event triggers 
	 * this module to send output. This can happen, for example, if
	 * a Parameter is marked as driving and then the Parameter is changed
	 * at runtime.
	 */
	transient protected Propagator uiEventPropagator = null;
	
	public AbstractSignalPathModule() {

	}
	
	/**
	 * This is called immediately after instantiation, and it should
	 * call addInput or addOutput for all inputs and outputs in the module.
	 * The default implementation obtains the declared inputs and outputs via 
	 * reflection. They are sorted in alphabetic order.  If you want to control
	 * the order in which IO is shown on the module, then override this method
	 * and call addInput() or addOutput() in the order you want.
	 */
	public void init() {
		
		/**
		 * Execute in a privileged block so that also user defined
		 * untrusted modules can benefit from auto-initialization of IO. 
		 */
        AccessController.doPrivileged(
	        new PrivilegedAction<Object>() {
	            public Object run() {
	            	
					// Loop through class hierarchy and collect declared fields
					List<Field> fieldList = new ArrayList<>();
					Class clazz = AbstractSignalPathModule.this.getClass();
					do {
						fieldList.addAll(Arrays.asList(clazz.getDeclaredFields()));
						clazz = clazz.getSuperclass();
					} while (clazz != null);

					Field[] fields = fieldList.toArray(new Field[fieldList.size()]);
	    			
	    			// Sort by field name
	    			Arrays.sort(fields, new Comparator<Field>() {
						@Override
						public int compare(Field o1, Field o2) {
							return o1.getName().compareTo(o2.getName());
						}
	    			});
	    			
	    			for (Field f : fields) {
	    				try {
	    					// This is required to avoid java.lang.IllegalAccessException and requires privileges
	    					f.setAccessible(true);
	    					Object obj = f.get(AbstractSignalPathModule.this);
	    					if (Input.class.isInstance(obj) && !inputs.contains(obj) && f.getAnnotation(ExcludeInAutodetection.class) == null) {
								addInput((Input) obj);
							}
	    					else if (Output.class.isInstance(obj) && !outputs.contains(obj) && f.getAnnotation(ExcludeInAutodetection.class) == null) {
								addOutput((Output) obj);
							}
	    				} catch (Exception e) {
	    					log.error("Could not get field: "+f+", class: "+AbstractSignalPathModule.this.getClass()+" due to exception: "+e);
	    				} finally {
	    					// Set the field back to non-accessible
	    					f.setAccessible(false);
	    				}
	    			}
					return null;
	            }
	        });
	}
	
	/**
	 * This method is for local initialization of the module. It
	 * gets called after the module is set up, ie. after the connections
	 * have been made. The default implementation does nothing.
	 */
	public void initialize() {
		
	}
	
	public void addInput(Input input) {
		addInput(input,input.getName());
	}
	
	protected void addInput(Input input, String name) {
		if (inputs.contains(input))
			inputs.remove(input);
		
		inputs.add(input);
		
		inputsByName.put(name,input);
		for (Object alias : input.getAliases())
			inputsByName.put(alias.toString(), input);
		
		inputCount = inputs.size();
		
		// re-count because inputs already contained in the counters might be added
		checkDirtyAndReadyCounters();
	}
	
	public void addOutput(Output output) {
		addOutput(output,output.getName());
	}
	
	protected void addOutput(Output output, String name) {
		if (outputs.contains(output))
			outputs.remove(output);
		
		outputs.add(output);
		outputsByName.put(name,output);
		
		for (Object alias : output.getAliases())
			outputsByName.put(alias.toString(), output);
	}
	
	public Input[] getInputs() {
		return inputs.toArray(new Input[inputs.size()]);
	}

	public Output[] getOutputs() {
		return outputs.toArray(new Output[outputs.size()]);
	}
	
	public Input getInput(String name) {
		return inputsByName.get(name);
	}

	public Output getOutput(String name) {
		return outputsByName.get(name);
	}
	
	public void markReady(Input input) {
		readyInputs++;
	}
	
	public void cancelReady(Input input) {
		readyInputs--;
	}
	
	public boolean allInputsReady() {
		return readyInputs==inputCount;
	}
	
	public abstract void sendOutput();

	public void clear() {
		if (clearState && (lastCleared==null || lastCleared.before(globals.time))) {
			lastCleared = globals.time;
			clearState();
			
			for (Output o : getOutputs())
				o.doClear();
			for (Input i : getInputs())
				i.doClear();
			
			checkDirtyAndReadyCounters();
		}
	}
	public abstract void clearState();

	public boolean isClearState() {
		return clearState;
	}

	public void setClearState(boolean clearState) {
		this.clearState = clearState;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isSendPending() {
		return sendPending;
	}

	public void setSendPending(boolean sendPending) {
		this.sendPending = sendPending;
	}
	
	public boolean wasReady() {
		return wasReady;
	}
	
	/**
	 * Used to signal that all the connections to this output 
	 * have been made. 
	 * 
	 * Can be called multiple times for modules implementing
	 * the ISharedInstance interface, once for each SignalPath
	 * the module exists in!
	 */
	public void connectionsReady() {

		initialize();
		
		// Only report the initialization of this module once
		if (!initialized) {
			globals.onModuleInitialized(this);
			initialized = true;
		}
	}

	/**
	 * By default creates one Propagator containing all outputs of the module.
	 */
	protected Output[][] getSpecialPropagatorDefinitions() {
		return new Output[][] {};
	}
	
	private void findFeedbackConnections() {
		ArrayList<AbstractSignalPathModule> traversedModules = new ArrayList<AbstractSignalPathModule>();
		ArrayList<Input> traversedInputs = new ArrayList<Input>();
		traversedModules.add(this);
		for (Output o : getOutputs())
			recurseFeedbackConnections(o,traversedModules,traversedInputs);
	}

	private void recurseFeedbackConnections(Output output,
			ArrayList<AbstractSignalPathModule> traversedModules,ArrayList<Input> traversedInputs) {
		
		Input[] inputs = output.getTargets();
		HashSet<AbstractSignalPathModule> newModules = new HashSet<AbstractSignalPathModule>();
		for (Input i : inputs) {
			// Never traverse feedback connections any further
			if (i.isFeedbackConnection())
				continue;
			
			AbstractSignalPathModule module = i.getOwner();
			// Mark as feedback if traversed contains the input owner
			if (traversedModules.contains(module)) {
				i.setFeedbackConnection(true);
				System.out.println("Found feedback connection: "+traversedModules+" -> "+output+" -> "+i);
				
				// TODO: remove this exception if an automatic algorithm can be made to work
				throw new IllegalStateException("Infinite cycle found! You must mark some input(s) as feedback! "+traversedModules+" -> "+output+" -> "+i);
				
				// TODO: The previous one was not a sufficient algorithm. One possibility is to keep track of
				// the whole traversal tree and mark as feedback connections all connections that connect
				// to any ancestor element. Another possibility is that no unambiguous solution exists
				// and the fb connections must be marked by hand.
				
//				int pos = traversedInputs.size()-1;
//				
//				while (pos>=0) {
//					Input parentInput = traversedInputs.get(pos);
//					int count = 0;
//					for (Input potential : parentInput.getOwner().getInputs()) {
//						// Depends on starting point?
//						if (potential.dependsOn(traversedModules.get(0)))
//							count++;
//					}
//					if (count > 1) {
//						parentInput.setFeedbackConnection(true);
//						System.out.println("Found dependant feedback connection: "+parentInput.getSource() + " -> " + parentInput);
//						pos--;
//					}
//					else break;
//				}
			}
			else {
				// Collect modules traversed in this step. Several of these inputs could be in the same module.
				boolean notYetTraversed = newModules.add(module);
				if (notYetTraversed) {
					for (Output o : module.getOutputs()) {
						// Make a copy of traversed
						ArrayList<AbstractSignalPathModule> newTraversedModules = new ArrayList<AbstractSignalPathModule>();
						newTraversedModules.addAll(traversedModules);
						// Add this module to the set
						newTraversedModules.add(module);
						
						ArrayList<Input> newTraversedInputs = new ArrayList<Input>();
						newTraversedInputs.addAll(traversedInputs);
						newTraversedInputs.add(i);
						
						recurseFeedbackConnections(o,newTraversedModules,newTraversedInputs);
					}
				}
			}
			
		}
	}

	public void trySendOutput() {
		if (isSendPending() && allInputsReady()) {
			wasReady = true;
			setSendPending(false);
			sendOutput();
			drivingInputs.clear();
		}
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	@SuppressWarnings("rawtypes")
	public Map<String,Object> getConfiguration() {
		Map<String,Object> map = (json != null ? json : new HashMap<String,Object>());
		
		List<Map> params = new ArrayList<Map>();
		List<Map> ins = new ArrayList<Map>();
		List<Map> outs = new ArrayList<Map>();
		for (Input i : getInputs())
			if (i instanceof Parameter)
				params.add(i.getConfiguration());
			else ins.add(i.getConfiguration());
		
		for (Output o : getOutputs())
			outs.add(o.getConfiguration());
		
		map.put("params",params);
		map.put("inputs",ins);
		map.put("outputs",outs);
		
		map.put("name",name);
		
		map.put("canClearState", canClearState);
		map.put("clearState", clearState);
		
		if (canRefresh)
			map.put("canRefresh", canRefresh);
		
		return map;
	}
	
	/**
	 * Configures the module and its IO.
	 * @param config
	 */
	public void configure(Map<String,Object> config) {
		setConfiguration(config);
		setIOConfiguration(config,true);
		onConfiguration(config);
		// Ignore not found inputs here too. For example, if the
		// number of inputs in TimeSeriesChart is made smaller, some
		// inputs in the config are not found anymore.
		setIOConfiguration(config,true);
	}
	
	/**
	 * Gets called when the module and (static) io have been
	 * configured. After calling this the io will be configured
	 * again in case new io has been created. The default
	 * implementation does nothing.
	 * @param config
	 */
	protected void onConfiguration(Map<String,Object> config) {
		
	}
	
	
	private void setIOConfiguration(Map<String,Object> config, boolean ignoreNotFound) {
		// Set IO config automatically
		if (config.get("params")!=null) {
			for (Map modConf : (List<Map>)config.get("params")) {
				Input i = getInput((String)modConf.get("name"));
				if (i==null && !ignoreNotFound)
					throw new RuntimeException("Parameter not found: "+modConf.get("name"));
				else if (i!=null)
					i.setConfiguration(modConf);
			}
		}
		// There may be new Parameters added since the creation of the config map.
		// These need to receive the default value in order to be marked ready.
		for (Input i : getInputs()) {
			if (i instanceof Parameter) {
				Parameter p = (Parameter) i;
				if (!p.isConfigured()) {
					p.setConfiguration(p.getConfiguration());
				}
			}
		}
		
		if (config.get("inputs")!=null) {
			for (Map modConf : (List<Map>)config.get("inputs")) {
				Input i = getInput((String)modConf.get("name"));
				if (i==null && !ignoreNotFound)
					throw new RuntimeException("Input not found: "+modConf.get("name"));
				else if (i!=null)
					i.setConfiguration(modConf);
			}
		}
		if (config.get("outputs")!=null) {
			for (Map modConf : (List<Map>)config.get("outputs")) {
				Output o = getOutput((String)modConf.get("name"));
				if (o==null && !ignoreNotFound)
					throw new RuntimeException("Output not found: "+modConf.get("name"));
				else if (o!=null)
					o.setConfiguration(modConf);
			}
		}
	}
	
	private void setConfiguration(Map<String,Object> config) {
		json = config;
		
		if (config.containsKey("clearState"))
			clearState = Boolean.parseBoolean(config.get("clearState").toString());
		
		if (config.containsKey("hash"))
			hash = Integer.parseInt(config.get("hash").toString());
		
		// Embedded modules inherit the hash-id of their parents
		if (parentSignalPath!=null && parentSignalPath.getHash()!=null)
			hash = parentSignalPath.getHash();
	}

	public Module getDomainObject() {
		return domainObject;
	}

	public void setDomainObject(Module domainObject) {
		this.domainObject = domainObject;
	}

	public Integer getHash() {
		return hash;
	}

	public void setHash(Integer hash) {
		this.hash = hash;
	}

	/**
	 * This method will be called when execution of the SignalPath ends.
	 */
	public void destroy() {
		
	}

	public SignalPath getTopParentSignalPath() {
		if (parentSignalPath==null) return null;
		// Return cached value
		else if (cachedTopParentSignalPath !=null)
			return cachedTopParentSignalPath;
		// Establish cached value
		else {
			cachedTopParentSignalPath = parentSignalPath;
			while (cachedTopParentSignalPath.getParentSignalPath()!=null)
				cachedTopParentSignalPath = cachedTopParentSignalPath.getParentSignalPath();

			return cachedTopParentSignalPath;
		}
	}
	
	public SignalPath getParentSignalPath() {
		return parentSignalPath;
	}

	public void setParentSignalPath(SignalPath parentSignalPath) {
		this.parentSignalPath = parentSignalPath;
	}

	public Integer getInitPriority() {
		return initPriority;
	}
	
	/**
	 * This method is used to serve requests from the UI to individual modules. 
	 * The default implementation inserts the requests into the global event queue
	 * as a FeedEvent. It returns a Future that will hold the RuntimeResponse that
	 * results when the event is processed from the queue.
	 * 
	 * This method is not intended to be subclassed. To implement handling of requests,
	 * subclass the handleRequest() method.
	 * 
	 * @param request The RuntimeRequest, which should contain at least the key "type", holding a String and indicating the type of request
	 */
	public Future<RuntimeResponse> onRequest(final RuntimeRequest request) {
		// Add event to message queue, don't do it right away 
		FeedEvent<RuntimeRequest, AbstractSignalPathModule> fe = new FeedEvent<>(request, globals.isRealtime() ? request.getTimestamp() : globals.time, this);

		final RuntimeResponse response = new RuntimeResponse();
		response.put("request", request);
		
		FutureTask<RuntimeResponse> future = new FutureTask<>(new Runnable() {
			@Override
			public void run() {
				try {
					handleRequest(request, response);
				} catch (AccessControlException e) {
					String error = "Unauthenticated request! Type: "+request.getType()+", Msg: "+e.getMessage();
					log.error(error);
					response.setSuccess(false);
					response.put("error", error);
				}
			}
		}, response);
		request.setFuture(future);
		
		globals.getDataSource().getEventQueue().enqueue(fe);
		return future;
	}
	
	@Override
	public void receive(FeedEvent event) {
		if (event.content instanceof RuntimeRequest) {
			RuntimeRequest request = (RuntimeRequest) event.content;
			((FutureTask)request.getFuture()).run();
		}
		else log.warn("receive: Unidentified FeedEvent content: "+event.content);
	}
	
	/**
	 * This method handles RuntimeRequests. Handlers can check if the request is made by an
	 * authenticated user by checking request.isAuthenticated(). An AccessControlException
	 * should be thrown for secure but unauthenticated requests.
	 * 
	 * If you override this method, be sure to call super.handleRequest(request,response) if the
	 * request type is not processed in the subclass.
	 * 
	 * Key-value pairs can be added to the response via response.put(key, value). Also
	 * you should call response.setSuccess(true) to indicate successful processing of the message.
	 * 
	 * @param request
	 */
	protected void handleRequest(RuntimeRequest request, RuntimeResponse response) {
		// By default all modules support runtime parameter changes
		if (request.getType().equals("paramChange")) {
			
			Parameter param = (Parameter) getInput(request.get("param").toString());
			try {
				
				if (!request.isAuthenticated()) {
					throw new AccessControlException("Unauthenticated parameter change request!");
				}
				
				Object value = param.parseValue(request.get("value").toString());
				param.receive(value);
				
				if (isSendPending()) {
					if (uiEventPropagator==null) {
						uiEventPropagator = new Propagator();
						uiEventPropagator.addModule(this);
					}
					trySendOutput();
					if (wasReady) {
						uiEventPropagator.propagate();
					}
				}
				
				response.setSuccess(true);
			} catch (Exception e) {
				log.error("Error making runtime parameter change!",e);
				globals.getUiChannel().push(new ErrorMessage("Parameter change failed!"), parentSignalPath.getUiChannelId());
			}
		}
	}

	public void checkDirtyAndReadyCounters() {
		// Set the quick dirty/ready counters
		int readyCount = 0;
		for (Input i : getInputs()) {
			if (i.isReady())
				readyCount++;
		}

		readyInputs = readyCount;
	}
	
	@Override
	public void onDay(Date day) {

	}
	
	public Map<String,Object> addOption(Map<String,Object> config, String name, String type, Object value) {
		if (config.get("options")==null)
			config.put("options", new HashMap<String,Object>());
		
		Map<String, Object> options = (Map<String, Object>) config.get("options");
		Map<String, Object> item = new HashMap<>();
		options.put(name, item);
		item.put("type",type);
		item.put("value",value);
		return item;
	}
	
	public Object getOption(Map<String,Object> config, String name) {
		return MapTraversal.getProperty(config, "options."+name+".value");
	}

	/**
	 * Override to handle steps before serialization.
	 * Don't forget to call super.beforeSerialization()!
	 */
	public void beforeSerialization() {
		domainObject = HibernateHelper.deproxy(domainObject, Module.class);
	}

	/**
	 * Override to handle steps after serialization
	 */
	public void afterSerialization() {}

	/**
	 * Override to handle steps after deserialization
	 */
	public void afterDeserialization() {
	}
}

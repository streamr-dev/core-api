package com.unifina.feed;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

import com.unifina.data.FeedEvent;
import com.unifina.data.IEventRecipient;
import com.unifina.datasource.IStartListener;
import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.Propagator;
import com.unifina.utils.Globals;

/**
 * An AbstractEventRecipient receives FeedEvents and updates the outputs
 * of registered AbstractSignalPathModules accordingly. It then handles the
 * propagation of events from those outputs through the SignalPaths in which
 * those modules reside.
 * @author Henri
 *
 * @param <ModuleClass> The type of objects than can be registered
 */
public abstract class AbstractEventRecipient<ModuleClass, MessageClass extends ITimestamped> implements IEventRecipient, IStartListener {

	protected ArrayList<ModuleClass> modules = new ArrayList<>();
	protected int moduleSize = 0;
	
	protected Propagator propagator = new Propagator();
	
	private Class<?> clazz;

	protected Globals globals;
	
	public AbstractEventRecipient(Globals globals) {
		this.globals = globals;
		if (globals!=null && globals.getDataSource()!=null)
			globals.getDataSource().addStartListener(this);
	}
	
	@Override
	public void onStart() {
		propagator.initialize();
	}
	
	public Class<?> getParameterizedClass() {
		if(clazz == null) {
			ParameterizedType pt = (ParameterizedType)this.getClass().getGenericSuperclass();
			clazz = (Class<?>)pt.getActualTypeArguments()[0];
		}
		return clazz;
	}
	
	public void register(ModuleClass module) {
		if (!getParameterizedClass().isAssignableFrom(module.getClass()))
			throw new IllegalArgumentException("Can not register module of type: "+module.getClass()+", required type: "+getParameterizedClass()+". Module: "+module);
		else if (!modules.contains(module)) {
			modules.add(module);
			moduleSize = modules.size();
			
			if (module instanceof AbstractSignalPathModule) {
				AbstractSignalPathModule m = (AbstractSignalPathModule) module;
				propagator.addModule(m);
			}
		}
	}
	
	@Override
	public void receive(FeedEvent event) {
		sendOutputFromModules(event);
		propagator.propagate();
	}

	public Propagator getPropagator() {
		return propagator;
	}
	
	public List<ModuleClass> getModules() {
		return modules;
	}
	
	/**
	 * This method should process the event, then if necessary, loop through each 
	 * registered module and send appropriate values from the outputs of connected 
	 * modules. Propagation will take place immediately after calling this method.
	 * @param event
	 */
	protected abstract void sendOutputFromModules(FeedEvent<MessageClass, ? extends IEventRecipient> event);
	
}

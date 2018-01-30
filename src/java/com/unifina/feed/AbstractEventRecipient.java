package com.unifina.feed;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collections;
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

	private final List<ModuleClass> modules = new ArrayList<>();
	private final Propagator propagator = new Propagator();
	private final Class<?> parameterizedClass;
	
	public AbstractEventRecipient(Globals globals) {
		if (globals != null && globals.getDataSource() != null) {
			globals.getDataSource().addStartListener(this);
		}
		ParameterizedType pt = (ParameterizedType) this.getClass().getGenericSuperclass();
		parameterizedClass = (Class<?>) pt.getActualTypeArguments()[0];
	}
	
	@Override
	public void onStart() {
		propagator.initialize();
	}
	
	public void register(ModuleClass module) {
		if (!parameterizedClass.isAssignableFrom(module.getClass())) {
			throw new IllegalArgumentException("Can not register module of type: " + module.getClass() +
				", required type: " + parameterizedClass + ". Module: " + module);
		} else if (!modules.contains(module)) {
			modules.add(module);
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
	
	protected List<ModuleClass> getModules() {
		return Collections.unmodifiableList(modules);
	}
	
	/**
	 * This method should process the event, then if necessary, loop through each 
	 * registered module and send appropriate values from the outputs of connected 
	 * modules. Propagation will take place immediately after calling this method.
	 * @param event
	 */
	protected abstract void sendOutputFromModules(FeedEvent<MessageClass, ? extends IEventRecipient> event);
	
}

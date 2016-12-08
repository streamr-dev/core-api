package com.unifina.signalpath;

import com.unifina.utils.Globals;

import java.util.Map;

/**
 * An abstraction for modules with side effects (eg. making API call, sending email, writing to database).
 * These side effects usually need to be turned off in historical mode, but can be enabled if necessary.
 * The main method to implement is activateWithSideEffects().
 *
 * This module supports showing a notification once on the first activation in historical mode,
 * or on every historical mode activation.
 *
 * Also a backup activation strategy is supported via activateWithoutSideEffects().
 */
public abstract class ModuleWithSideEffects extends AbstractSignalPathModule {

	private boolean oneTimeNotificationShown = false;
	private boolean activateInHistoricalMode = false;

	public static final String OPTION_ACTIVATE_IN_HISTORICAL_MODE = "activateInHistoricalMode";

	@Override
	public void sendOutput() {
		Globals globals = getGlobals();

		if (globals.isRealtime() || activateInHistoricalMode) {
			// Normal operation
			activateWithSideEffects();
		} else if (globals.getUiChannel() != null && parentSignalPath != null) {
			activateWithoutSideEffects();

			// Show notification about historical mode unless it's already been shown
			if (!oneTimeNotificationShown) {
				String notification = getOneTimeHistoricalNotification();
				if (notification != null) {
					getGlobals().getUiChannel().push(new NotificationMessage(notification), parentSignalPath.getUiChannelId());
				}
				oneTimeNotificationShown = true;
			}

			// Can also show a notification on every activation
			String notification = getHistoricalNotification();
			if (notification != null) {
				getGlobals().getUiChannel().push(new NotificationMessage(notification), parentSignalPath.getUiChannelId());
			}
		}
	}

	/**
	 * Controls whether to allow the user to enable activation with side effects in historical mode. Default true.
     */
	protected boolean allowSideEffectsInHistoricalMode() {
		return true;
	}

	/**
	 * This method implements the normal operation of the module.
	 */
	protected abstract void activateWithSideEffects();

	/**
	 * This method can implement a backup action without side effects. The default implementation does nothing.
	 */
	protected void activateWithoutSideEffects() {}

	/**
	 * Returns a notification shown on the first historical activation, or null if none should be shown.
	 * Defaults to null.
     */
	protected String getOneTimeHistoricalNotification() {
		return null;
	}

	/**
	 * Returns a notification shown on historical activation, or null if none should be shown.
	 * Defaults to null.
	 */
	protected String getHistoricalNotification() {
		return null;
	}

	@Override
	public Map<String, Object> getConfiguration() {
		Map<String, Object> config = super.getConfiguration();

		if (allowSideEffectsInHistoricalMode()) {
			ModuleOptions options = ModuleOptions.get(config);
			options.add(new ModuleOption(OPTION_ACTIVATE_IN_HISTORICAL_MODE, activateInHistoricalMode, ModuleOption.OPTION_BOOLEAN));
		}

		return config;
	}

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);

		if (allowSideEffectsInHistoricalMode()) {
			ModuleOptions options = ModuleOptions.get(config);
			if (options.getOption(OPTION_ACTIVATE_IN_HISTORICAL_MODE) != null) {
				activateInHistoricalMode = options.getOption(OPTION_ACTIVATE_IN_HISTORICAL_MODE).getBoolean();
			}
		}
	}

}

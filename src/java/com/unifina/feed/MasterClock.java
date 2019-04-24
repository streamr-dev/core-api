package com.unifina.feed;

import com.streamr.client.protocol.message_layer.ITimestamped;
import com.unifina.datasource.DataSource;
import com.unifina.datasource.ITimeListener;
import com.unifina.utils.Globals;

import java.util.Date;

public class MasterClock extends AbstractEventRecipient<ITimeListener, ITimestamped> {
	public MasterClock(Globals globals, DataSource dataSource) {
		super(globals);
		// globals.dataSource is not yet set
		dataSource.addStartListener(this);
	}

	@Override
	protected void sendOutputFromModules(ITimestamped event) {
		Date date = event.getTimestampAsDate();
		long epochSec = date.getTime() / 1000;

		// Iterate by index to avoid ConcurrentModificationException in case new modules are added.
		for (int i=0; i < getModules().size(); i++) {
			ITimeListener module = getModules().get(i);
			if (isTimeToTick(epochSec, module.tickRateInSec())) {
				module.setTime(date);
			}
		}
	}

	public static boolean isTimeToTick(long epochSec, int tickRateInSec) {
		return tickRateInSec != 0 && epochSec % tickRateInSec == 0;
	}
}

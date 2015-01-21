package com.unifina.domain.dashboard

import com.unifina.domain.signalpath.UiChannel

class DashboardItem {
	
	String title
	
	UiChannel uiChannel
	
	static belongsTo = [dashboard: Dashboard]
}

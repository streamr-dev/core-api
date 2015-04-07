package com.unifina.domain.dashboard

import com.unifina.domain.signalpath.UiChannel

class DashboardItem {
	
	String title
	
	UiChannel uiChannel
	
	int order
	
	static belongsTo = [dashboard: Dashboard]
	
	static constraints = {
		title(nullable:true)
	}
}

package com.unifina.domain.dashboard

import com.unifina.domain.signalpath.UiChannel

class DashboardItem implements Comparable {
	
	String title
	
	UiChannel uiChannel
	
	int ord
	
	static belongsTo = [dashboard: Dashboard]
	
	static constraints = {
		title(nullable:true)
	}
	
	int compareTo(obj) {
		return ord.compareTo(obj.ord)
	}
}

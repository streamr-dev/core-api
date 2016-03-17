package com.unifina.domain.dashboard

import com.unifina.domain.signalpath.Canvas

class DashboardItem implements Comparable {
	
	String title
	Canvas canvas
	Integer module
	String webcomponent
	Integer ord
	String size
	
	static belongsTo = [dashboard: Dashboard]
	
	static constraints = {
		title(nullable:true)
	}
	
	int compareTo(obj) {
		return ord.compareTo(obj.ord)
	}

	Map toMap() {
		return [
				id: id,
				title: title,
				ord: ord,
				size: size,
				canvas:  canvas.id,
				module: module,
				webcomponent: webcomponent
		]
	}
}

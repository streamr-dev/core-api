package com.unifina.domain.dashboard

import com.unifina.domain.signalpath.Canvas
import com.unifina.utils.IdGenerator

class DashboardItem implements Comparable {

	String title
	Canvas canvas
	Integer module
	String webcomponent
	Integer ord
	String size
	
	static belongsTo = [dashboard: Dashboard, canvas: Canvas]

	static constraints = {
		title(nullable:true)
	}

	int compareTo(obj) {
		int cmp = ord.compareTo(obj.ord)
		return cmp != 0 ? cmp :
			   id != null && obj.id != null ? id.compareTo(obj.id) :
		       title.compareTo(obj.title)
	}

	Map toMap() {
		return [
				id: id,
				dashboard: dashboard.id,
				title: title,
				ord: ord,
				size: size,
				canvas:  canvas.id,
				module: module,
				webcomponent: webcomponent
		]
	}

	void updateWebcomponent() {
		def module = canvas.toMap().modules.find { it.hash ==  module }
		webcomponent = module?.uiChannel?.webcomponent
	}
}

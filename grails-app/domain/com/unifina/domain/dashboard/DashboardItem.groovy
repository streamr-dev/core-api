package com.unifina.domain.dashboard

import com.unifina.domain.signalpath.Canvas
import com.unifina.utils.IdGenerator

class DashboardItem implements Comparable {

	String id
	String title
	Canvas canvas
	Dashboard dashboard
	Integer module
	String webcomponent

	static belongsTo = [dashboard: Dashboard, canvas: Canvas]

	static constraints = {
		title nullable: true, blank: false
	}

	int compareTo(Object obj) {
		DashboardItem item = (DashboardItem) obj
		return id != null ? id.compareTo(item.id) : title.compareTo(item.title)
	}

	static mapping = {
		id generator: IdGenerator.name
		dashboard column: "dashboard_id"
		canvas column: "canvas_id"
	}

	Map toMap() {
		return [
				id          : id,
				dashboard   : dashboard?.id,
				title       : title,
				canvas      : canvas?.id,
				module      : module,
				webcomponent: webcomponent
		]
	}
}

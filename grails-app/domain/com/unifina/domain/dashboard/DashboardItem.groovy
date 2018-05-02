package com.unifina.domain.dashboard

import com.unifina.domain.signalpath.Canvas
import com.unifina.utils.IdGenerator
import com.unifina.utils.Webcomponent

class DashboardItem {

	String id
	String title
	Canvas canvas
	Dashboard dashboard
	Integer module
	Webcomponent webcomponent

	static belongsTo = [dashboard: Dashboard, canvas: Canvas]

	static constraints = {
		title nullable: true, blank: false
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
				webcomponent: webcomponent.getName()
		]
	}
}

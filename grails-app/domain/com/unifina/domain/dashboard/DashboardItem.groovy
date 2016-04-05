package com.unifina.domain.dashboard

import com.unifina.domain.signalpath.UiChannel

class DashboardItem implements Comparable {

	String title

	UiChannel uiChannel

	int ord

	String size

	static belongsTo = [dashboard: Dashboard]

	static constraints = {
		title(nullable:true)
	}

	int compareTo(obj) {
		int cmp = ord.compareTo(obj.ord)
		return cmp != 0 ? cmp :
			id != null && obj.id != null ? id.compareTo(obj.id) :
				title.compareTo(obj.title)
	}

	def toMap() {
		[
			id: id,
			ord: ord,
			title: title,
			size: size,
			uiChannelId: uiChannelId
		]
	}
}

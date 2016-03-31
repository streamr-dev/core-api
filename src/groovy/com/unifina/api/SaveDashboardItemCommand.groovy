package com.unifina.api

import com.unifina.domain.dashboard.DashboardItem
import com.unifina.domain.signalpath.UiChannel
import grails.validation.Validateable

@Validateable
class SaveDashboardItemCommand {
	String title
	String uiChannelId
	int ord
	String size

	static constraints = {
		title(blank: false)
		uiChannelId(blank: false)
		ord(min: 0)
		size(inList: ["small", "medium", "large"])
	}

	DashboardItem toDashboardItem() {
		new DashboardItem(
			title: title,
			uiChannel: UiChannel.get(uiChannelId), // TODO: remove
			ord: ord,
			size: size
		)
	}

	void copyValuesTo(DashboardItem dashboardItem) {
		dashboardItem.title = title
		dashboardItem.uiChannel = UiChannel.get(uiChannelId) // TODO: remove
		dashboardItem.ord = ord
		dashboardItem.size = size
	}
}

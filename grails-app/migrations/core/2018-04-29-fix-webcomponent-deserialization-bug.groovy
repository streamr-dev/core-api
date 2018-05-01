package core
databaseChangeLog = {
	changeSet(author: "eric", id: "fix-webcomponent-deserialization-bug") {
		sql('UPDATE dashboard_item SET webcomponent = "STREAMR_CLIENT" WHERE webcomponent = "streamr-client"')
		sql('UPDATE dashboard_item SET webcomponent = "STREAMR_WIDGET" WHERE webcomponent = "streamr-widget"')
		sql('UPDATE dashboard_item SET webcomponent = "STREAMR_INPUT" WHERE webcomponent = "streamr-input"')
		sql('UPDATE dashboard_item SET webcomponent = "STREAMR_LABEL" WHERE webcomponent = "streamr-label"')
		sql('UPDATE dashboard_item SET webcomponent = "STREAMR_CHART" WHERE webcomponent = "streamr-chart"')
		sql('UPDATE dashboard_item SET webcomponent = "STREAMR_HEATMAP" WHERE webcomponent = "streamr-heatmap"')
		sql('UPDATE dashboard_item SET webcomponent = "STREAMR_TABLE" WHERE webcomponent = "streamr-table"')
		sql('UPDATE dashboard_item SET webcomponent = "STREAMR_BUTTON" WHERE webcomponent = "streamr-button"')
		sql('UPDATE dashboard_item SET webcomponent = "STREAMR_SWITCHER" WHERE webcomponent = "streamr-switcher"')
		sql('UPDATE dashboard_item SET webcomponent = "STREAMR_TEXT_FIELD" WHERE webcomponent = "streamr-text-field"')
		sql('UPDATE dashboard_item SET webcomponent = "STREAMR_MAP" WHERE webcomponent = "streamr-map"')
	}
}
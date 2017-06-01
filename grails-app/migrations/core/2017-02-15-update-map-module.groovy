package core
databaseChangeLog = {
	changeSet(author: "eric", id: "update-map-module") {
		update(tableName: "module") {
			column(name: "implementing_class", value: 'com.unifina.signalpath.charts.GeographicalMapModule')
			column(name: "name", value: "Map (geo)")
			column(name: "json_help", value: "{\"params\":{},\"paramNames\":[],\"inputs\":{\"id\":\"Id of the marker to place. Will also be displayed on hover over the marker.\",\"latitude\":\"Latitude coordinate for the marker\",\"longitude\":\"Longitude coordinate for the marker\"},\"inputNames\":[\"id\",\"latitude\",\"longitude\"],\"outputs\":{},\"outputNames\":[],\"helpText\":\"<p>This module displays a world map. Markers can be drawn on the map at WGS84 coordinates given to the inputs&nbsp;<strong>latitude</strong>&nbsp;and&nbsp;<strong>longitude</strong>&nbsp;as&nbsp;decimal numbers (degrees). Markers also have an&nbsp;<strong>id</strong>. To draw multiple markers, connect the <b>id</b> input. Coordinates for the same id will move the marker, and coordinates for a new id will create a new marker.</p>\\n\\n<p>In module options, you can enable directional markers to expose an additional&nbsp;<strong>heading</strong>&nbsp;input, which controls marker heading (e.g. vehicles on the street or ships at sea). Other options include marker coloring, autozoom behavior etc.</p>\\n\"}")
			where("id = 214")
		}
	}
}
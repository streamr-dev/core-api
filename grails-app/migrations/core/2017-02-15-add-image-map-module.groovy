package core
databaseChangeLog = {
	changeSet(author: "eric", id: "update-map-module") {
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 583)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 13) // Visualizations
			column(name: "implementing_class", value: "com.unifina.signalpath.charts.ImageMapModule")
			column(name: "name", value: "Map (image)")
			column(name: "js_module", value: "ImageMapModule")
			column(name: "type", value: "module")
			column(name: "module_package_id", valueNumeric: 1)
			column(name: "json_help", value: "{\"params\":{},\"paramNames\":[],\"inputs\":{\"id\":\"Id of the marker to draw\",\"x\":\"Horizontal coordinate of the marker between 0 and 1\",\"y\":\"Vertical coordinate of the marker between 0 and 1\"},\"inputNames\":[\"id\",\"x\",\"y\"],\"outputs\":{},\"outputNames\":[],\"helpText\":\"<p>This module displays a map based on an user-defined image. The image is loaded from a&nbsp;<strong>URL</strong>&nbsp;given in module options.&nbsp;The map automatically scales <strong>x</strong> and <strong>y</strong> coordinates between 0..1&nbsp;to image dimensions.&nbsp;This means&nbsp;that regardless of image size in pixels&nbsp;(x,y) = (0,0) is the top left corner of the image, and (1,1) is the bottom right corner.</p>\\n\\n<p>Markers also have an&nbsp;<strong>id</strong>. To draw multiple markers, connect the <b>id</b> input. Coordinates for the same id will move the marker, and coordinates for a new id will create a new marker.</p>\\n\\n<p>In module options, you can enable directional markers to expose an additional&nbsp;<strong>heading</strong>&nbsp;input, which controls marker heading (e.g. direction in which people are facing in a space). Other options include marker coloring, autozoom behavior etc.</p>\\n\"}")
		}
	}
}
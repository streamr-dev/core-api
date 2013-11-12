SignalPath.defaultChartOptions = {
//		chart: {
//			backgroundColor: "rgba(255, 255, 255, 0.0)",
//		},
//		legend: {
//			itemStyle: {
//				color: "#ffffff",
//			},
//			itemHoverStyle: {
//				color: "#9999cc"
//			}
//		},
//		yAxis: {
//			title: {
//				text: null
//			},
//			tickColor: "#ffffff",
//			labels: {
//				style: {
//					color: "#ffffff"
//				}
//			}
//		},
//		xAxis: {
//			tickColor: "#ffffff",
//			labels: {
//				style: {
//					color: "#ffffff"
//				}
//			}
//		},
//		tooltip: {
//			backgroundColor: "#ffffff",
//			
//		}
}

// Any styles and colors defined here should be consistent with
// colors in the CSS. Some browsers don't support SVG styling via CSS.

jsPlumb.Defaults.EndpointStyle = { radius: 10, /*strokeStyle:'#fff', fillStyle: '#ccc' */};
//jsPlumb.Defaults.EndpointHoverStyle = { radius: 10, strokeStyle:'#fff', fillStyle:'#fff'};
jsPlumb.Defaults.Connector = [ "Bezier", { curviness: 50 } ];
jsPlumb.Defaults.PaintStyle = {
		lineWidth:3,
		strokeStyle: "#aaa" //('rgba(200,0,0,100)'
};
jsPlumb.Defaults.Overlays = [["Arrow", {direction:-1, paintStyle: {cssClass:"arrow"}}]];

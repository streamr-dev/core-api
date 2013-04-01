	
onSignalPathLoad = function(saveData,data,signalPathContext) {
	
	if (signalPathContext.beginDate)
		$("#beginDate").val(signalPathContext.beginDate);
	if (signalPathContext.endDate)
		$("#endDate").val(signalPathContext.endDate);

	if (signalPathContext.timeOfDayFilter) {
		$("#timeOfDayStart").val(signalPathContext.timeOfDayFilter.timeOfDayStart);
		$("#timeOfDayEnd").val(signalPathContext.timeOfDayFilter.timeOfDayEnd);
	}	
	
	$("#speed").val(signalPathContext.speed!=null ? signalPathContext.speed : 0);
	$("#marketSimulator").val(signalPathContext.marketSimulator!=null ? signalPathContext.marketSimulator : 1);
	$("#latency").val(signalPathContext.latency ? signalPathContext.latency : 0);
	$("#riskProfile").val(signalPathContext.riskProfile!=null ? signalPathContext.riskProfile : "null");

	$('#saveSignalPath').removeAttr("disabled");
	$('#saveSignalPath').html("Save to "+saveData.target);
	
	if (signalPathContext.resetPortfolio)
		$("#resetPortfolio").attr("checked","checked");
	
	var lb = $("#loadBrowser");
	if (lb.is(":visible")) {
		lb.hide();
		$("#loadTabs").tabs("destroy");
	}
}

$(document).ready(function() {
	
	getSignalPathContext = function() {
		var tz = jstz.determine_timezone().timezone;
		
		return {
			beginDate: $("#beginDate").val(),
			endDate: $("#endDate").val(),
			speed: $("#speed").val(),
			latency: $("#latency").val(),
			timeOfDayFilter: {
				timeOfDayStart: $("#timeOfDayStart").val(),
				timeOfDayEnd: $("#timeOfDayEnd").val(),	
				timeZone: tz.olson_tz,
				timeZoneOffset: tz.utc_offset,
				timeZoneDst: tz.uses_dst
			},
			marketSimulator: $("#marketSimulator").val(),
			live: $("#live").hasClass("active"),
			riskProfile: $("#riskProfile").val(),
			resetPortfolio: $("#resetPortfolio").attr("checked") ? true : false
		}
	}
	
	$('#modSearch').autocomplete({
		minLength: 2,
		delay: 0,
		select: function(event,ui) {
			if (ui.item) {
				SignalPath.addModule(ui.item.id,{});
				$('#modSearch').val("");
			}
			else {
				// Nothing selected
			}
		}
	}).data("autocomplete")._renderItem = function(ul,item) {
		return $("<li></li>")
		.data("item.autocomplete",item)
		.append("<a>"+item.name+"</a>")
		.appendTo(ul);
	};

	refreshModules = function() {
		
		/**
		 * Module search autocomplete
		 */
		$.getJSON(project_webroot+"module/jsonGetModules", function(data) {
			$('#modSearch').autocomplete("option","source",(function(d) {
				return function(req,cb) {
					var result = []
					var term = req.term.toLowerCase();
					for (var i=0;i<data.length;i++) {
						if (data[i].name.toLowerCase().indexOf(req.term)!=-1)
							result.push(data[i]);
					}
					cb(result);
				};
			})(data));
		});
		
		/**
		 * Module tree browser
		 */
		$("#moduleTree").jstree({ 
			"core": {
				"animation": 100
			},
			
			"themes": {
				"icons": false
			},
			
			"ui": {
				"select_limit": 1
			},
			
			"json_data" : {
				"ajax" : {
					"url" : project_webroot+"module/jsonGetModuleTree",
				}
			},
			
			"types": {
				"default" : {
				    draggable : false
				}
			},
			
			"plugins" : [ "json_data", "ui" ]
		})
		.bind("select_node.jstree", function (event, data) {
			// `data.rslt.obj` is the jquery extended node that was clicked
				if (data.rslt.obj.data("canAdd")) {
					$('#addModule').removeAttr("disabled");
				}
				else {
					$('#addModule').attr("disabled","disabled");
					data.inst.open_node(data.rslt.obj);
				}
	    })
	    .bind("loaded.jstree", function() {
			$("#moduleTree ul").addClass("jstree-no-icons");
		});
		$("#moduleTree").addClass("jstree-default jstree-no-icons");
	}
    
	refreshModules();
	
    $("#beginDate").datepicker({
    	dateFormat: "yy-mm-dd",
    	firstDay: 1
    });
    $("#endDate").datepicker({
    	dateFormat: "yy-mm-dd",
    	firstDay: 1
    });

    $("#showMoreContextOptions a").click(function() {
    	$("#showMoreContextOptions").hide();
    	$("#moreContextOptions").show();
    });
    
    $("#showLessContextOptions").click(function() {
    	$("#showMoreContextOptions").show();
    	$("#moreContextOptions").hide();
    });
    
	$('#toggleImportExport').click(function() {
		if (!$(this).hasClass("active")) {
			$(this).addClass("active");
			
//			$(".input,.output")
//			.addClass("exportable")
//			.bind("click.export", function() {
//				if ($(this).hasClass("export"))
//					$(this).removeClass("export");
//				else $(this).addClass("export");
//			})
//			.hover(function() {$(this).css("cursor","hand");}, function() {$(this).css("cursor","default");});
			$(".ioSwitch.export").show();
		}
		else {
			$(this).removeClass("active");
			
//			$(".input,.output")
//			.removeClass("exportable")
//			.unbind("click.export")
//			.unbind("mouseenter mouseleave");
			
			$(".ioSwitch.export").hide();
		}
	});
	
	$('#obSearch').autocomplete({
		source: "jsonGetOrderBook",
		minLength: 2,
		select: function(event,ui) {
			if (ui.item) {
//				SignalPath.addModule(68,{orderBookId:ui.item.id});
				SignalPath.addModule(68,{params:[{name:"symbol", value:ui.item.id}]});
				$('#obSearch').val("");
			}
			else {
				// Nothing selected
			}
		}
	}).data("autocomplete")._renderItem = function(ul,item) {
		return $("<li></li>")
			.data("item.autocomplete",item)
			.append("<a>"+item.name+"</a>")
			.appendTo(ul);
	};
	
	$('#addModule').click(function() {
		var id = $('#moduleTree').jstree("get_selected").data("id");
		SignalPath.addModule(id, {});
	}).attr("disabled","disabled");
	
	$('#refreshModules').click(function() {
		refreshModules();
	});
	
	$('#addChart').click(function() {
		SignalPath.addModule(67,{});
	});
	
	$('#run').click(function() {
		SignalPath.run(getSignalPathContext());
	});
	
	$(document).bind('keyup', 'alt+r', function() {
		SignalPath.run(getSignalPathContext());
	});
	
	$('#abort').click(function() {
		SignalPath.abort();
	});
	
	$('#csv').click(function() {
		var ctx = getSignalPathContext();
		
		ctx.csv = true;
		ctx.csvOptions = {
			csvTimeFormat: $("#csvTimeFormat").val(),
			filterEmpty: $("#csvFilterEmpty").attr("checked") ? true : false,
			lastOfDayOnly: $("#csvLastOfDayOnly").attr("checked") ? true : false,
		}
		
		SignalPath.run(ctx);
	});
	

	
	$("#addToAccount").click(function() {
		var ac = $("#accountBrowser");
		if (ac.is(":visible")) {
			ac.hide();
			$("#accountTabs").tabs("destroy");
		}
		else {
			$("#accountTabs").tabs();
			ac.show();
			ac.position({my: "right top", at: "right bottom", of: "#addToAccount", collision:"none"});
		}
	});
	
	$("#loadSignalPath").click(function() {
		var lb = $("#loadBrowser");
		if (lb.is(":visible")) {
			lb.hide();
			$("#loadTabs").tabs("destroy");
		}
		else {
			$("#loadTabs").tabs();
			lb.show();
			lb.position({my: "left top", at: "left bottom", of: "#loadSignalPath", collision:"none"});
		}
	});
	
});


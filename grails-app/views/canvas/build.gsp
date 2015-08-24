<html>
	<head>
		<meta name="layout" content="sidemenu" />

		<title>Canvas</title>

		<r:require module='jstree'/>
		<r:require module='pnotify'/>
		<r:require module='jquery-ui'/>
		<r:require module="bootbox"/>
		<r:require module="bootstrap-contextmenu"/>
		<r:require module="bootstrap-datepicker"/>
		<r:require module="slimscroll"/>
		<r:require module="search-control"/>
		<r:require module="signalpath-browser"/>
		<r:require module="signalpath-theme"/>
		<r:require module="hotkeys"/>
		<r:require module="touchpunch"/>
		<r:require module="detect-timezone"/>

		<r:script>

// Make the loadBrowser global to allow apps that use this plugin to extend it by adding tabs
var loadBrowser

$('#moduleTree').bind('loaded.jstree', function() {
	Tour.autoStart()
})

$(document).ready(function() {

	function getSignalPathContext() {
		var tz = jstz();
		
		return {
			beginDate: $("#beginDate").val(),
			endDate: $("#endDate").val(),
			speed: $("#speed").val(),
			timeOfDayFilter: {
				timeOfDayStart: $("#timeOfDayStart").val(),
				timeOfDayEnd: $("#timeOfDayEnd").val(),	
				timeZone: tz.timezone_name,
				timeZoneOffset: tz.utc_offset,
				timeZoneDst: tz.uses_dst
			}
		}
	}

	SignalPath.init({
		canvas: 'canvas',
		signalPathContext: getSignalPathContext,
		errorHandler: function(data) {
			$.pnotify({
				type: 'error',
        		title: 'Error',
	        	text: data.msg,
	        	delay: 4000
    		});
		},
		notificationHandler: function(data) {
			$.pnotify({
				type: 'info',
	        	text: data.msg,
	        	delay: 4000
    		});
		},
		runUrl: Streamr.createLink('live', 'ajaxCreate'),
		abortUrl: Streamr.createLink('live', 'ajaxStop'),
		connectionOptions: {
			server: "${grailsApplication.config.streamr.ui.server}"
		}
	});
	
	$(SignalPath).on('loading', function() {
		$('#modal-spinner').show()
	})

	$(SignalPath).on('loaded', function(event,saveData,data,signalPathContext) {	
		$('#modal-spinner').hide()
		
		if (signalPathContext.beginDate) {
			$("#beginDate").val(signalPathContext.beginDate).trigger("change")
		}
		if (signalPathContext.endDate)
			$("#endDate").val(signalPathContext.endDate).trigger("change")

		if (signalPathContext.timeOfDayFilter) {
			$("#timeOfDayStart").val(signalPathContext.timeOfDayFilter.timeOfDayStart).trigger("change")
			$("#timeOfDayEnd").val(signalPathContext.timeOfDayFilter.timeOfDayEnd).trigger("change")
		}

		$("#speed").val(signalPathContext.speed!=null ? signalPathContext.speed : 0).trigger("change")		
	});
	
	$(SignalPath).on('workspaceChanged', function(event, mode) {
		if (mode=="dashboard") {
			$("#controls").hide();
			$("#topButtons").addClass("showOnHover");
		}
		else {
			$("#controls").show();
			$("#topButtons").removeClass("showOnHover");
		}
	});
	
	<g:if test="${load}">
		SignalPath.loadSignalPath({url:"${load}"});
	</g:if>
	
	$.pnotify.defaults.history = false;
	$.pnotify.defaults.styling = "bootstrap";

	$(SignalPath).on('error', function(error) {
		console.error(error)
		$('#modal-spinner').hide()
	})

	$(SignalPath).on('saving', function() {
		$('#modal-spinner').show()
	})

	$(SignalPath).on('saved', function(event,data) {
		$('#modal-spinner').hide()
		$.pnotify({
			type: 'success',
        	title: '${message(code:"signalpath.saved.title")}',
	        text: '${message(code:"signalpath.saved.to")} '+data.target+'.',
	        delay: 4000
    	});
	});

	// show search control
	new SearchControl(
		'${ createLink(controller: "stream", action: "search") }',
		'${ createLink(controller: "module", action: "jsonGetModules") }',
		$('#search'))
	
	// Bind slimScroll to main menu
    $('#main-menu-inner').slimScroll({
      height: '100%'
    })

	loadBrowser = new SignalPathBrowser()
		.tab('Archive', '${ createLink(controller: "savedSignalPath", \
			action: "loadBrowser", params: [ browserId: "archiveLoadBrowser" ]) }')

		<%-- Don't show the live tab without ROLE_LIVE --%>
		<sec:ifAllGranted roles="ROLE_LIVE">
			.tab('Live', '${ createLink(controller: "live", \
				action: "loadBrowser", params: [ browserId: "liveLoadBrowser" ]) }')
		</sec:ifAllGranted>
		
		.tab('Examples', '${ createLink(controller: "savedSignalPath", \
			action: "loadBrowser", params: [ browserId: "examplesLoadBrowser" ]) }')
			
		.onSelect(function(url) {
			SignalPath.loadSignalPath({ url: url })
		})

	<%-- Show examples loader if requested --%>
	<g:if test="${examples}">
		// Use a timeout to allow extensions to register their tabs before the loadBrowser is shown
		setTimeout(function() {
			loadBrowser.modal()
			loadBrowser.show("Examples")
		}, 0)
	</g:if>

	$('#loadSignalPath').click(function() {
		loadBrowser.modal()
	})
	
	$(document).bind('keyup', 'alt+r', function() {
		SignalPath.run();
	});
	
	$('#csv').click(function() {
		var ctx = {
			csv: true,
			csvOptions: {
				timeFormat: $("#csvTimeFormat").val(),
				separator: $("#csvSeparator").val(),
				filterEmpty: $("#csvFilterEmpty").attr("checked") ? true : false,
				lastOfDayOnly: $("#csvLastOfDayOnly").attr("checked") ? true : false
			}
		}
		
		SignalPath.run(ctx);
	});
	
	$('#runLiveModalButton').click(function() {
		if (SignalPath.getName())
			$('#runLiveName').val(SignalPath.getName())
	})
	
	$('#runLiveButton').click(function() {
		var name = $('#runLiveName').val()
		SignalPath.setName(name)
		
		var ctx = {
			live: true
		}
		
		SignalPath.run(ctx, false, function(data) {
			var url_root = '${createLink(controller:"live", action:"show")}'
			
			$.pnotify({
				type: 'info',
	        	text: "Live Canvas launced:"+name,
	        	delay: 4000
    		});
    		
    		window.location = url_root + "/" + data.id
		});
	})
})

$(document).unload(function () {
	SignalPath.unload()
})

</r:script>

<!-- mustache templates -->
<g:render template="/templates/remote-tabs" plugin="unifina-core"/>

<!-- extension point for apps using the core plugin -->
<g:render template="/canvas/signalPathExtensions"/>
<g:render template="/canvas/buildHeadExtensions"/>

</head>

<body class="build-page main-menu-fixed">
	<div id="main-menu" role="navigation">
		<div id="main-menu-inner">
			<div id="toolbar-buttons" class="menu-content" style="overflow: visible;">
				<div class="btn-group load-save-group">
					<button id="loadSignalPath" class="btn btn-default">
						<i class="fa fa-folder-open"></i>
						<g:message code="signalPath.loadSignalPath.label" default="Load" />
					</button>
		
					<sp:saveButtonDropdown/>
				</div>
			</div>

			<div class="menu-content">
			
				<div class="menu-content-header">
					<label>Run Options</label>
					<a href="#" class="btn btn-primary btn-outline dark btn-xs pull-right" title="Show More Options" data-toggle="modal" data-target="#runOptionsModal">
						<i class="fa fa-cog"></i>
					</a>
				</div>
			
				<form onsubmit="return false;">
					<g:hiddenField name="defaultBeginDate" value="${formatDate(date:beginDate,format:"yyyy-MM-dd")}"/>
					<g:hiddenField name="defaultEndDate" value="${formatDate(date:endDate,format:"yyyy-MM-dd")}"/>

					<div class="form-group form-group-period">
						<div class="input-group">
							<span class="input-group-addon">From</span>
							<ui:datePicker name="beginDate" value="${beginDate}" class="form-control"/>
						</div>
						<div class="input-group">
							<span class="input-group-addon">To</span>
							<ui:datePicker name="endDate" value="${endDate}" class="form-control"/>
						</div>
					</div>

					<div id="run-group" class="btn-group btn-block">
						<sp:runButton buttonId="run" class="btn-primary col-xs-10">
							<i class="fa fa-play"></i>
							Run
						</sp:runButton>
						<button id="runDropdown" type="button" class="btn btn-primary col-xs-2 dropdown-toggle"
							data-toggle="dropdown">
							<span class="caret"></span> 
							<span class="sr-only">Toggle Dropdown</span>
						</button>
						<ul class="dropdown-menu" role="menu">
							<sec:ifAllGranted roles="ROLE_LIVE">
								<li><a id="runLiveModalButton" href="#" data-toggle="modal" data-target="#runLiveModal">Launch Live..</a></li>
							</sec:ifAllGranted>
							<li><a id="csvModalButton" href="#" data-toggle="modal" data-target="#csvModal">Run as CSV export..</a></li>
						</ul>
					</div>
				</form>
			</div>
			
			<div id="search-control" class="menu-content" style="overflow: visible">
				<label for="search">Add Stream / Module</label><br>
				<input type="text" class="typeahead form-control" id="search" placeholder="Type to search"/>
			</div>

			<div class="menu-content">
				<div class="menu-content-header">
					<label for="moduleTree">Module Browser</label>
				</div>
				
				<sp:moduleBrowser id="moduleTree" buttonId="addModule" />
				
				<sp:moduleAddButton buttonId="addModule" browserId="moduleTree" class="btn-block">
					<i class="fa fa-plus"></i>
					<g:message code="signalPath.addModule.label" default="Add Module" />
				</sp:moduleAddButton>
				

			</div>
		</div> <!-- / #main-menu-inner -->
	</div> <!-- / #main-menu -->

	<div id="content-wrapper">
		<div id="canvas" class="scrollable"></div>
	</div>

	<div id="main-menu-bg"></div>
	
	<div id="runLiveModal" class="modal fade">
	  <div class="modal-dialog">
	    <div class="modal-content">
	      <div class="modal-header">
	        <button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>
	        <h4 class="modal-title">Launch Live Options</h4>
	      </div>
	      <div class="modal-body">
				<div class="form-group">
					<label>Name</label>
					
					<input type="text" name="name" id="runLiveName" class="form-control">
				</div>
	      </div>
	      <div class="modal-footer">
	        <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
	        <button id="runLiveButton" class="btn btn-primary" data-dismiss="modal">Launch</button>
	      </div>
	    </div><!-- /.modal-content -->
	  </div><!-- /.modal-dialog -->
	</div><!-- /.modal -->

	<div id="csvModal" class="modal fade">
	  <div class="modal-dialog">
	    <div class="modal-content">
	      <div class="modal-header">
	        <button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>
	        <h4 class="modal-title">CSV Export Options</h4>
	      </div>
	      <div class="modal-body">
				<div class="form-group">
					<label>Time Format</label>
					
					<select id="csvTimeFormat" class="form-control">
						<option value="1">Java timestamp (milliseconds since January 1st 1970 UTC)</option>
						<option value="2" selected>ISO 8601 in your timezone</option>
						<option value="3">ISO 8601 in UTC</option>
					</select>
					
					<label>Separator</label>
					
					<select id="csvSeparator" class="form-control">
						<option value="," selected>Comma (,)</option>
						<option value=";">Semicolon (;)</option>
						<option value="tab">Tab</option>
					</select>
					
					<label>Filters</label>
					
					<div class="checkbox">
						<label>
							<input type="checkbox" id="csvFilterEmpty" value="true" checked/>
							Require data in all columns
						</label>
					</div>
					
					<div class="checkbox">
						<label>
							<input type="checkbox" id="csvLastOfDayOnly" value="false"/>
							Last row of day only
						</label>
					</div>
				</div>
	      </div>
	      <div class="modal-footer">
	        <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
	        <button id="csv" class="btn btn-primary" data-dismiss="modal">Run</button>
	      </div>
	    </div><!-- /.modal-content -->
	  </div><!-- /.modal-dialog -->
	</div><!-- /.modal -->
	
	<div id="runOptionsModal" class="modal fade">
	  <div class="modal-dialog">
	    <div class="modal-content">
	      <div class="modal-header">
	        <button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>
	        <h4 class="modal-title">Advanced Run Options</h4>
	      </div>
	      <div class="modal-body">
				<div class="form-group">
					<label>Speed</label>
					<select id="speed" class="form-control">
						<option value="0">Full</option>
						<option value="1">1x</option>
						<option value="10">10x</option>
						<option value="100">100x</option>
						<option value="1000">1000x</option>
					</select>
				</div>
				
				<div class="form-group">
					<label>Speed time of day</label>
					<input id="timeOfDayStart" type="text" name="timeOfDayStart" value="00:00:00" class="form-control">
					<input id="timeOfDayEnd" type="text" name="timeOfDayEnd" value="23:59:00" class="form-control">
				</div>

	      </div>
	      <div class="modal-footer">
	        <button type="button" class="btn btn-primary" data-dismiss="modal">OK</button>
	      </div>
	    </div><!-- /.modal-content -->
	  </div><!-- /.modal-dialog -->
	</div><!-- /.modal -->

	<ul id="save-dropdown-menu" class="dropdown-menu" role="menu">
		<li class="disabled"><a href="#" id="saveButton">Save</a></li>
		<li><a href="#" id="saveAsButton">Save as..</a></li>
	</ul>
	
	<g:render template="/feedback/fixedFeedback" plugin="unifina-core"/>

	<!-- extension point for apps using the core plugin -->
	<g:render template="/canvas/buildBodyExtensions"/>

</body>
</html>


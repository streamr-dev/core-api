
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<title><g:message code="runningSignalPath.show.label" args="[rsp.name]"/></title>

<r:require module="signalpath-theme"/>
<g:render template="/canvas/signalPathExtensions"/>

<r:require module="toolbar"/>
<r:script>
	 $(document).ready(function() {
	 	new Toolbar($("#toolbar"))
	 })
</r:script>

<r:script>
	$(document).ready(function() {
		SignalPath.init({
			canvas: "canvas",
			zoom: ${params.zoom ?: 1},
			connectionOptions: {
				server: "${grailsApplication.config.streamr.ui.server}"
			},
			resendOptions: { resend_all:false, resend_last: 0 },
			errorHandler: function(msg) {
				console.error(msg)
			}
		});
		
		SignalPath.loadSignalPath({
			url: '${createLink(action:"getJson", id:rsp.id)}'
		}, function(saveData, signalPathData, signalPathContext, runData) {
			SignalPath.subscribe(runData, true);
			
			<g:if test="${params.embedded}">
			// Terrible workaround for jsPlumb rendering problems within iframe on IE
			var count = 0
			var interval = setInterval(function() {
				SignalPath.jsPlumb.repaintEverything()
				if (count++ === 10)
					clearInterval(interval)
			}, 2000)
			</g:if>
		});
	});
	$(document).unload(function () {
		SignalPath.unload();
	});
</r:script>

</head>

<body class="live-show-view full-width-height">
	<!-- <div class="table"> -->
		<ui:breadcrumb>
			<g:render template="/live/breadcrumbList" model="[runningSignalPath:rsp]"/>
			<g:render template="/live/breadcrumbShow" model="[runningSignalPath:rsp, active:true]"/>
		</ui:breadcrumb>
		<form method="post" role="form" id="toolbarForm">
			<g:hiddenField name="id" value="${rsp.id}" />

			<div id="toolbar" class="toolbar text-left">
				<!-- <div class="btn-group"> -->
					<g:if test="${rsp.state=="running"}">
						<button id="stopButton" class="btn btn-default confirm" data-action="${createLink(action:'stop')}" data-confirm="<g:message code="runningSignalPath.stop.confirm" args="[rsp.name]"></g:message>">
							<i class="fa fa-pause"></i>
							${message(code: 'runningSignalPath.stop.label')}
						</button>        	
					</g:if>
					<g:elseif test="${rsp.state=="stopped"}">
						<button id="startButton" class="btn btn-default" data-action="${createLink(action:'start')}">
							<i class="fa fa-play"></i>
							${message(code: 'runningSignalPath.start.label')}
						</button>
						<button id="deleteButton" class="btn btn-default confirm" data-action="${createLink(action:'delete')}" data-confirm="<g:message code="runningSignalPath.delete.confirm" args="[rsp.name]"></g:message>">
							<i class="fa fa-trash-o"></i>
							${message(code: 'runningSignalPath.delete.label')}
						</button>
					</g:elseif>
				<!-- </div> -->
			</div>
		</form>
		<!-- <div class="full"> -->
			<div id="canvas" class="embeddable"></div>
		<!-- </div> -->
	<!-- </div> -->
	<script id="scheduler-template" type="text/template">
			<ol class="table scheduler-table">
				
			</ol>
			<div class="col-xs-12 form-inline setup">
				<div class="add-rule">
					<i class="btn add-rule-btn btn-primary fa fa-plus">&nbsp;Add</i>
				</div>
				<div class="default-value">
					Default value: <input type="number" step="any" name="default-value" class="form-control input-default input-sm" value="0"/>
				</div>
			</div>
		</script>
	<script id="rule-view-template" type="text/template">
		<li class="rule">
			<div class="td">Every</div>
			<div class="td interval-type">
				<select name="interval-type" class="form-control input-sm">
					<option value="0">hour</option>
					<option value="1">day</option>
					<option value="2">week</option>
					<option value="3">month</option>
					<option value="4">year</option>
				</select>
			</div>
			<div class="td date"></div>
			<div class="td value">
				Value: <input name="value" type="number" step="any" class="form-control input-sm" value="0.0"/>
			</div>
			<div class="td move">
				<div class="move-up-btn" title="Move up">
					<i class=" fa fa-caret-up"></i>
				</div>
				<div class="move-down-btn" title="Move down">
					<i class=" fa fa-caret-down"></i>
				</div>
			</div>
			<div class="td delete">
				<i class="delete-btn fa fa-trash-o" title="Remove"></i>
			</div>
		</li>
	</script>
	<script id="rule-view-year-template" type="text/template">
				the
				<select name="day" class="form-control input-sm">
					{[ _.each(_.range(31), function(i){ ]}
						<option value="{{i+1}}">
							{[ if(i+1 == 1 || (i+1) % 10 == 1){ ]} 
								{{ i+1 + "st" }}
							{[ } else if(i+1 == 2 || (i+1) % 10 == 2) { ]}
								{{ i+1 + "nd" }}
							{[ } else if(i+1 == 3 || (i+1) % 10 == 3) { ]}
								{{ i+1 + "rd" }}
							{[ } else { ]}
								{{ i+1 + "th" }}
							{[ } ]}
						</option>
					{[ }); ]}
				</select>
				of
				<select name="month" class="form-control input-sm">
					<option value="1">January</option>
					<option value="2">February</option>
					<option value="3">March</option>
					<option value="4">April</option>
					<option value="5">May</option>
					<option value="6">June</option>
					<option value="7">July</option>
					<option value="8">August</option>
					<option value="9">September</option>
					<option value="10">October</option>
					<option value="11">November</option>
					<option value="12">December</option>
				</select>
				at
				<select name="hour" class="form-control input-sm">
					{[ _.each(_.range(24), function(i){ ]}
						<option value="{{i}}">
							{[ if(i<10){ ]} 
								0{{i}}
							{[ } else { ]}
								{{i}}
							{[ } ]}
						</option>
					{[ }); ]}
				</select>
				:
				<select name="minute" class="form-control input-sm">
					{[ _.each(_.range(60), function(i){ ]}
						<option value="{{i}}">
							{[ if(i<10){ ]} 
								0{{i}}
							{[ } else { ]}
								{{i}}
							{[ } ]}
						</option>
					{[ }); ]}
				</select>
		</script>
	<script id="rule-view-month-template" type="text/template"> 
				the
				<select name="day" class="form-control input-sm">
					{[ _.each(_.range(31), function(i){ ]}
						<option value="{{i+1}}">
							{[ if(i+1 == 1 || (i+1) % 10 == 1){ ]} 
								{{ i+1 + "st" }}
							{[ } else if(i+1 == 2 || (i+1) % 10 == 2) { ]}
								{{ i+1 + "nd" }}
							{[ } else if(i+1 == 3 || (i+1) % 10 == 3) { ]}
								{{ i+1 + "rd" }}
							{[ } else { ]}
								{{ i+1 + "th" }}
							{[ } ]}
						</option>
					{[ }); ]}
				</select>
				at
				<select name="hour" class="form-control input-sm">
					{[ _.each(_.range(24), function(i){ ]}
						<option value="{{i}}">
							{[ if(i<10){ ]} 
								0{{i}}
							{[ } else { ]}
								{{i}}
							{[ } ]}
						</option>
					{[ }); ]}
				</select>
				:
				<select name="minute" class="form-control input-sm">
					{[ _.each(_.range(60), function(i){ ]}
						<option value="{{i}}">
							{[ if(i<10){ ]} 
								0{{i}}
							{[ } else { ]}
								{{i}}
							{[ } ]}
						</option>
					{[ }); ]}
				</select>
		</script>
	<script id="rule-view-week-template" type="text/template"> 
				<select name="weekday" class="form-control input-sm">
					<option value="0">Monday</option>
					<option value="1">Tuesday</option>
					<option value="2">Wednesday</option>
					<option value="3">Thursday</option>
					<option value="4">Friday</option>
					<option value="5">Saturday</option>
					<option value="6">Sunday</option>
				</select>
				at
				<select name="hour" class="form-control input-sm">
					{[ _.each(_.range(24), function(i){ ]}
						<option value="{{i}}">
							{[ if(i<10){ ]} 
								0{{i}}
							{[ } else { ]}
								{{i}}
							{[ } ]}
						</option>
					{[ }); ]}
				</select>
				:
				<select name="minute" class="form-control input-sm">
					{[ _.each(_.range(60), function(i){ ]}
						<option value="{{i}}">
							{[ if(i<10){ ]} 
								0{{i}}
							{[ } else { ]}
								{{i}}
							{[ } ]}
						</option>
					{[ }); ]}
				</select>
		</script>
	<script id="rule-view-day-template" type="text/template"> 
				<select name="hour" class="form-control input-sm">
					{[ _.each(_.range(24), function(i){ ]}
						<option value="{{i}}">
							{[ if(i<10){ ]} 
								0{{i}}
							{[ } else { ]}
								{{i}}
							{[ } ]}
						</option>
					{[ }); ]}
				</select>
				:
				<select name="minute" class="form-control input-sm">
					{[ _.each(_.range(60), function(i){ ]}
						<option value="{{i}}">
							{[ if(i<10){ ]} 
								0{{i}}
							{[ } else { ]}
								{{i}}
							{[ } ]}
						</option>
					{[ }); ]}
				</select>
		</script>
	<script id="rule-view-hour-template" type="text/template"> 
				<select name="minute" class="form-control input-sm">
					{[ _.each(_.range(60), function(i){ ]}
						<option value="{{i}}">
							{[ if(i<10){ ]} 
								0{{i}}
							{[ } else { ]}
								{{i}}
							{[ } ]}
						</option>
					{[ }); ]}
				</select>
				minutes from the beginning of the hour
		</script>
</body>
</html>

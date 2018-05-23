<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <title>Edit Module Help</title>
        <r:script>

        function addRow(table, io, map) {
			var newRow = $("<tr><td class='name'></td><td class='value'><input type='text'></td></tr>");
			table.append(newRow);
			newRow.find("td.name").text(io.name);

			var helpText = ""
			if (map!=null && map[io.name]!=null)
				helpText = map[io.name];

			newRow.find("td.value input").val(helpText);
        }

        function loadHelp(moduleJson) {

			var helpJson = null;
			$("#message").html("Loading module help texts..");
        	$.ajax({
			    type: 'GET',
			    url: Streamr.createLink({uri: 'api/v1/modules/'+moduleJson.id+'/help'}),
			    dataType: 'json',
			    success: function(data) {
			    	helpJson = data;
					$("#message").hide();
				},
			    error: function() {$("#message").html("An error occured, module help texts could not be loaded.");},
			    data: {id:${module.id}},
			    async: false
			});

			if (helpJson!=null && helpJson.helpText!=null)
				$("#moduleHelp").val(helpJson.helpText);

			var paramTable = $("#params"),
				inputTable = $("#inputs"),
				outputTable = $("#outputs");

			$(moduleJson.params).each(function(i,io) {
				addRow(paramTable, io, helpJson.params);
			});
			$(moduleJson.inputs).each(function(i,io) {
				addRow(inputTable, io, helpJson.inputs);
			});
			$(moduleJson.outputs).each(function(i,io) {
				addRow(outputTable, io, helpJson.outputs);
			});

        }

        function saveHelp(helpJson) {
       		$.ajax({
			    type: 'POST',
			    url: '${createLink(controller:"module",action:"jsonSetModuleHelp")}',
			    dataType: 'json',
			    success: function(data) {
			    	var msg = (data.success ? "Module help successfully saved." : "An error has occurred.");
			    	$("#message").text(msg).show();
			    },
			    error: function(jqXHR, textStatus, errorThrown) {
			    	$("#message").text("An error has occurred.").show();
			    },
			    data: {id:${module.id}, jsonHelp:JSON.stringify(makeHelp())}
			});
        }

        function makeHelp() {
        	var paramTable = $("#params"),
				inputTable = $("#inputs"),
				outputTable = $("#outputs"),
				result = {params:{}, paramNames:[], inputs:{}, inputNames:[], outputs:{}, outputNames:[]};

			result.helpText = $("#moduleHelp").val();
			paramTable.find("tr").each(function(i,row) {
				var name = $(row).find("td.name").text();
				var value = $(row).find("td.value input").val();
				if (value!=null && value!="") {
					result.paramNames.push(name);
					result.params[name] = value;
				}
			});
			inputTable.find("tr").each(function(i,row) {
				var name = $(row).find("td.name").text();
				var value = $(row).find("td.value input").val();
				if (value!=null && value!="") {
					result.inputNames.push(name);
					result.inputs[name] = value;
				}
			});
			outputTable.find("tr").each(function(i,row) {
				var name = $(row).find("td.name").text();
				var value = $(row).find("td.value input").val();
				if (value!=null && value!="") {
					result.outputNames.push(name);
					result.outputs[name] = value;
				}
			});
			return result;
        }

        $(document).ready(function() {

        	$("#message").html("Loading module information..");
        	// Do a couple of sync calls to get the JSON
			var moduleJson = null;
        	$.ajax({
			    type: 'GET',
			    url: '${createLink(controller:"module",action:"jsonGetModule")}',
			    dataType: 'json',
			    success: function(data) {moduleJson = data;},
			    error: function() {$("#message").html("An error occured, module data could not be loaded.");},
			    data: {id:${module.id}},
			    async: false
			});

			if (moduleJson==null) {
				$("#message").html("An error occured, module data could not be loaded.");
				return;
			}
			else loadHelp(moduleJson);

			$("#save").click(saveHelp);
        });
        </r:script>
    </head>
    <body class="help-page">
        <div class="body">
            <h1>Module help editor: ${module.name} (Package: ${module.modulePackage.name})</h1>

            <div id="message" class="message"></div>

            <h2>Module Help Text</h2>
            <textarea id="moduleHelp"></textarea>

            <h2>Parameters</h2>
            <table id="params"></table>

            <h2>Inputs</h2>
            <table id="inputs"></table>

            <h2>Outputs</h2>
            <table id="outputs"></table>

            <button id="save">Save</button>
        </div>
    </body>
</html>

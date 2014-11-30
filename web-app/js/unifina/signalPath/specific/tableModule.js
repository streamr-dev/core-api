SignalPath.TableModule = function(data,canvas,prot) {
	prot = prot || {};
	var pub = SignalPath.GenericModule(data,canvas,prot)

	var area = null;

	var tableContainer;
	var table;
	var tableHeader;
	var tableBody;
	var rowCount = 0;
	
	/**
	 * Initialization
	 */
	var superCreateDiv = prot.createDiv;
	function createDiv() {
		superCreateDiv();
		
		if (prot.jsonData.tableConfig && prot.jsonData.tableConfig.headers) {
			initTable(prot.jsonData.tableConfig.headers)
		}
		
		if (prot.jsonData.options && prot.jsonData.options.maxRows)
			rowCount = prot.jsonData.options.maxRows.value
	}
	prot.createDiv = createDiv;
	
	function initTable(headers) {
		if (tableContainer)
			tableContainer.remove()
			
		tableContainer = $("<div class='table-module-container'></div>");
		prot.body.append(tableContainer);
		
		table = $("<table class='event-table-module-content table table-condensed table-striped'></table>");
		tableContainer.append(table);
		
		tableHeader = $("<thead></thead>");
		table.append(tableHeader);
		
		for (var i=0;i<headers.length;i++)
			tableHeader.append("<th>"+headers[i]+"</th>");
		
		tableBody = $("<tbody></tbody>");
		table.append(tableBody);
	}
	
	pub.receiveResponse = function(d) {
		// New row message
		if (d.nr) {
			// Remove last row if table full
			if (rowCount>0) {
				var rows = $(tableBody).children();
				if (rows.length==rowCount)
					$(rows[rows.length-1]).remove();
			}
			
			var newRow = $("<tr"+(d.id!=null ? " id='"+d.id+"'" : "")+"></tr>");
			for (var i=0;i<d.nr.length;i++)
				newRow.append("<td>"+(d.nr[i]!=null ? d.nr[i] : "")+"</td>");
			
			tableBody.prepend(newRow);
		}
		// Edit cell message: d.id=row id, d.e=cell index, d.c=cell content 
		else if (d.e!=null) {
			var cell = $('#'+d.id+" td:eq("+d.e+")");
			cell.html(d.c);
		}
		else if (d.hdr) {
			initTable(d.hdr.headers)
		}
	}
	
	var superClean = pub.clean;
	pub.clean = function() {
		superClean();
		
		// Clean rows
		if (tableBody) {
			tableBody.empty()
		}
	}
	
	return pub;
}

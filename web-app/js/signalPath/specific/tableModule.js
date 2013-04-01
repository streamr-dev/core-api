SignalPath.TableModule = function(data,canvas,my) {
	my = my || {};
	var that = SignalPath.GenericModule(data,canvas,my)

	var area = null;

	var tableContainer;
	var table;
	var tableHeader;
	var tableBody;
	var rowCount = 20;
	
	/**
	 * Initialization
	 */
	var superCreateDiv = my.createDiv;
	function createDiv() {
		superCreateDiv();
		
		if (table!=null)
			$(table).remove();
		
		var d = data.tableConfig;
		var headers = d.headers;
		
		tableContainer = $("<div></div>");
		table = $("<table></table>");
		
		tableHeader = $("<thead></thead>");
		
		for (var i=0;i<headers.length;i++)
			tableHeader.append("<th>"+headers[i]+"</th>");
		
		table.append(tableHeader);
		
		tableBody = $("<tbody></tbody>");
//		for (var i=0;i<100;i++)
//			tableBody.append("<tr colspan='"+headers.length+"'></tr>;")
		table.append(tableBody);
		
		tableContainer.append(table);
		my.body.append(tableContainer);
		
		
		rowCount = d.rows;
		if (d.width) {
			$(tableContainer).css("width",d.width);
			$(table).css("width","100%");
			
			if (d.maxHeight) {
				$(tableContainer).css("overflow","hidden");
//				$(tableHeader).css("width",d.width);
				$(table).css("display","block");
				$(table).css("width","100%");
				$(table).css("max-height",d.maxHeight);
				$(table).css("overflow","auto");
			}
		}
	}
	my.createDiv = createDiv;
	
	that.receiveResponse = function(d) {
		// New row message
		if (d.nr) {
			// Remove last row if table full
			if (rowCount!=-1) {
				var rows = $(tableBody).children();
				if (rows.length==rowCount)
					$(rows[rows.length-1]).remove();
			}
			
			var newRow = $("<tr"+(d.id!=null ? " id='"+d.id+"'" : "")+"></tr>");
			for (var i=0;i<d.nr.length;i++)
				newRow.append("<td>"+d.nr[i]+"</td>");
			
			tableBody.prepend(newRow);
		}
		// Edit cell message: d.id=row id, d.e=cell index, d.c=cell content 
		else if (d.e!=null) {
			var cell = $('#'+d.id+" td:eq("+d.e+")");
			cell.html(d.c);
		}
	}
	
	var superClean = that.clean;
	that.clean = function() {
		superClean();
		// Remove all rows
		tableBody.find("tr").remove();
	}
	
	return that;
}

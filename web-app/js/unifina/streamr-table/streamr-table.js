(function(exports) {

function StreamrTable(parent, options) {
	this.$parent = $(parent)

	if (options) {
		this.options = options
		this.rowCount = options.maxRows ? options.maxRows : 0;
	} else {
		this.options = {}
		this.options.maxRows = 0
		this.options.displayTitle = false
	}
}

StreamrTable.prototype.initTable = function (title, headers) {
		
	if (this.tableContainer)
		this.tableContainer.remove()

	this.tableContainer = $("<div class='table-module-container'></div>");
	this.$parent.append(this.tableContainer);

	if (this.options.displayTitle) {
		this.tableCaption = $("<h4 class='streamr-widget-title'>");
		this.tableCaption.text(title);
		this.tableContainer.append(this.tableCaption);
	}

	this.table = $("<table class='event-table-module-content table table-condensed table-striped'></table>");
	this.tableContainer.append(this.table);

	this.tableHeader = $("<thead><tr></tr></thead>");
	this.table.append(this.tableHeader);

	if (headers) {
		for (var i=0; i<headers.length; i++)
			this.tableHeader.find("tr").append("<th>"+headers[i]+"</th>");
	}

	this.tableBody = $("<tbody></tbody>");
	this.table.append(this.tableBody);
}

StreamrTable.prototype.addRow = function (row, rowId, op) {
	if (op != "append") { op = "prepend" }
	var rowIdString = (rowId != null) ? " id='" + rowId + "'" : "";
	var newRow = $("<tr"+ rowIdString +"></tr>");
	for (var i = 0; i < row.length; i++) {
		var content = row[i] == null ? "" :
					  row[i] instanceof Object ? JSON.stringify(row[i]) : row[i];
		newRow.append("<td>" + content + "</td>");
	}
	this.tableBody[op](newRow);
}

StreamrTable.prototype.receiveResponse = function (d) {
	// New row message
	if (d.nr) {
		// Remove last row if table full
		if (this.rowCount>0) {
			var rows = $(this.tableBody).children();
			if (rows.length==this.rowCount)
				$(rows[rows.length-1]).remove();
		}
		
		this.addRow(d.nr, d.id);
	}
	// New contents: 2d array that replaces existing contents
	else if (d.nc) {
		this.tableBody.empty();
		for (var i in d.nc) {
			this.addRow(d.nc[i], "row-" + i, "append");
		}
	}
	// New map
	else if (d.nm) {
		$(this.tableBody).empty();

		for (var key in d.nm) {
			this.addRow([key, d.nm[key]], "row-" + key);
		}
	}
	// Edit cell message: d.id=row id, d.e=cell index, d.c=cell content 
	else if (d.e!=null && d.id) {
		var cell = this.tableBody.find('#'+d.id+ " td").eq(d.e);
		cell.html(d.c);
	}
	else if (d.hdr) {
		this.initTable(d.hdr.title, d.hdr.headers)
	}
}

StreamrTable.prototype.clean = function() {		
	// Clean rows
	if (this.tableBody) {
		this.tableBody.empty()
	}
}

exports.StreamrTable = StreamrTable

})(typeof(exports) !== 'undefined' ? exports : window)
(function(exports) {

function StreamrTable(parent, options) {
	this.$parent = $(parent)
    this.options = options

    this.createTable()
    this.initTable()
}

// Creates tableContainer
StreamrTable.prototype.createTable = function() {
    if (this.tableContainer)
        this.tableContainer.remove()

    this.tableContainer = $("<div class='table-module-container'></div>");
    this.$parent.append(this.tableContainer);
}

// Creates the table, can be called again with different headers
StreamrTable.prototype.initTable = function (title, headers) {
    headers = headers || this.options.headers
    title = title || this.options.title

    this.tableContainer.empty()

	if (this.options.displayTitle) {
        this.setTitle(title)
	}

	this.table = $("<table class='event-table-module-content table table-condensed table-striped'></table>");
	this.tableContainer.append(this.table);

	this.setHeaders(headers)

	this.tableBody = $("<tbody></tbody>");
	this.table.append(this.tableBody);
}

StreamrTable.prototype.setTitle = function(title) {
    if (!this.tableCaption) {
        this.tableCaption = $("<h4 class='streamr-widget-title'/>");
        this.tableCaption.text(title);
        this.tableContainer.append(this.tableCaption);
    } else {
        this.tableCaption.text(title);
    }
}

StreamrTable.prototype.setHeaders = function(headers) {
    this.tableHeader = $("<thead><tr></tr></thead>");
    if (this.table.find('thead').length) {
        this.table.find('thead').replaceWith(this.tableHeader);
    } else {
        this.table.prepend(this.tableHeader)
    }

    if (headers) {
        for (var i=0; i<headers.length; i++) {
            this.tableHeader.find("tr").append("<th>"+headers[i]+"</th>");
        }
    }
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
		this.addRow(d.nr, d.id);
		// Remove last row(s) if table full
	    	while ($(this.tableBody).children().length > (this.options.maxRows || Infinity)) {
			$(this.tableBody).children().last().remove()
		}
	} else if (d.nc) {
	    // New contents: 2d array that replaces existing contents
		this.tableBody.empty();
		for (var i in d.nc) {
			this.addRow(d.nc[i], "row-" + i, "append");
		}
	} else if (d.nm) {
	    // New map
		this.tableBody.empty();
		for (var key in d.nm) {
			this.addRow([key, d.nm[key]], "row-" + key, "append");
		}
	} else if (d.e != null && d.id) {
	    // Edit cell message: d.id=row id, d.e=cell index, d.c=cell content
		var cell = this.tableBody.find('#'+d.id+ " td").eq(d.e);
		cell.text(d.c);
	}
	else if (d.hdr) {
		this.setHeaders(d.hdr.headers)
		if (this.options.displayTitle) {
			this.setTitle(d.hdr.title)
		}
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

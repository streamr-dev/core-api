function StreamrTable(parent, options) {
	var _this = this
	this.$parent = $(parent)

	this.tableContainer;
	this.table;
	this.tableHeader;
	this.tableBody;
	this.rowCount = options.maxRows ? options.maxRows : 0;

	this.options = options ? options : {}
}

StreamrTable.prototype.initTable = function (headers) {
		
		if (this.tableContainer)
			this.tableContainer.remove()
			
		this.tableContainer = $("<div class='table-module-container'></div>");
		this.$parent.append(this.tableContainer);
		
		this.table = $("<table class='event-table-module-content table table-condensed table-striped'></table>");
		this.tableContainer.append(this.table);
		
		this.tableHeader = $("<thead></thead>");
		this.table.append(this.tableHeader);
		
		if(headers) {
			for (var i=0;i<headers.length;i++)
				this.tableHeader.append("<th>"+headers[i]+"</th>");
		}
		
		
		this.tableBody = $("<tbody></tbody>");
		this.table.append(this.tableBody);
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
		
		var newRow = $("<tr"+(d.id!=null ? " id='"+d.id+"'" : "")+"></tr>");
		for (var i=0;i<d.nr.length;i++)
			newRow.append("<td>"+(d.nr[i]!=null ? d.nr[i] : "")+"</td>");
		
		this.tableBody.prepend(newRow);
	}
	// Edit cell message: d.id=row id, d.e=cell index, d.c=cell content 
	else if (d.e!=null) {
		var cell = $('#'+d.id+" td:eq("+d.e+")");
		cell.html(d.c);
	}
	else if (d.hdr) {
		this.initTable(d.hdr.headers)
	}
}

StreamrTable.prototype.clean = function() {		
	// Clean rows
	if (this.tableBody) {
		this.tableBody.empty()
	}
}


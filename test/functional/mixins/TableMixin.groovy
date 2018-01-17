package mixins

/**
 * Useful mixin for tables created with <ui:table> tags
 */
class TableMixin {
	
	def getTableRows(table) {
		return table.find(".tbody .tr")
	}
	
	def getTableRowByData(table, dataKey, value) {
		return table.find(".tbody .tr[data-${dataKey}='${value}']")
	}
}

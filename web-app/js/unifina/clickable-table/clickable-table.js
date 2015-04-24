$( document ).ready(function () {
	var table = $("table.table-clickable")
	if(table){
		var a = table.find("tbody tr")
		$.each(a, function(i, tr){
			if($(tr).attr("data-link")){
				$(tr).click(function(){
					window.location = $(tr).attr("data-link")
				})
			}
		})
	}
})
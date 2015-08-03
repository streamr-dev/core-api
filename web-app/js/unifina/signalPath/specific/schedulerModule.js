SignalPath.SchedulerModule = function(data,canvas,prot) {
	prot = prot || {};
	var pub = SignalPath.GenericModule(data,canvas,prot)

	/**
	 * Initialization
	 */
	var superCreateDiv = prot.createDiv;
	function createDiv() {
		superCreateDiv();
		var container = $("<div/>", {
			class: "scheduler"
		})
		var dateParser = new DateParser()
		prot.body.append(container)
		prot.scheduler = new Scheduler({
			el: container,
			schedule: prot.jsonData.schedule
		})
		prot.scheduler.on('error', function(msg) {
			$.pnotify()
		})
	}

	var superToJSON = pub.toJSON;
	function toJSON() {
		if (!prot.scheduler.validate())
			throw "Scheduler has invalid data"

		prot.jsonData = superToJSON();
		prot.jsonData.schedule = prot.scheduler.buildJSON()

		return prot.jsonData;
	}
	pub.toJSON = toJSON;

	prot.createDiv = createDiv;

	return pub;
}



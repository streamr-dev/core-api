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
		prot.body.append(container)
		prot.scheduler = new Scheduler({
			el: container,
			schedule: prot.jsonData.schedule
		})
		prot.scheduler.on('Error', function(msg) {
			$.pnotify({
				type: 'error',
        		title: 'Something went wrong!',
	        	text: msg.text,
	        	delay: 4000
			})
		})
		prot.scheduler.on("update", function(){
			prot.redraw()
		})
	}

	pub.receiveResponse = function(d){
		console.log(d)
	}

	var superToJSON = pub.toJSON;
	function toJSON() {
		var schedulerJSON

		try {
			schedulerJSON = prot.scheduler.buildJSON()
		} catch (e) {
			throw "Scheduler has invalid data"
		}

		prot.jsonData = superToJSON();
		prot.jsonData.schedule = schedulerJSON

		return prot.jsonData;
	}

	function getScheduler() {
		return prot.scheduler
	}

	pub.toJSON = toJSON;

	prot.createDiv = createDiv;

	return pub;
}



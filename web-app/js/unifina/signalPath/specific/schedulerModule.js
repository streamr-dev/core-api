SignalPath.SchedulerModule = function(data,canvas,prot) {
	prot = prot || {};
	var pub = SignalPath.UIChannelModule(data,canvas,prot)

	/**
	 * Initialization
	 */
	var superCreateDiv = prot.createDiv;
	function createDiv() {
		prot.jsonData.canCollapse = true
		superCreateDiv();
		var container = $("<div/>", {
			class: "scheduler"
		})
		prot.body.append(container)

		prot.scheduler = new Scheduler({
			el: container,
			footerEl: prot.footer,
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
		
		prot.redraw()
	}
	prot.createDiv = createDiv;

	prot.receiveResponse = function(payload) {
		prot.scheduler.highlightActives(payload.activeRules)
	}

	prot.getUIChannelOptions = function() {
		// Force resending of only last value
		return { resend_last: 1 }
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
	pub.toJSON = toJSON;

	function getScheduler() {
		return prot.scheduler
	}



	return pub;
}



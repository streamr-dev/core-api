package pages

class TaskWorkerPage extends GrailsPage {

	static controller = "taskWorker"
	static action = "status"
	
	static url = "$controller/$action"
	
	static content = {
		navbar { module NavbarModule }
		startWorkerButton { $("#startWorker") }
		taskWorkerTable { $("#task-worker-table") }
	}
}

package com.unifina.controller.data

import com.unifina.domain.data.Feed
import com.unifina.domain.data.FeedFile
import com.unifina.feed.file.AbstractFeedFileDiscoveryUtil
import com.unifina.service.FeedFileService

class FeedFileController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

	FeedFileService feedFileService
	
    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [feedFileList: FeedFile.list(params), feedFileTotal: FeedFile.count()]
    }

    def create = {
        def feedFile = new FeedFile()
        feedFile.properties = params
        return [feedFile: feedFile]
    }

    def save = {
        def feedFile = new FeedFile(params)
        if (feedFile.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'feedFile.label', default: 'FeedFile'), feedFile.id])}"
            redirect(action: "show", id: feedFile.id)
        }
        else {
            render(view: "create", model: [feedFile: feedFile])
        }
    }
	
	def discover() {
		List messages = []
		List feedIds = params.list("id")
		
		// Use ids given as params, or if none given, get all feeds
		List<Feed> feeds = (feedIds!=null && feedIds.size()>0 ? Feed.getAll(feedIds) : Feed.list())
		
		feeds.each {Feed feed->
			AbstractFeedFileDiscoveryUtil du = feedFileService.getDiscoveryUtil(feed)
			if (du) {
				int count = du.discover()
				messages << "Found $count new files for feed $feed.id"
			}
			else messages << "File discovery not enabled for feed $feed.id"
		}
		
		if (messages.size()>0)
			flash.message = messages.join("<br>")
			
		redirect(action:"index")
	}
	
	def preprocess() {
		FeedFile.findAllByProcessed(false).findAll {!it.processing && !it.processTaskCreated}.each {
			feedFileService.createPreprocessTask(it)
		}
		
		flash.message = "Preprocess tasks created."
		
		redirect(action:"index")
	}
	
    def show = {
        def feedFile = FeedFile.get(params.id)
        if (!feedFile) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'feedFile.label', default: 'FeedFile'), params.id])}"
            redirect(action: "list")
        }
        else {
            [feedFile: feedFile]
        }
    }

    def edit = {
        def feedFile = FeedFile.get(params.id)
        if (!feedFile) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'feedFile.label', default: 'FeedFile'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [feedFile: feedFile]
        }
    }

    def update = {
        def feedFile = FeedFile.get(params.id)
        if (feedFile) {
            if (params.version) {
                def version = params.version.toLong()
                if (feedFile.version > version) {
                    
                    feedFile.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'feedFile.label', default: 'FeedFile')] as Object[], "Another user has updated this FeedFile while you were editing")
                    render(view: "edit", model: [feedFile: feedFile])
                    return
                }
            }
            feedFile.properties = params
            if (!feedFile.hasErrors() && feedFile.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'feedFile.label', default: 'FeedFile'), feedFile.id])}"
                redirect(action: "show", id: feedFile.id)
            }
            else {
                render(view: "edit", model: [feedFile: feedFile])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'feedFile.label', default: 'FeedFile'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def feedFile = FeedFile.get(params.id)
        if (feedFile) {
            try {
                feedFile.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'feedFile.label', default: 'FeedFile'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'feedFile.label', default: 'FeedFile'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'feedFile.label', default: 'FeedFile'), params.id])}"
            redirect(action: "list")
        }
    }
}

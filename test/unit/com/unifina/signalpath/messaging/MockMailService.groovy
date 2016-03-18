package com.unifina.signalpath.messaging

class MockMailService {

	def mailSent = false
	def from
	def to
	def subject
	def body
	def html
	
	public MockMailService() {
	
	}
	
	def sendMail(Closure c) {
		mailSent = true
		def delegate = new MailDelegate()
		c.delegate = delegate
		c.call(delegate)
	}

	def clear() {
		mailSent = false
		from = null
		to = null
		subject = null
		body = null
		html = null
	}

	public class MailDelegate {
		def from(s) {
			from = s
		}
		def to(s) {
			to = s
		}
		def subject(s) {
			subject = s
		}
		def body(s) {
			body = s
		}
		def html(s) {
			html = s
		}
	}
}

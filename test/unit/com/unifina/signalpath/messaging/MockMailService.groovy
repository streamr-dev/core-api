package com.unifina.signalpath.messaging

class MockMailService {

	def mailSent = false
	def from
	def to
	def subject
	def body
	
	public MockMailService() {
	
	}
	
	def sendMail(Closure c) {
		mailSent = true
		def delegate = new MailDelegate()
		c.delegate = delegate
		c.call(delegate)
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
	}

}

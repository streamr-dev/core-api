package com.unifina.signalpath.remote

import grails.converters.JSON
import grails.test.mixin.*
import grails.test.mixin.support.*

import org.apache.http.client.utils.URIBuilder
import org.junit.*

import com.mashape.unirest.http.HttpResponse
import com.mashape.unirest.http.JsonNode
import com.mashape.unirest.http.Unirest

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
class RemoteJsonApiTests {

    void setUp() {
        // Setup logic here
    }

    void tearDown() {
        // Tear down logic here
    }

    void testSomething() {
		URIBuilder builder = new URIBuilder("https://textalytics-textalytics-sentiment-analysis-11.p.mashape.com/sentiment-1.1.php")
		.setParameter("of", "json")
		.setParameter("txt", "What a happy test this is!");
		
		String uri = builder.build().toString()
		
		HttpResponse<JsonNode> request = Unirest.get(builder.build().toString())
		  .header("X-Mashape-Authorization", "d6daq4euknsX84PFvDzwHdN3jF26qL3S")
		  .header("accept", "application/json")
		  .asString();
		  
		  println request.getBody().toString()
		  
//		  HttpResponse<JsonNode> request = Unirest.get("https://textalytics-textalytics-sentiment-analysis-11.p.mashape.com/sentiment-1.1?of=json&txt=This%20has%20been%20a%20terrible%20day.&model=es-general&entities=%3Centities%3E&concepts=%3Cconcepts%3E")
//		  .header("X-Mashape-Authorization", "d6daq4euknsX84PFvDzwHdN3jF26qL3S")
//		  .asString();
		  
		  def json = JSON.parse(request.getBody().toString())
		  assert json.score == "0.60"
		  assert request.code == 200
		  
//		assert jsonResponse.code == 200
    }
}

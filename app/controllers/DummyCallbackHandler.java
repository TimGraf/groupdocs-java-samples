package controllers;

import java.util.HashMap;
import java.util.Map;

import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

public class DummyCallbackHandler extends Controller {
	
	@BodyParser.Of(value = BodyParser.TolerantText.class)
	public static Result index() {
		System.out.println("Method: " + request().method());
		System.out.println("URL: " + request().uri());
		System.out.println("Headers: " + flattenMap(request().headers()));
		System.out.println("Body: " + request().body().asText());
		System.out.println("");
		
		return ok();
	}
	
	private static Map<String, String> flattenMap(Map<String, String[]> headers){
		Map<String, String> temp = new HashMap<String, String>();
				
		for(String header : headers.keySet()){
			temp.put(header, request().getHeader(header));
		}
		
		return temp;
	}
}
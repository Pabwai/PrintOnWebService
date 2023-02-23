package com.print.conrtoller;

import org.apache.tomcat.util.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.print.service.CreatePolicyPDF;


@RestController
public class ApiServiceController {
	
	@Autowired
	CreatePolicyPDF createPolicyPDF;
	
	@PostMapping(value = "/print", 
			consumes = "application/json; charset=utf-8", 
			produces = "application/json; charset=utf-8")
	 public ResponseEntity<String> pdf(@RequestBody String request) throws JSONException{

		
		JSONObject dataJson = new JSONObject(request);
		
		byte[] bytePDF = createPolicyPDF.setFilePDF(dataJson);

		return new ResponseEntity<String>(Base64.encodeBase64String(bytePDF), HttpStatus.OK); 
		
		
	}
	
}

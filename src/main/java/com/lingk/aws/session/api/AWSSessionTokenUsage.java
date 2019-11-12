package com.lingk.aws.session.api;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.fission.Context;
import io.fission.Function;

@SuppressWarnings("rawtypes")
public class AWSSessionTokenUsage implements Function {
	ObjectMapper mapper = new ObjectMapper();

	@SuppressWarnings("unchecked")
	public ResponseEntity call(RequestEntity req, Context context) {

		StringBuffer sb = new StringBuffer();
		try {
			JsonNode body = mapper.readTree(req.getBody().toString());
			Iterator<Map.Entry<String, JsonNode>> jsonFields = body.fields();
			sb.append("#linux");
			while (jsonFields.hasNext()) {
				Map.Entry<String, JsonNode> next = jsonFields.next();
				sb.append(MessageFormat.format("export {}={}\n", next.getKey(), next.getValue().asText()));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			sb.append(e.getMessage());
		}
		HttpHeaders httpHeaders = new HttpHeaders();
		//httpHeaders.setContentType(org.springframework.http.MediaType.TEXT_PLAIN);
		return new ResponseEntity<StringBuffer>(sb, httpHeaders, HttpStatus.OK);
	}
}

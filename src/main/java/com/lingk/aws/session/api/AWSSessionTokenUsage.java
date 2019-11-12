package com.lingk.aws.session.api;

import java.text.MessageFormat;
import java.util.HashMap;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import io.fission.Context;
import io.fission.Function;

@SuppressWarnings("rawtypes")
public class AWSSessionTokenUsage implements Function {
	@SuppressWarnings("unchecked")
	public ResponseEntity call(RequestEntity req, Context context) {
		HashMap data = (HashMap) req.getBody();
		StringBuffer sb = new StringBuffer();
		sb.append("#linux");
		for (Object key : data.keySet()) {
			sb.append(MessageFormat.format("export {}={}\n", key, data.get(key)));
		}

		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(org.springframework.http.MediaType.TEXT_PLAIN);
		return new ResponseEntity(sb.toString(), httpHeaders, HttpStatus.OK);
	}
}

package com.lingk.aws.session.api;

import java.text.MessageFormat;
import java.util.HashMap;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.fission.Context;
import io.fission.Function;

@SuppressWarnings("rawtypes")
public class AWSSessionTokenUsage implements Function {
	ObjectMapper mapper = new ObjectMapper();

	@SuppressWarnings("serial")
	static HashMap<String, String> replacer = new HashMap<String, String>() {
		{
			put("sessionToken", "AWS_SECRET_ACCESS_KEY");
			put("awsaccessKeyId", "AWS_ACCESS_KEY_ID");
			put("awssecretKey", "AWS_SECRET_ACCESS_KEY");
		}
	};

	public ResponseEntity call(RequestEntity req, Context context) {
		StringBuffer sb = new StringBuffer();
		try {
			HashMap data = (HashMap) req.getBody();
			sb.append("#####linux\n\n");
			for (Object key : data.keySet()) {
				sb.append(MessageFormat.format("export {0}={1}\n", replacer.get(key), data.get(key)));
			}

			sb.append("\n\n#####windows\n\n");
			for (Object key : data.keySet()) {
				sb.append(MessageFormat.format("set {0}={1}\n", replacer.get(key), data.get(key)));
			}

		} catch (Exception e) {
			sb.append(e.getMessage());
		}
		HttpHeaders httpHeaders = new HttpHeaders();
		return new ResponseEntity<StringBuffer>(sb, httpHeaders, HttpStatus.OK);
	}
}

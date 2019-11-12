package com.lingk.aws.session.api;

import java.text.MessageFormat;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.fission.Context;
import io.fission.Function;

@SuppressWarnings("rawtypes")
public class AWSSessionTokenUsage implements Function {
	ObjectMapper mapper = new ObjectMapper();

	public ResponseEntity call(RequestEntity req, Context context) {
		MultiValueMap<String, String> parameters = UriComponentsBuilder.fromUri(req.getUrl()).build().getQueryParams();

		StringBuffer sb = new StringBuffer();
		try {
			sb.append("#####linux\n\n");
			sb.append(MessageFormat.format("export AWS_SESSION_TOKEN={0}\n", parameters.getFirst("sessionToken")));
			sb.append(MessageFormat.format("export AWS_ACCESS_KEY_ID={0}\n", parameters.getFirst("awsaccessKeyId")));
			sb.append(MessageFormat.format("export AWS_SECRET_ACCESS_KEY={0}\n", parameters.getFirst("awssecretKey")));
			sb.append("\n\n#####windows\n\n");
			sb.append(MessageFormat.format("set AWS_SESSION_TOKEN={0}\n", parameters.getFirst("sessionToken")));
			sb.append(MessageFormat.format("set AWS_ACCESS_KEY_ID={0}\n", parameters.getFirst("awsaccessKeyId")));
			sb.append(MessageFormat.format("set AWS_SECRET_ACCESS_KEY={0}\n", parameters.getFirst("awssecretKey")));

		} catch (Exception e) {
			sb.append(e.getMessage());
		}
		HttpHeaders httpHeaders = new HttpHeaders();
		return new ResponseEntity<StringBuffer>(sb, httpHeaders, HttpStatus.OK);
	}
}

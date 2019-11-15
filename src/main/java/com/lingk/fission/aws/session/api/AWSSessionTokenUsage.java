package com.lingk.fission.aws.session.api;

import java.text.MessageFormat;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.fission.Context;
import io.fission.Function;

@SuppressWarnings("rawtypes")
public class AWSSessionTokenUsage implements Function {
	ObjectMapper mapper = new ObjectMapper();

	public ResponseEntity call(RequestEntity req, Context context) {
		StringBuffer sb = new StringBuffer();
		try {
			MultiValueMap<String, String> parameters = UriComponentsBuilder.fromUri(req.getUrl()).build().getQueryParams();
			Jwt jwt = JwtHelper.decode(parameters.getFirst("jwt"));
			JsonNode claims = mapper.readTree(jwt.getClaims());
			sb.append(MessageFormat.format("aws configure set aws_access_key_id {0} --profile lingk-fission\\n", claims.get("sessionToken")));
			sb.append(MessageFormat.format("aws configure set aws_secret_access_key {0} --profile lingk-fission\\n", claims.get("awsaccessKeyId")));
			sb.append(MessageFormat.format("aws configure set aws_session_token {0} --profile lingk-fission\\n", claims.get("awssecretKey")));
			sb.append("aws eks --region TOBE-REPLACED-WITH-REGION update-kubeconfig --name cluster TOBE-REPLACED-WITH-CLUSTER --profile lingk-fission\\n");

		} catch (Exception e) {
			sb.append(e.getMessage());
		}
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.TEXT_PLAIN);
		return new ResponseEntity<StringBuffer>(sb, httpHeaders, HttpStatus.OK);
	}

}

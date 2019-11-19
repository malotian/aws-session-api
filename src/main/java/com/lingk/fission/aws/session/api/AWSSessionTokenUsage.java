package com.lingk.fission.aws.session.api;

import java.text.MessageFormat;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
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
	public static void main(String[] args) {
		final String validity = "2019-11-18T00:23:12.000Z";
		final DateTime dt = new DateTime(validity);
		System.out.println("valid till: " + dt.toString(DateTimeFormat.fullDateTime()));

	}

	ObjectMapper mapper = new ObjectMapper();

	@Override
	public ResponseEntity call(RequestEntity req, Context context) {
		final StringBuffer sb = new StringBuffer();
		try {
			final MultiValueMap<String, String> parameters = UriComponentsBuilder.fromUri(req.getUrl()).build().getQueryParams();
			final Jwt jwt = JwtHelper.decode(parameters.getFirst("jwt"));
			final JsonNode claims = mapper.readTree(jwt.getClaims());
			final DateTime dt = new DateTime(claims.get("Expiration").asText());
			sb.append("# execute following command on terminal, note: session will be valid till: " + dt.toString(DateTimeFormat.fullDateTime()));
			sb.append("\n\n");
			sb.append(MessageFormat.format("aws configure set aws_access_key_id {0} --profile lingk-fission", claims.get("AccessKeyId")));
			sb.append("\n\n");
			sb.append(MessageFormat.format("aws configure set aws_secret_access_key {0} --profile lingk-fission", claims.get("SecretAccessKey")));
			sb.append("\n\n");
			sb.append(MessageFormat.format("aws configure set aws_session_token {0} --profile lingk-fission", claims.get("SessionToken")));
			sb.append("\n\n");
			sb.append("aws eks --region <TOBE-REPLACED-WITH-REGION> update-kubeconfig --name fission-<TOBE-REPLACED-WITH-REGION> --profile lingk-fission");
			sb.append("\n\n");
		} catch (final Exception e) {
			sb.append(e.getMessage());
		}
		final HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.TEXT_PLAIN);
		return new ResponseEntity<>(sb, httpHeaders, HttpStatus.OK);
	}

}

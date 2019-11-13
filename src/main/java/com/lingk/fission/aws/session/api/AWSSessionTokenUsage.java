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

			sb.append("#####linux\n\n");
			sb.append(MessageFormat.format("export AWS_SESSION_TOKEN={0}\n", claims.get("sessionToken")));
			sb.append(MessageFormat.format("export AWS_ACCESS_KEY_ID={0}\n", claims.get("awsaccessKeyId")));
			sb.append(MessageFormat.format("export AWS_SECRET_ACCESS_KEY={0}\n", claims.get("awssecretKey")));
			sb.append("\n\n#####windows\n\n");
			sb.append(MessageFormat.format("set AWS_SESSION_TOKEN={0}\n", claims.get("sessionToken")));
			sb.append(MessageFormat.format("set AWS_ACCESS_KEY_ID={0}\n", claims.get("awsaccessKeyId")));
			sb.append(MessageFormat.format("set AWS_SECRET_ACCESS_KEY={0}\n", claims.get("awssecretKey")));

		} catch (Exception e) {
			sb.append(e.getMessage());
		}
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.TEXT_PLAIN);
		return new ResponseEntity<StringBuffer>(sb, httpHeaders, HttpStatus.OK);
	}
}

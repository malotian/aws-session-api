package com.lingk.faas.fission.aws.session.provider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
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

	static ObjectMapper mapper = new ObjectMapper();

	@Override
	public ResponseEntity call(RequestEntity req, Context context) {
		final HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.TEXT_HTML);

		final StringBuffer sb = new StringBuffer();
		try {
			final MultiValueMap<String, String> parameters = UriComponentsBuilder.fromUri(req.getUrl()).build().getQueryParams();
			final Jwt jwt = JwtHelper.decode(parameters.getFirst("jwt"));
			final JsonNode claims = mapper.readTree(jwt.getClaims());
			final DateTime dt = new DateTime(claims.get("Expiration").asText());
			sb.append("# copy and execute following commands on terminal\n");
			sb.append(MessageFormat.format("\taws configure set aws_access_key_id {0} --profile lingk-fission\n", claims.get("AccessKeyId")));
			sb.append(MessageFormat.format("\taws configure set aws_secret_access_key {0} --profile lingk-fission\n", claims.get("SecretAccessKey")));
			sb.append(MessageFormat.format("\taws configure set aws_session_token {0} --profile lingk-fission\n", claims.get("SessionToken")));
			sb.append(MessageFormat.format("\taws eks --region {0} update-kubeconfig --name fission-{0} --profile lingk-fission\n\n\n", "us-east-1"));
			sb.append(MessageFormat.format("#### **note**: with above fission access will be valid till: {0}", dt.toString(DateTimeFormat.fullDateTime())));
			StringBuilder template = new StringBuilder();
			try (BufferedReader br = new BufferedReader(new InputStreamReader(AWSSessionTokenUsage.class.getClassLoader().getResourceAsStream("template.html")))) {
				String line;
				while ((line = br.readLine()) != null) {
					template.append(line).append("\n");
				}
			}

			String response = template.toString().replaceAll("<!--MARKDOWN HERE-->", sb.toString());
			return new ResponseEntity<String>(response, httpHeaders, HttpStatus.OK);

		} catch (final Exception e) {
			sb.append(e.getMessage());
			return new ResponseEntity<>(sb, httpHeaders, HttpStatus.OK);
		}
	}
}

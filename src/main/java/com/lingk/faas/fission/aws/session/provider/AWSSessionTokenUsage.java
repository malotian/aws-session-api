package com.lingk.faas.fission.aws.session.provider;

import java.io.IOException;
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
			sb.append(MessageFormat.format("# **note**: with above fission access will be valid till: {0}", dt.toString(DateTimeFormat.fullDateTime())));
			Files.readString(Paths.get("template.html")).replaceAll("<!--MARKDOWN HERE-->", sb.toString());

		} catch (final Exception e) {
			sb.append(e.getMessage());
		}
		final HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.TEXT_PLAIN);
		return new ResponseEntity<>(sb, httpHeaders, HttpStatus.OK);
	}

	public static void main(String[] args) throws IOException {
		final MultiValueMap<String, String> parameters = UriComponentsBuilder.fromUriString(
				"https://fission.lingkcore.com/aws-session-api-usage?jwt=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJBY2Nlc3NLZXlJZCI6IkFTSUE2R1I0TjZSRlFMWkpOU05FIiwiU2VjcmV0QWNjZXNzS2V5IjoiV3JDY2FhSzBWVG9RZ282cWR5T21xb1BOMHF2dXMzR3JmaVB2Ly8yTiIsIlNlc3Npb25Ub2tlbiI6IkZ3b0daWEl2WVhkekVJSC8vLy8vLy8vLy93RWFEUHVPTkJHRkIwR3VyNzdjM2lLQkFYREhCd2gwL0U1dHVLNUMxQUFITlBzK0UvMnhDS0RDL3ZBS0gza0p6OXZXRWhKSEZDNVo0T2RSOVhnRnFwVXVKc3BhL1U0Ym9FUzNYZENYcEd5a3E0d2hUWDcwdHZtaTBYdGJHbzUxcCtxMmp3Rk53UGtVV2tGR2ZKem9DNE83UUI1cUJOalg3RDdBVmlDdFd2SmJjemVuR08rcmN0dzFmQUVJMTBQd3FuVlpHU2pFcU1mdUJUSW9RM0d6Q29Iak9MR3ZtcFVMYm5lK3pxRjlsTFdzenRvelRKWXhYWDQ3WWNqclBrSWVKWUlHY3c9PSIsIkV4cGlyYXRpb24iOiIyMDE5LTExLTE4VDAwOjE0OjEyLjAwMFoiLCJpYXQiOjE1NzQwMzI0NTJ9.nrKEIQanTDa4TvQ2B2cNCYZNWm8OrkqWqu6N4sXXMQU&state=g6Fo2SAwZDc1dUVYX0VIMmVpQ1cxRjNFQWFhRlFRcDgwRzRHWaN0aWTZIHE1Zlc2VkVvQ3M4SWZETDRxdUZoZDBEY2NUMUdmRWpUo2NpZNkgSlQwRGtJSXZIN28weElITWQ2NE1YMUZuZFVyTm5rdzA")
				.build().getQueryParams();
		final Jwt jwt = JwtHelper.decode(parameters.getFirst("jwt"));
		final JsonNode claims = mapper.readTree(jwt.getClaims());
		final DateTime dt = new DateTime(claims.get("Expiration").asText());
		final StringBuffer sb = new StringBuffer();
		sb.append("# copy and execute following commands on terminal\n");
		sb.append(MessageFormat.format("`aws configure set aws_access_key_id {0} --profile lingk-fission`\n\n", claims.get("AccessKeyId")));
		sb.append(MessageFormat.format("`aws configure set aws_secret_access_key {0} --profile lingk-fission`\n\n", claims.get("SecretAccessKey")));
		sb.append(MessageFormat.format("`aws configure set aws_session_token {0} --profile lingk-fission`\n\n", claims.get("SessionToken")));
		sb.append(MessageFormat.format("`aws eks --region {0} update-kubeconfig --name fission-{0} --profile lingk-fission`\n\n\n", "us-east-1"));
		sb.append(MessageFormat.format("#### note: with above fission access will be valid till: {0}", dt.toString(DateTimeFormat.fullDateTime())));
		String response = Files.readString(Paths.get("src/main/resources/template.html")).replaceAll("<!--MARKDOWN HERE-->", sb.toString());
		System.out.println(response);
	}

}

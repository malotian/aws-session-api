package com.lingk.aws.session.api;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.Credentials;
import com.amazonaws.services.securitytoken.model.GetSessionTokenRequest;
import com.amazonaws.services.securitytoken.model.GetSessionTokenResult;

import io.fission.Context;
import io.fission.Function;

@SuppressWarnings("rawtypes")
public class AWSSessionTokenHelper implements Function {

	private static Logger LOG = LoggerFactory.getLogger(AWSSessionTokenHelper.class);

	static HashMap<BasicAWSCredentials, AWSSecurityTokenService> stsClients = new HashMap<BasicAWSCredentials, AWSSecurityTokenService>();

	static AWSSecurityTokenService getSTSClient(String accessKey, String secretKey) {

		BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);

		if (stsClients.containsKey(credentials))
			return stsClients.get(credentials);

		AWSSecurityTokenService service = AWSSecurityTokenServiceClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentials))
				.withRegion(Regions.DEFAULT_REGION).build();
		stsClients.put(credentials, service);
		return service;
	}

	public ResponseEntity call(RequestEntity req, Context context) {

		try {

			MultiValueMap<String, String> parameters = UriComponentsBuilder.fromUri(req.getUrl()).build().getQueryParams();
			AWSSecurityTokenService stsClient = getSTSClient(parameters.getFirst("accessKey"), parameters.getFirst("secretKey"));

			GetSessionTokenRequest getSessionTokenRequest = new GetSessionTokenRequest().withDurationSeconds(7200);
			GetSessionTokenResult sessionTokenResult = stsClient.getSessionToken(getSessionTokenRequest);
			Credentials sessionCredentials = sessionTokenResult.getCredentials().withSessionToken(sessionTokenResult.getCredentials().getSessionToken())
					.withExpiration(sessionTokenResult.getCredentials().getExpiration());

			BasicSessionCredentials basicSessionCredentials = new BasicSessionCredentials(sessionCredentials.getAccessKeyId(), sessionCredentials.getSecretAccessKey(),
					sessionCredentials.getSessionToken());

			HttpHeaders headers = new HttpHeaders();
			headers.add("content-type", "application/json");
			return new ResponseEntity<BasicSessionCredentials>(basicSessionCredentials, headers, HttpStatus.OK);
		} catch (Exception e) {
			LOG.info("error", e);
			return new ResponseEntity<Exception>(e, HttpStatus.BAD_REQUEST);
		}
	}
}

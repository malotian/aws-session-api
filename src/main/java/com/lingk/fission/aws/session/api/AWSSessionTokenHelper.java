package com.lingk.fission.aws.session.api;

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

	static HashMap<BasicAWSCredentials, AWSSecurityTokenService> stsClients = new HashMap<>();

	static AWSSecurityTokenService getSTSClient(String accessKey, String secretKey) {

		final BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);

		if (AWSSessionTokenHelper.stsClients.containsKey(credentials)) {
			return AWSSessionTokenHelper.stsClients.get(credentials);
		}

		final AWSSecurityTokenService service = AWSSecurityTokenServiceClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentials))
				.withRegion(Regions.DEFAULT_REGION).build();
		AWSSessionTokenHelper.stsClients.put(credentials, service);
		return service;
	}

	@Override
	public ResponseEntity call(RequestEntity req, Context context) {

		try {

			final MultiValueMap<String, String> parameters = UriComponentsBuilder.fromUri(req.getUrl()).build().getQueryParams();
			final AWSSecurityTokenService stsClient = AWSSessionTokenHelper.getSTSClient(parameters.getFirst("accessKey"), parameters.getFirst("secretKey"));

			final GetSessionTokenRequest getSessionTokenRequest = new GetSessionTokenRequest().withDurationSeconds(7200);
			final GetSessionTokenResult sessionTokenResult = stsClient.getSessionToken(getSessionTokenRequest);
			final Credentials sessionCredentials = sessionTokenResult.getCredentials().withSessionToken(sessionTokenResult.getCredentials().getSessionToken())
					.withExpiration(sessionTokenResult.getCredentials().getExpiration());

			final BasicSessionCredentials basicSessionCredentials = new BasicSessionCredentials(sessionCredentials.getAccessKeyId(), sessionCredentials.getSecretAccessKey(),
					sessionCredentials.getSessionToken());

			final HttpHeaders headers = new HttpHeaders();
			headers.add("content-type", "application/json");
			return new ResponseEntity<>(basicSessionCredentials, headers, HttpStatus.OK);
		} catch (final Exception e) {
			AWSSessionTokenHelper.LOG.info("error", e);
			return new ResponseEntity<>(e, HttpStatus.BAD_REQUEST);
		}
	}
}

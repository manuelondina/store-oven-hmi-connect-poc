package com.mercadona.poc.infraestructure.utilities;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercadona.poc.infraestructure.dto.LoginDTO;
import com.mercadona.poc.infraestructure.service.SendFilesService;

public class Utilities {

	private static final Logger LOGGER = LoggerFactory.getLogger(SendFilesService.class);

	private Utilities() {
		throw new AssertionError("Utility class cannot be instantiated.");
	}

	// Get the client to keep the connection alive
	public static CloseableHttpClient getClient() {
		ConnectionKeepAliveStrategy keepAliveStrat = new DefaultConnectionKeepAliveStrategy() {
			@Override
			public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
				long keepAlive = super.getKeepAliveDuration(response, context);
				if (keepAlive == -1) {
					keepAlive = 10000;
					;
				}
				return keepAlive;
			}
		};
		// Create the client with the keep alive strategy
		return HttpClients.custom().setKeepAliveStrategy(keepAliveStrat).build();
	}

	// Prepare the values to send to the login page
	public static List<BasicNameValuePair> prepareValues(String user, String password, String token, String url) {
		List<BasicNameValuePair> nameValuePairs = new ArrayList<>();
		nameValuePairs.add(new BasicNameValuePair("Login", user));
		nameValuePairs.add(new BasicNameValuePair("Token", token));
		nameValuePairs.add(new BasicNameValuePair("Redirection", url));
		nameValuePairs.add(new BasicNameValuePair("Password", password));
		return nameValuePairs;
	}

	// Find the tokens in the response
	public static LoginDTO findTokens(String sResponse, LoginDTO loginDTO, String sToken, String mToken) {
		int posI = sResponse.indexOf("NAME='SingleUseToken'") + 29;
		int posF = sResponse.indexOf("'", posI);
		sToken = sResponse.substring(posI, posF);

		posI = sResponse.indexOf("name='MultiUseToken'") + 28;
		posF = sResponse.indexOf("'", posI);
		mToken = sResponse.substring(posI, posF);

		LOGGER.info("Conectandose a {}", loginDTO.getOvenHost());

		loginDTO.setSingleToken(sToken);
		loginDTO.setMultiUsetoken(mToken);
		return loginDTO;
	}

	// Encode the user and password to Base64
	public static String encodeToBase64(String user, String pass) {
		String combinedValues = user + ":" + pass;
		byte[] encodedBytes = Base64.getEncoder().encode(combinedValues.getBytes());
		String encodedString = new String(encodedBytes);
		return "Basic " + encodedString;
	}
}

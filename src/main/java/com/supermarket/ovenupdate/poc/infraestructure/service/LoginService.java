package com.supermarket.ovenupdate.poc.infraestructure.service;

import static com.supermarket.ovenupdate.poc.constants.PocServerEnum.FILES;
import static com.supermarket.ovenupdate.poc.constants.PocServerEnum.LOGIN_FORM;
import static com.supermarket.ovenupdate.poc.constants.PocServerEnum.LOGIN_TOKEN;
import static com.supermarket.ovenupdate.poc.constants.PocServerEnum.PROTOCOL;
import static com.supermarket.ovenupdate.poc.constants.PocServerEnum.START_HTML;
import static com.supermarket.ovenupdate.poc.constants.PocServerEnum.UP_SERVER;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.supermarket.ovenupdate.poc.config.OvenPropertiesConfig;
import com.supermarket.ovenupdate.poc.infraestructure.dto.LoginDTO;
import com.supermarket.ovenupdate.poc.infraestructure.utilities.Utilities;

@Service
public class LoginService {

	@Autowired
	private OvenPropertiesConfig properties;

	private static final Logger LOGGER = LoggerFactory.getLogger(SendFilesService.class);

	String mtoken = null;
	String stoken = null;
	String cookie = null;

	LoginDTO login(String ovenName) {
		// Service to login in the oven
		LoginDTO loginDTO = new LoginDTO();
		loginDTO.setOvenHost(ovenName);
		loginDTO.setUser(properties.getUsers().get("username"));
		loginDTO.setPassword(properties.getUsers().get("password"));

		try {
			// START URL
			String startUrl = PROTOCOL + loginDTO.getOvenHost() + START_HTML;
			CloseableHttpClient client = Utilities.getClient();
			URI address = new URI(startUrl);
			HttpGet httpGet = new HttpGet(address);

			CloseableHttpResponse response = client.execute(httpGet);

			HttpEntity entity = response.getEntity();
			String sResponse = EntityUtils.toString(entity);
			response.close();

			// SEND LOGIN FORM
			loginForm(client, response, entity, address, sResponse, loginDTO, startUrl);

			// DO REDIRECT TO START URL
			redirect(client, response, entity, address, sResponse, loginDTO, startUrl);

			// BROWSE FILE PAGE TO GET TOKENS
			sResponse = browseFiles(client, response, entity, address, sResponse, loginDTO, startUrl);

			// Find Tokens for the sendFiles method when it redirects to the URL
			loginDTO = Utilities.findTokens(sResponse, loginDTO, this.stoken, this.mtoken);
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			e.printStackTrace();
		}
		return loginDTO;
	}

	// Login form to get the cookie
	private LoginDTO loginForm(CloseableHttpClient client, CloseableHttpResponse response, HttpEntity entity,
			URI address, String sResponse, LoginDTO loginDTO, String startUrl)
			throws URISyntaxException, IOException {
		address = new URI(PROTOCOL + loginDTO.getOvenHost() + LOGIN_FORM);

		HttpPost httpPost = new HttpPost(address);

		List<BasicNameValuePair> nameValuePairs = Utilities.prepareValues(loginDTO.getUser(), loginDTO.getPassword(),
				LOGIN_TOKEN, startUrl);

		httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, StandardCharsets.UTF_8));
		// Response from the server after login
		response = client.execute(httpPost);

		Header[] headers = response.getHeaders("set-cookie");
		String h = headers[0].toString();
		int posI = h.indexOf("siemens_ad_session=") + 19;
		int posF = h.indexOf(";", posI);
		cookie = h.substring(posI, posF);
		loginDTO.setCookie(cookie);

		entity = response.getEntity();
		sResponse = EntityUtils.toString(entity);
		response.close();
		return loginDTO;
	}

	// Browse files to get the tokens
	private String browseFiles(CloseableHttpClient client, CloseableHttpResponse response, HttpEntity entity,
			URI address, String sResponse, LoginDTO loginDTO, String startUrl)
			throws URISyntaxException, ClientProtocolException, IOException {
		address = new URI(PROTOCOL + loginDTO.getOvenHost() + FILES + UP_SERVER);

		HttpGet httpGet = new HttpGet(address);

		response = client.execute(httpGet);

		entity = response.getEntity();
		sResponse = EntityUtils.toString(entity);
		response.close();
		return sResponse;
	}

	// Redirect to the start URL
	private LoginDTO redirect(CloseableHttpClient client, CloseableHttpResponse response, HttpEntity entity,
			URI address, String sResponse, LoginDTO loginDTO, String startUrl)
			throws URISyntaxException, ClientProtocolException, IOException {
		address = new URI(PROTOCOL + loginDTO.getOvenHost() + START_HTML);

		final BasicCookieStore cookieStore = new BasicCookieStore();
		final BasicClientCookie objCookie = new BasicClientCookie("siemens_ad_session", loginDTO.getCookie());
		objCookie.setDomain(loginDTO.getOvenHost());
		objCookie.setAttribute("domain", "true");
		objCookie.setPath("/");
		cookieStore.addCookie(objCookie);

		HttpGet httpGet = new HttpGet(address);
		client = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();

		response = client.execute(httpGet);

		entity = response.getEntity();
		sResponse = EntityUtils.toString(entity);
		response.close();
		return loginDTO;
	}
}

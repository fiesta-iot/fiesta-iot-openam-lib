/*******************************************************************************
 * Copyright (c) 2018 Jorge Lanza, 
 *                    David Gomez, 
 *                    Luis Sanchez,
 *                    Juan Ramon Santana
 *
 * For the full copyright and license information, please view the LICENSE
 * file that is distributed with this source code.
 *******************************************************************************/
package eu.fiesta_iot.utils.openam.api;

import java.io.IOException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.StatusType;
import javax.ws.rs.core.UriBuilder;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenericApi {

	protected Logger log = LoggerFactory.getLogger(getClass());

	private String openamRootEndpoint;
	private String openamResourceEndpoint;
	private static final String DEFAULT_TOKEN_HEADER = "iplanetDirectoryPro";
	private String openamTokenHeader;

	private String url;
	private String realm;
	private String token;

	public GenericApi(String rootEndpoint, String resourceEndpoint,
	        String tokenHeader) {
		this.openamRootEndpoint = rootEndpoint;
		this.openamResourceEndpoint = resourceEndpoint;
		this.openamTokenHeader = tokenHeader;

		setToken(null);
		setRealm(null);
	}

	public GenericApi(String rootEndpoint, String resourceEndpoint) {
		this(rootEndpoint, resourceEndpoint, DEFAULT_TOKEN_HEADER);
	}

	public String getRootEndpoint() {
		return openamRootEndpoint;
	}

	public String getRealm() {
		return realm;
	}
	
	public final void setRealm(String realm) {
		UriBuilder builder = UriBuilder.fromUri(openamRootEndpoint).path("json"); 
		if (realm != null) {
			builder = builder.path(realm);
		}
		
		this.realm = realm; 
		this.url = builder.path(openamResourceEndpoint).build().toString();
		
		log.debug(url);
	}

	public final void setToken(String token) {
		this.token = token;
	}

	public String getToken() {
		return token;
	}
	
	private Client createClient() {
		SSLContext sslContext = null;
		try {
			sslContext = SSLContext.getInstance("TLS");

			// Set up a TrustManager that trusts everything
			sslContext.init(null, new TrustManager[] { new X509TrustManager() {
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				public void checkClientTrusted(X509Certificate[] certs,
				                               String authType) {
				}

				public void checkServerTrusted(X509Certificate[] certs,
				                               String authType) {
				}
			} }, new SecureRandom());
		} catch (NoSuchAlgorithmException e) {
			log.warn(e.getMessage());
		} catch (KeyManagementException e) {
			log.warn(e.getMessage());
		}

		// Proxy implementation to get remote data
		Client client = new ResteasyClientBuilder()
		        .establishConnectionTimeout(100, TimeUnit.SECONDS)
		        .socketTimeout(2, TimeUnit.SECONDS).sslContext(sslContext)
		        .hostnameVerifier(new HostnameVerifier() {

			        public boolean verify(String s, SSLSession sslSession) {
				        return true;
			        }

		        })
		        // .hostnameVerifier(SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER)
		        // .hostnameVerifier(new NoopHostnameVerifier());
		        .build();

		return client;
	}

	protected WebTarget createWebTarget(String path) {
		Client client = createClient();
		WebTarget bldr = client.target(url);
		
		return (path != null) ? bldr.path(path) : bldr ;
	}

	protected WebTarget createWebTarget() {
		return createWebTarget(null);
	}

	public Response sendRequest(WebTarget wt, String action,
	                            boolean includeTokenHeader) throws IOException {
		return sendRequest(wt, action, null, "{}", includeTokenHeader);
	}

	public Response sendRequest(String action) throws IOException {
		return sendRequest(createWebTarget(), action, null, "{}", true);
	}

	public Response
	        sendRequest(String action,
	                    Hashtable<String, String> queryParams) throws IOException {
		return sendRequest(createWebTarget(), action, queryParams, "{}", true);
	}

	public Response sendRequest(String action,
	                            String jsonObject) throws IOException {
		return sendRequest(createWebTarget(), action, null, jsonObject, true);
	}

	public Response sendRequest(WebTarget wt, String action,
	                            Hashtable<String, String> queryParams,
	                            String jsonObject,
	                            boolean includeTokenHeader) throws IOException {
		if (queryParams != null) {
			for (Map.Entry<String, String> entry : queryParams.entrySet()) {
				wt.queryParam(entry.getKey(), entry.getValue());
			}
		}
		
		log.debug(wt.getUri().toString());

		Invocation.Builder ib = wt.queryParam("_action", action).request()
		        .accept("application/json");

		if (includeTokenHeader) {
			if (this.token == null) {
				// Use setToken to define the token
				throw new NullPointerException("Current authenticated token must be provided.");
			}
			// Add current authenticated token header
			ib = ib.header(openamTokenHeader, token);
		}

		Response rsp = ib.post(Entity.json(jsonObject));
		if (rsp.getStatus() != Response.Status.OK.getStatusCode()) {
			StatusType status = rsp.getStatusInfo();
			System.out.println(rsp.readEntity(String.class));
			rsp.close();
			throw new IOException("HTTP error " + status.getStatusCode()
			                      + " with message " + status.getReasonPhrase()
			                      + " when executing action.");
		}

		return rsp;
	}
}

/*******************************************************************************
 * Copyright (c) 2018 Jorge Lanza, 
 *                    David Gomez, 
 *                    Luis Sanchez,
 *                    Juan Ramon Santana
 *
 * For the full copyright and license information, please view the LICENSE
 * file that is distributed with this source code.
 *******************************************************************************/
package eu.fiesta_iot.utils.openam;

import java.io.IOException;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.fiesta_iot.utils.openam.api.Authenticate;
import eu.fiesta_iot.utils.openam.api.Sessions;

public class Authentication {

	protected Logger log = LoggerFactory.getLogger(getClass());

	private static final String OPENAM_TOKEN_HEADER = "iplanetDirectoryPro";

	private String username;
	private String password;

	private String token = null;

	private Authenticate authenticate;
	private Sessions sessions;
	
	public Authentication(String openamRoot) {
		authenticate = new Authenticate(openamRoot);
		sessions = new Sessions(openamRoot);
	}
	
	public MultivaluedMap<String, Object> getAuthenticationHeaders() {
		MultivaluedMap<String, Object> header = new MultivaluedHashMap<>();
		header.add(OPENAM_TOKEN_HEADER, token);
		return header;
	}
	
	protected String getRootEndpoint() {
		return authenticate.getRootEndpoint();
	}
	
	public String getCurrentToken() {
		return token;
	}

	protected void setCurrentToken(String token) {
		this.token = token;
		sessions.setToken(token);
	}

	protected String getRealm() {
		return authenticate.getRealm();
	}
	
	public void setRealm(String realm) {
		authenticate.setRealm(realm);
		sessions.setRealm(realm);
	}
	
	public String renew() throws IOException {
		return retrieveSsoToken();
	}

	public boolean isActive() throws IOException {
		checkNullSession();
		return sessions.isActive(token, false);
	}

	public String login(String username, String password) throws IOException {
		if (getCurrentToken() != null) {
			// Previous session active
			logout();
		}
		this.username = username;
		this.password = password;
		return retrieveSsoToken();
	}
	
	public void logout() throws IOException {
		checkNullSession();
		sessions.logout();
		setCurrentToken(null);
	}

	public boolean refresh() throws IOException {
		checkNullSession();
		return sessions.isActive(sessions.getToken(), true);
	}
	
	/**
	 * Obtain a SSO token using the clients credentials - username and
	 * password. Invoke the OpenAM authentication API with a POST call.
	 * 
	 * @throws IOException
	 */
	private String retrieveSsoToken() throws IOException {
		// TODO: Delete previous token?
		String token = null;
		token = authenticate.retrieveToken(username, password);
		log.debug("SSO Token retrieved: " + token);
	
		setCurrentToken(token);
		
		return token;
	}
	
	private void checkNullSession() {
		//if (sessions.getToken() == null) {
		if (getCurrentToken() == null) {
			throw new IllegalStateException("No succesful authentication process finished"); 
		}
	}
}

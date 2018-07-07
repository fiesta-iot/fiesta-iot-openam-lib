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
import java.util.Hashtable;

import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Sessions extends GenericApi {

	private static final String OPENAM_SESSIONS_RESOURCE = "sessions";

	public Sessions(String rootEndpoint) {
		super(rootEndpoint, OPENAM_SESSIONS_RESOURCE);
	}

	public Sessions(String rootEndpoint, String token) {
		this(rootEndpoint);
		setToken(token);
	}
	
	// Current authenticated token in header
	public void logout() throws IOException {
		Response rsp = sendRequest("logout");
		
		// TODO: Check correct answer 
		// {"result":"Successfully logged out"}
	}

	// No token header
	// Token being queried 
	public boolean validate(String token) throws IOException {
		Response rsp = sendRequest(createWebTarget(token), "validate", false);

		String jsonStr = rsp.readEntity(String.class);
		ObjectMapper mapper = new ObjectMapper();
		JsonNode jsonObj = mapper.readTree(jsonStr);

		return jsonObj.get("valid").asBoolean();
	}

	
	// Current authenticated token in header
	// Token being queried
	public boolean isActive(String token, boolean refresh) throws IOException {
		Hashtable<String, String> queryParams = new Hashtable<>(1);
		queryParams.put("tokenId", token);
		if (refresh) {
			queryParams.put("refresh", Boolean.TRUE.toString());
		}
		Response rsp = sendRequest("isActive", queryParams);

		String jsonStr = rsp.readEntity(String.class);
		ObjectMapper mapper = new ObjectMapper();
		JsonNode jsonObj = mapper.readTree(jsonStr);

		return jsonObj.get("active").asBoolean();
	}

	// Current authenticated token in header
	// Token being queried
	public int getTimeLeft(String token) throws IOException {
		return getIntParameter("getTimeLeft", "maxtime", token);
	}

	// Current authenticated token in header
	public int getMaxSessionTime() throws IOException {
		return getIntParameter("getMaxSessionTime", "maxsessiontime");
	}

	// Current authenticated token in header
	public int getMaxIdle() throws IOException {
		return getIntParameter("getMaxIdle", "maxidletime");
	}

	// Current authenticated token in header
	// Token being queried
	public int getIdle(String token) throws IOException {
		return getIntParameter("getIdle", "idletime", token);
	}

	private int getIntParameter(String resource, String json) {
		return getIntParameter(resource, json);
	}

	private int getIntParameter(String resource, String json,
	                            String token) throws IOException {
		Response rsp;
		if (token != null) {
			Hashtable<String, String> queryParams = new Hashtable<>(1);
			queryParams.put("tokenId", token);
			rsp = sendRequest(resource, queryParams);
		} else {
			rsp = sendRequest(resource);
		}

		String jsonStr = rsp.readEntity(String.class);
		ObjectMapper mapper = new ObjectMapper();
		JsonNode jsonObj = mapper.readTree(jsonStr);

		return jsonObj.get(json).asInt();
	}

}

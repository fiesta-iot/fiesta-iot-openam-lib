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

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.StatusType;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Authenticate extends GenericApi {

	private static final String OPENAM_AUTHENTICATION_RESOURCE = "authenticate";

	private static final String OPENAM_USERNAME_HEADER = "X-OpenAM-Username";
	private static final String OPENAM_PASSWORD_HEADER = "X-OpenAM-Password";
	
	public Authenticate(String rootEndpoint) {
		super(rootEndpoint, OPENAM_AUTHENTICATION_RESOURCE);
	}

	public String retrieveToken(String username, String password) throws IOException {
		WebTarget a = createWebTarget();
		Response rsp = a.request().accept("application/json")
		        .header(OPENAM_USERNAME_HEADER, username)
		        .header(OPENAM_PASSWORD_HEADER, password)
		        .post(Entity.json("{}"));

		if (rsp.getStatus() != Response.Status.OK.getStatusCode()) {
			StatusType status = rsp.getStatusInfo();
			rsp.close();
			throw new IOException("HTTP error " + status.getStatusCode()
			                      + " with message " + status.getReasonPhrase()
			                      + " when authenticating.");
		}

		String jsonStr = rsp.readEntity(String.class);
		
		ObjectMapper mapper = new ObjectMapper();
		JsonNode jsonObj = mapper.readTree(jsonStr);

		String token = jsonObj.get("tokenId").asText();
		log.debug("SSO Token retrieved: " + token);

		return token;
	}
}

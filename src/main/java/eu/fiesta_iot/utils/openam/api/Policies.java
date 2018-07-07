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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Policies extends GenericApi {

	private static String OPENAM_POLICIES_RESOURCE = "policies";

	private static String DEFAULT_POLICY_SET = "iPlanetAMWebAgentService";

//	public enum Action {
//	    GET("GET"),
//	    POST("POST"),
//	    PUT("PUT"),
//	    PATCH("PATCH"),
//	    DELETE("DELETE")
//	    ;
//
//	    private final String text;
//
//	    /**
//	     * @param text
//	     */
//	    private Action(final String text) {
//	        this.text = text;
//	    }
//
//	    /* (non-Javadoc)
//	     * @see java.lang.Enum#toString()
//	     */
//	    @Override
//	    public String toString() {
//	        return text;
//	    }
//	}
	
	public Policies(String rootEndpoint) {
		super(rootEndpoint, OPENAM_POLICIES_RESOURCE);
	}

	public Policies(String rootEndpoint, String token) {
		this(rootEndpoint);
		setToken(token);
	}

//	public boolean evaluate(String resource, String userToken, Action action) throws IOException {
//		return evaluate(resource, userToken, action.toString());
//	}
	
	public boolean evaluate(String resource, String userToken, String action) throws IOException {

		return evaluate(resource, userToken, action, DEFAULT_POLICY_SET);
	}

//	public boolean evaluate(String resource, String userToken, Action action, String policySet) throws IOException {
//		return evaluate(resource, userToken, action.toString(), policySet);
//	}
	
	public boolean evaluate(String resource, String userToken, String action,
	                        String policySet) throws IOException {
		
		List<String> resources = new ArrayList<String>(Arrays.asList(resource));
		Map<String, Boolean> allowed = evaluate(resources, userToken, action, policySet);
		return (boolean) allowed.values().toArray()[0];
	}
	
//	public Map<String, Boolean> evaluate(List<String> resources, String userToken, Action action) throws IOException {
//		return evaluate(resources, userToken, action.toString());
//	}
	
	
	public Map<String, Boolean> evaluate(List<String> resources,
	                                     String userToken,
	                                     String action) throws IOException {
		return evaluate(resources, userToken, action, DEFAULT_POLICY_SET);
	}

//	public Map<String, Boolean> evaluate(Collection<String> resources, String userToken, Action action, String policySet) throws IOException {
//		return evaluate(resources, userToken, action.toString(), policySet);
//	}
	
	// Same order is not guarantee
	// (From documentation) It seems that OpenAM returns all the resources even if they are not defined
	public Map<String, Boolean> evaluate(Collection<String> resources,
	                                     String userToken, String action,
	                                     String policySet) throws IOException {
		// Create Json object
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode jsonRoot = mapper.createObjectNode();
		ArrayNode jsonResources = mapper.valueToTree(resources);
		jsonRoot.putArray("resources").addAll(jsonResources);
		jsonRoot.put("application", policySet);
		ObjectNode jsonSubject = jsonRoot.putObject("subject");
		jsonSubject.put("ssoToken", userToken);

		log.debug(jsonRoot.toString());
		
		Response rsp = sendRequest("evaluate", jsonRoot.toString());

		String jsonStr = rsp.readEntity(String.class);
		log.debug(jsonStr);
		
		JsonNode jsonArrayResults = mapper.readTree(jsonStr);
		if (!jsonArrayResults.isArray()) {
			// Error
		}

		String jsonActionName = action.toUpperCase();
		Map<String, Boolean> result =
		        new HashMap<String, Boolean>(resources.size());
		for (final JsonNode objNode : jsonArrayResults) {
			String resource = objNode.get("resource").asText();
			JsonNode jsonActions = objNode.get("actions");
			JsonNode jsonAction = jsonActions.get(jsonActionName);
			// By default is false
			boolean allowed =
			        (jsonAction != null) ? jsonAction.asBoolean() : false;
			        
			result.put(resource, allowed);
		}

		return result;
	}
}

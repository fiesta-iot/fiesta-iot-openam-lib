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
import java.util.Collection;
import java.util.Map;

import eu.fiesta_iot.utils.openam.api.Policies;

public class Authorization {

	private Policies policies;

	public enum Action {
		GET("GET"),
		POST("POST"),
		PUT("PUT"),
		PATCH("PATCH"),
		DELETE("DELETE");

		private final String text;

		/**
		 * @param text
		 */
		private Action(final String text) {
			this.text = text;
		}

		/* (non-Javadoc)
		 * @see java.lang.Enum#toString()
		 */
		@Override
		public String toString() {
			return text;
		}
	}

	Authentication auth;

	public Authorization(Authentication auth) {
		if (auth == null || auth.getCurrentToken() == null) {
			throw new IllegalArgumentException("Expected already authenticated instance");
		}

		this.auth = auth;
		policies = new Policies(auth.getRootEndpoint());
		setRealm(auth.getRealm());
	}

	public void setRealm(String realm) {
		policies.setRealm(realm);
	}

	public Map<String, Boolean>
	        checkAccessRightsFor(Action action, String userToken,
	                             Collection<String> resources,
	                             String policySet) throws IOException {

		if (auth.getCurrentToken() == null) {
			throw new IllegalStateException("No authentication information available");
		}

		updatePoliciesConfiguration();
				
		// Retrieve policy set from the resource list
		return policies.evaluate(resources, userToken, action.toString(),
		                         policySet);
	}

	public boolean checkAccessRightsFor(Action action, String userToken,
	                                    String resource,
	                                    String policySet) throws IOException {
		updatePoliciesConfiguration();

		// Retrieve policy set from the resource list
		return policies.evaluate(resource, userToken, action.toString(),
		                         policySet);
	}

	// Update policies with latest values
	private void updatePoliciesConfiguration() {
		policies.setToken(auth.getCurrentToken());
		// Copy realm from authentication
		// TODO: Check how to set user rights for different realms authentication 
		// policies.setRealm(auth.getRealm());
	}
	
}

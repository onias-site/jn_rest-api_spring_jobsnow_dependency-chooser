package com.jn.rest.api.endpoints;

import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.ccp.decorators.CcpJsonRepresentation;
import com.jn.entities.JnEntityLoginSessionValidation;
import com.jn.rest.open.api.JnOpenApiLogin;
import com.jn.services.JnServiceLogin;

import com.ccp.json.fields.validation.CcpJsonCommonsFields;

/**
 * REST controller for all login operations at path {@code /login/{email}}.
 * Covers user existence check, token creation, login with password, logout,
 * pre-registration, password registration, and token resend/unlock requests.
 */
@CrossOrigin
@RestController
@RequestMapping("/login/{email}")
public class JnRestApiLogin implements JnOpenApiLogin {

	/**
	 * Validate session token. Delegates to the matching service.
	 * @param sessionToken the {@code sessionToken} path variable
	 * @param body the request body
	 */
	@GetMapping("/{sessionToken}")
	public void validateLogin(@PathVariable("sessionToken") String sessionToken, @RequestBody String body) {
		CcpJsonRepresentation ccpJsonRepresentation = new CcpJsonRepresentation(body);
		CcpJsonRepresentation json = ccpJsonRepresentation.put(CcpJsonCommonsFields.sessionToken, sessionToken);
		JnServiceLogin.ValidateLogin.execute(json.content);
	}
	
	/**
	 * Execute login. Delegates to the matching service.
	 * @param body the request body
	 * @return the response body
	 */
	@PostMapping
	public Map<String, Object> executeLogin(@RequestBody Map<String, Object> body) {
		Map<String, Object> execute = JnServiceLogin.ExecuteLogin.execute(body);
		return execute;
	}

	/**
	 * Request login token by email. Delegates to the matching service.
	 * @param body the request body
	 * @return the response body
	 */
	@PostMapping("/token")
	public Map<String, Object> createLoginEmail(@RequestBody Map<String, Object> body) {
		Map<String, Object> execute = JnServiceLogin.CreateLoginEmail.execute(body);
		return execute;
	}

	/**
	 * Request token resend (unlock flow). Delegates to the matching service.
	 * @param body the request body
	 * @return the response body
	 */
	@PostMapping("/token/request/unlocking")
	public Map<String, Object> unlockLoginToken(@RequestBody Map<String, Object> body) {
		Map<String, Object> execute = JnServiceLogin.UnlockLoginToken.execute(body);
		return execute;
	}

	/**
	 * Request token unlock. Delegates to the matching service.
	 * @param body the request body
	 * @return the response body
	 */
	@PostMapping("/token/request/resending")
	public Map<String, Object> resendLoginToken(@RequestBody Map<String, Object> body) {
		Map<String, Object> execute = JnServiceLogin.ResendLoginToken.execute(body);
		return execute;
	}

	/**
	 * Check user registration status. Delegates to the matching service.
	 * @param body the request body
	 */
	@RequestMapping(value = "/token", method = RequestMethod.HEAD)
	public void existsLoginEmail(@RequestBody String body) {
		CcpJsonRepresentation json = new CcpJsonRepresentation(body);
		JnServiceLogin.ExistsLoginEmail.execute(json.content);
	}

	/**
	 * Execute logout. Delegates to the matching service.
	 * @param body the request body
	 * @param sessionToken the {@code sessionToken} path variable
	 */
	@DeleteMapping("/{sessionToken}")
	public void executeLogout(@RequestBody String body, @PathVariable("sessionToken") String sessionToken) {
		CcpJsonRepresentation incompleteSessionValues = new CcpJsonRepresentation(body);
		CcpJsonRepresentation completeSessionValues = incompleteSessionValues.put(JnEntityLoginSessionValidation.Fields.token, sessionToken);
		JnServiceLogin.ExecuteLogout.execute(completeSessionValues.content);
	}

	/**
	 * Save pre-registration answers. Delegates to the matching service.
	 * @param body the request body
	 */
	@PostMapping("/pre-registration")
	public void saveAnswers(@RequestBody Map<String, Object> body) {
		JnServiceLogin.SaveAnswers.execute(body);
	}

	/**
	 * Save password. Delegates to the matching service.
	 * @param body the request body
	 * @return the response body
	 */
	@PostMapping("/password")
	public Map<String, Object> savePassword(@RequestBody Map<String, Object> body) {
		
		Map<String, Object> execute = JnServiceLogin.SavePassword.execute(body);
		return execute;
	}
	
	/**
	 * Request login token by email with language preference. Delegates to the matching service.
	 * @param body the request body
	 * @return the response body
	 */
	@PostMapping("/token/language/{language}")
	public Map<String, Object> createLoginToken(
			@RequestBody Map<String, Object> body
			) {
		
		Map<String, Object> execute = JnServiceLogin.CreateLoginToken.execute(body);
		return execute;
	}

	/**
	 * Always fails: exists only to exercise the exception handler of the API.
	 * @throws JnErrorLoginEndpointOnlyForTesting always
	 */
	@GetMapping("/erro")
	public void onlyError() {
		JnErrorLoginEndpointOnlyForTesting jnErrorLoginEndpointOnlyForTesting = new JnErrorLoginEndpointOnlyForTesting();
		throw jnErrorLoginEndpointOnlyForTesting;
	}

	/** Raised by the {@code /erro} endpoint, which exists only to exercise the exception handler of the API. */
	@SuppressWarnings("serial")
	public static class JnErrorLoginEndpointOnlyForTesting extends RuntimeException {
		/** Builds the error, marked as intentional. */
		private JnErrorLoginEndpointOnlyForTesting() {
			super("Test error");
		}
	}
}

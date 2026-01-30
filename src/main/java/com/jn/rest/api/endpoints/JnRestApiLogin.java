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
import com.ccp.decorators.CcpJsonRepresentation;
import com.jn.entities.JnEntityLoginSessionValidation;
import com.jn.services.JnServiceLogin;

@CrossOrigin
@RestController
@RequestMapping("/login/{email}")
public class JnRestApiLogin{

	@PostMapping
	public Map<String, Object> executeLogin(@RequestBody Map<String, Object> body) {
		
		Map<String, Object> execute = JnServiceLogin.ExecuteLogin.execute(body);
		return execute;
	}

	@PostMapping("/token")
	public Map<String, Object> createLoginEmail(@RequestBody Map<String, Object> body) {
		
		Map<String, Object> execute = JnServiceLogin.CreateLoginEmail.execute(body);
		return execute;
	}

	@RequestMapping(value = "/token", method = RequestMethod.HEAD)
	public void existsLoginEmail(@RequestBody String body) {
		CcpJsonRepresentation json = new CcpJsonRepresentation(body);
		JnServiceLogin.ExistsLoginEmail.execute(json.content);
	}

	@DeleteMapping("/{sessionToken}")
	public void executeLogout(@RequestBody String body, @PathVariable("sessionToken") String sessionToken) {
		CcpJsonRepresentation incompleteSessionValues = new CcpJsonRepresentation(body);
		CcpJsonRepresentation completeSessionValues = incompleteSessionValues.put(JnEntityLoginSessionValidation.Fields.token, sessionToken);
		JnServiceLogin.ExecuteLogout.execute(completeSessionValues.content);
	}

	@PostMapping("/pre-registration")
	public void saveAnswers(@RequestBody Map<String, Object> body) {
		JnServiceLogin.SaveAnswers.execute(body);
	}

	@PostMapping("/password")
	public Map<String, Object> savePassword(@RequestBody Map<String, Object> body) {
		
		Map<String, Object> execute = JnServiceLogin.SavePassword.execute(body);
		return execute;
	}
	
	@PostMapping("/token/language/{language}")
	public Map<String, Object> createLoginToken(
			@RequestBody Map<String, Object> body
			) {
		
		Map<String, Object> execute = JnServiceLogin.CreateLoginToken.execute(body);
		return execute;
	}

	@GetMapping("/erro")
	public void apenasDeErro() {
		throw new RuntimeException("erro de teste");
	}
}

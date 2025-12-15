package com.jn.rest.api.endpoints;

import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.ccp.constantes.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.jn.rest.api.swagger.JnOpenApiAsyncTask;
import com.jn.services.JnServiceAsyncTask;

@CrossOrigin
@RestController
public class JnRestApiAsyncTask implements JnOpenApiAsyncTask{

	@GetMapping("/{asyncTaskId}")
	public Map<String, Object> getAsyncTaskStatusById(@PathVariable("asyncTaskId") String asyncTaskId) {
		CcpJsonRepresentation put = CcpOtherConstants.EMPTY_JSON.put(JnServiceAsyncTask.JsonFieldNames.asyncTaskId, asyncTaskId);
		Map<String, Object> execute = JnServiceAsyncTask.GetAsyncTaskStatusById.execute(put.content);
		return execute;
	}

}

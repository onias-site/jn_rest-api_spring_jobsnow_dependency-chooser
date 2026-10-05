package com.jn.rest.api.endpoints;

import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.jn.rest.open.api.JnOpenApiAsyncTask;
import com.jn.services.JnServiceAsyncTask;

/**
 * REST controller of the asynchronous tasks at {@code /async/task/{asyncTaskId}}; it delegates to
 * {@code JnServiceAsyncTask.GetAsyncTaskStatusById}.
 */
@CrossOrigin
@RestController
public class JnRestApiAsyncTask implements JnOpenApiAsyncTask{

	/**
	 * Get async task status by ID. Delegates to the matching service.
	 * @param asyncTaskId the {@code asyncTaskId} path variable
	 * @return the response body
	 */
	@GetMapping("/{asyncTaskId}")
	public Map<String, Object> getAsyncTaskStatusById(@PathVariable("asyncTaskId") String asyncTaskId) {
		CcpJsonRepresentation put = CcpOtherConstants.EMPTY_JSON.put(JnServiceAsyncTask.JsonFieldNames.asyncTaskId, asyncTaskId);
		Map<String, Object> execute = JnServiceAsyncTask.GetAsyncTaskStatusById.execute(put.content);
		return execute;
	}

}

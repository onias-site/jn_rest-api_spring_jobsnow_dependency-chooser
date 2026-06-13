package com.jn.rest.open.api;

import java.util.Map;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * OpenAPI contract for the async task status endpoint at path {@code /async/task/{asyncTaskId}}.
 * Documents the possible responses: 200 (task found) and 404 (task not found).
 */
@RequestMapping("/async/task")
@Tag(name = "AsyncTask", description = "Asynchronous tasks — operations executed in the background whose result can be polled by ID.")
public interface JnOpenApiAsyncTask {

	@Operation(
		summary = "Get async task status by ID",
		description = "When does it occur? When the frontend needs to check the result of a previously triggered background operation. "
			+ "What does it do? Retrieves the current status and result of the async task identified by the given ID."
			+ "<br/><br/><b>Path variables:</b><ul>"
			+ "<li><b>asyncTaskId</b> – Required. The unique identifier of the async task.</li>"
			+ "</ul>"
	)
	@ApiResponses({
		@ApiResponse(content = {
			@Content(mediaType = "application/json", schema = @Schema(example = "{}")) },
			responseCode = "200",
			description = "Async task found. The response body contains the task result."),
		@ApiResponse(content = {
			@Content(mediaType = "application/json", schema = @Schema(example = "{}")) },
			responseCode = "404",
			description = "Async task not found — the ID does not match any known background operation."),
	})
	Map<String, Object> getAsyncTaskStatusById(@PathVariable("asyncTaskId") String asyncTaskId);
}

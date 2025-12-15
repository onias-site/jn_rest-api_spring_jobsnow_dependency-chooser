package com.jn.rest.api.swagger;

import java.util.Map;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping("/async/task")
@Tag(name = "AsyncTask", description = "Tarefas assíncronas, aquelas que são executadas em segundo plano")
public interface JnOpenApiAsyncTask {

	@Operation(summary = "Obter tarefa status da tarefa assíncrona pelo id")
	@ApiResponses({ @ApiResponse(content = {
			@Content(mediaType = "application/json", schema = @Schema(example = "{}")) }, responseCode = "200", description = "Status: 'Tarefa assíncrona encontrada'"),
			@ApiResponse(content = {
					@Content(mediaType = "application/json", schema = @Schema(example = "{}")) }, responseCode = "404", description = "Status: 'Tarefa assíncrona não encontrada'"
							), })
	Map<String, Object> getAsyncTaskStatusById(@PathVariable("asyncTaskId") String asyncTaskId);
}

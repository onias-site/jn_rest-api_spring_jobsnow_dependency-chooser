package com.jn.rest.api.endpoints;

import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ccp.decorators.CcpJsonRepresentation;
import com.jn.entities.JnEntityAsyncTask;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@CrossOrigin
@RestController
@RequestMapping("/async/task")
@Tag(name = "AsyncTask", description = "Tarefas assíncronas, aquelas que são executadas em segundo plano")
public class JnRestApiAsyncTask {

	@Operation(summary = "Obter tarefa status da tarefa assíncrona pelo id")
	@GetMapping("/{asyncTaskId}")
	@ApiResponses({ @ApiResponse(content = {
			@Content(mediaType = "application/json", schema = @Schema(example = "{}")) }, responseCode = "200", description = "Status: 'Tarefa assíncrona encontrada'"),
			@ApiResponse(content = {
					@Content(mediaType = "application/json", schema = @Schema(example = "{}")) }, responseCode = "404", description = "Status: 'Tarefa assíncrona não encontrada'"
							), })
	public Map<String, Object> getAsyncTaskStatusById(@PathVariable("asyncTaskId") String asyncTaskId) {
		CcpJsonRepresentation execute = JnEntityAsyncTask.ENTITY.getOneById(asyncTaskId);
		return execute.content;
	}

}

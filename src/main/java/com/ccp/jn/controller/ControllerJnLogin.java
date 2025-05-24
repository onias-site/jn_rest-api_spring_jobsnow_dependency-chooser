package com.ccp.jn.controller;

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
import com.ccp.validation.CcpJsonFieldsValidations;
import com.jn.entities.JnEntityLoginSessionValidation;
import com.jn.json.fields.validations.JnJsonFieldsValidationLoginAnswers;
import com.jn.json.fields.validations.JnJsonFieldsValidationPassword;
import com.jn.services.JnServiceLogin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@CrossOrigin
@RestController
@RequestMapping(value = "/login/{email}")
@Tag(name = "Login", description = "Controles de login para cadastro de token, senha, senha fraca, pre registro, alem de controles de bloqueios diversos tais como: token, senha, senha de desbloqueio de token")
public class ControllerJnLogin{

	@Operation(summary = "Executar Login", description = "Quando ocorre? Logo após o usuário digitar sua senha. Para que serve? Serve para o usuário executar login no sistema, gerando um token que será a prova "
			+ " (nas próximas requisições) que o requisitante (frontend), merece ter leitura ou escrita de certos recursos deste bando de dados. "
			+ "O parametro words hash é informado pelo front end (ou nao) por query parameter, se acaso ele for informado e estiver igual ao que o "
			+ "back end tem, o wordsHash não será devolvido na response desse método. Caso este parâmetro não for informado, ou se não for o mesmo que está no back end, então a lista do wordsHash é retornada juntamente com o novo wordsHash e o front deverá salvar no application storage (memória de longa duração do navegador)"
			+ "<br/><br/>Passo anterior: 'Verificação de e-mail'")
	@ApiResponses(value = { @ApiResponse(content = {
			@Content(schema = @Schema(example = "{\"sessionToken\": \"{valorDoToken}\"}")) }, 
	responseCode = "200", description = "Status: 'Usuário logado com sucesso' <br/><br/> Quando ocorre? Quando o usuário digita senha e e-mail corretos e não está com senha ou token bloqueado ou pendência de desbloqueio de token<br/><br/>Qual comportamento esperado do front end? Que ele remova o modal de login e guarde o 'sessionToken' contido no json retornado por este endpoint"),
			@ApiResponse(content = {
					@Content(schema = @Schema(example = "{\"sessionToken\": {valorDoToken}}")) }, 
			responseCode = "201", description = "Status: 'O cadastro de Pre registro está pendente' <br/><br/> Quando ocorre? Quando o usuário deixou de cadastrar dados do pré registro<br/><br/>Qual comportamento esperado do front end? Redirecionamento para a tela de cadastro do pré registro."),
			@ApiResponse(content = {
					@Content(schema = @Schema(example = "{\"sessionToken\": {valorDoToken}}"))
					}, responseCode = "202", description = "Status: 'O cadastro de  senha está pendente' <br/><br/> Quando ocorre? Quando o usuário deixou cadastrar senha<br/><br/>Qual comportamento esperado do front end? Redirecionamento para a tela de cadastro da senha."),
			@ApiResponse(content = {
					@Content(schema = @Schema(example = "")) }, responseCode = "400", description = "Status: 'Email inválido' <br/><br/> Quando ocorre? Quando a url path recebe um conjunto de caracteres que não representa um e-mail válido.<br/><br/>Qual comportamento esperado do front end? Apresentar erro genérico de sistema para o usuário."),
			@ApiResponse(content = {
					@Content(schema = @Schema(example = "")) }, responseCode = "421", description = "Status: 'Senha pendente de desbloqueio' <br/><br/> Quando ocorre? Quando o usuário, anteriormente tentou logar várias vezes com a mesma senha incorreta.<br/><br/>Qual comportamento esperado do front end? Redirecionar o usuário para a tela de alteração de senha."),
			@ApiResponse(content = {
					@Content(schema = @Schema(example = "")) }, responseCode = "403", description = "Status: 'Token bloqueado' <br/><br/> Quando ocorre? Quando o usuário, anteriormente tentou várias vezes alterar sua senha fazendo uso de token incorreto.<br/><br/>Qual comportamento esperado do front end? Redirecionar o usuário para a tela de requisição de desbloqueio de token."),
			@ApiResponse(content = {
					@Content(schema = @Schema(example = "")) }, responseCode = "404", description = "Status: 'Usuário novo no sistema' <br/><br/> Quando ocorre? Quando o e-mail do usuário é desconhecido por este banco de dados. <br/><br/>Qual comportamento esperado do front end? Redirecionar o usuário para a tela de confirmação de e-mail."),
			@ApiResponse(content = {
					@Content(schema = @Schema(example = "")) }, responseCode = "409", description = "Status: 'Usuário já logado' <br/><br/> Quando ocorre? Quando já está registrada uma sessão corrente para o usuário que está tentando fazer login neste sistema. <br/><br/>Qual comportamento esperado do front end? Redirecionar o usuário para a tela de alteração de senha."),
			@ApiResponse(content = {
					@Content(schema = @Schema(example = "")) }, responseCode = "423", description = "Status: 'Senha digitada incorretamente' <br/><br/> Quando ocorre? Quando o usuário, digitou incorretamente a senha, mas ainda não excedeu o máximo de tentativas de senhas incorretas. <br/><br/>Qual comportamento esperado do front end? Exibir mensagem de erro informando o número de tentativas incorretas de digitação de senha."),
			@ApiResponse(content = {
					@Content(schema = @Schema(example = "")) }, responseCode = "429", description = "Status: 'Senha recém bloqueada <br/><br/> Quando ocorre? No exato momento em que o usuário digitou incorretamente a senha, e acaba exceder o máximo de tentativas de senhas incorretas. <br/><br/>Qual comportamento esperado do front end? Redirecionar o usuário à tela de recadastro de senha."), })
	@PostMapping
	public Map<String, Object> executeLogin(@RequestBody Map<String, Object> body) {
		
		CcpJsonFieldsValidations.validate(JnJsonFieldsValidationPassword.class, body, "executeLogin");

		CcpJsonRepresentation json = new CcpJsonRepresentation(body);
		
		CcpJsonRepresentation putAll = json.putAll(body);
		
		CcpJsonRepresentation execute = JnServiceLogin.INSTANCE.executeLogin(putAll);
		return execute.content;
	}

	@Operation(summary = "Criar email para login", description = "Quando ocorre? Logo após ser constatado que é primeiro acesso deste usuário e ele confirmar o e-mail. Para que serve? Serve para o usuário requisitar envio de token para o seu e-mail e ele poder usar esse token para cadastrar senha. "
			+ " (nas próximas requisições) que o requisitante (frontend), merece ter leitura ou escrita de certos recursos deste bando de dados. Passo anterior: 'Verificação de e-mail'.")
	@ApiResponses(value = { @ApiResponse(content = {
			@Content(schema = @Schema(example = "")) }, responseCode = "200", description = "Status: 'Senha já cadastrada, usuário sem pendências"
					+ " de cadastro' <br/><br/> Quando ocorre? Quando o usuário previamente cadastrou todos os dados de pre requisitos "
					+ "(token, senha e pré registro)<br/><br/>"
					+ "Qual comportamento esperado do front end? Redirecionamento para a tela que pede senha para executar login."),
			@ApiResponse(content = {
					@Content(schema = @Schema(example = "")) }, responseCode = "201", description = "Status: 'O cadastro de Pre registro está pendente' <br/><br/> Quando ocorre? Quando o usuário deixou de cadastrar dados do pré registro<br/><br/>Qual comportamento esperado do front end? Redirecionamento para a tela de cadastro do pré registro."),
			@ApiResponse(responseCode = "202", description = "Status: 'Token para cadastro de senha enviado ao e-mail do usuário' <br/><br/> Quando ocorre? Quando o usuário acaba de requisitar com sucesso, o cadastro de senha<br/><br/>Qual comportamento esperado do front end? Redirecionamento para a tela de cadastro da senha."),
			@ApiResponse(content = {
					@Content(schema = @Schema(example = "")) }, responseCode = "400", description = "Status: 'Email inválido' <br/><br/> Quando ocorre? Quando a url path recebe um conjunto de caracteres que não representa um e-mail válido.<br/><br/>Qual comportamento esperado do front end? Apresentar erro genérico de sistema para o usuário."),
			@ApiResponse(content = {
					@Content(schema = @Schema(example = "")) }, responseCode = "421", description = "Status: 'Senha pendente de desbloqueio' <br/><br/> Quando ocorre? Quando o usuário, anteriormente tentou logar várias vezes com a mesma senha incorreta.<br/><br/>Qual comportamento esperado do front end? Redirecionar o usuário para a tela de alteração de senha."),
			@ApiResponse(content = {
					@Content(schema = @Schema(example = "")) }, responseCode = "403", description = "Status: 'Token bloqueado' <br/><br/> Quando ocorre? Quando o usuário, anteriormente tentou várias vezes alterar sua senha fazendo uso de token incorreto.<br/><br/>Qual comportamento esperado do front end? Redirecionar o usuário para a tela de requisição de desbloqueio de token."),
			@ApiResponse(content = {
					@Content(schema = @Schema(example = "")) }, responseCode = "409", description = "Status: 'Usuário já logado' <br/><br/> Quando ocorre? Quando já está registrada uma sessão corrente para o usuário que está tentando fazer login neste sistema. <br/><br/>Qual comportamento esperado do front end? Redirecionar o usuário para a tela de alteração de senha."),
			})
	@PostMapping("/token")
	public Map<String, Object> createLoginEmail(@RequestBody Map<String, Object> body) {
		
		CcpJsonRepresentation json = new CcpJsonRepresentation(body);

		CcpJsonRepresentation createLoginToken = JnServiceLogin.INSTANCE.createLoginEmail(json);
		return createLoginToken.content;
	}

	@Operation(summary = "Verificação de existência deste usuário", description = "Quando ocorre? Logo após o usuário se interessar em ter acesso a informações deste sistema que ele só pode ter se estiver devidamente identificado (logado) nele. Para que serve? Serve para verificar se o usuário existe no sistema, caso ele existir, verificar se há pendências cadastrais (senha, pré registro) para ele resolver e se não existir, fazê-lo preencher todos os dados que o sistema precisa.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Status: 'Senha já cadastrada, usuário sem pendências de cadastro' <br/><br/> Quando ocorre? Quando o usuário previamente cadastrou todos os dados de pre requisitos (token, senha e pré registro)<br/><br/>Qual comportamento esperado do front end? Redirecionamento para a tela que pede senha para executar login."),
			@ApiResponse(responseCode = "201", description = "Status: 'O cadastro de Pre registro está pendente' <br/><br/> Quando ocorre? Quando o usuário deixou de cadastrar dados do pré registro<br/><br/>Qual comportamento esperado do front end? Redirecionamento para a tela de cadastro do pré registro."),
			@ApiResponse(responseCode = "202", description = "Status: 'O cadastro de  senha está pendente' <br/><br/> Quando ocorre? Quando o usuário deixou cadastrar senha<br/><br/>Qual comportamento esperado do front end? Redirecionamento para a tela de cadastro da senha."),
			@ApiResponse(responseCode = "400", description = "Status: 'Email inválido' <br/><br/> Quando ocorre? Quando a url path recebe um conjunto de caracteres que não representa um e-mail válido.<br/><br/>Qual comportamento esperado do front end? Apresentar erro genérico de sistema para o usuário."),
			@ApiResponse(responseCode = "421", description = "Status: 'Senha pendente de desbloqueio' <br/><br/> Quando ocorre? Quando o usuário, anteriormente tentou logar várias vezes com a mesma senha incorreta.<br/><br/>Qual comportamento esperado do front end? Redirecionar o usuário para a tela de alteração de senha."),
			@ApiResponse(responseCode = "403", description = "Status: 'Token bloqueado' <br/><br/> Quando ocorre? Quando o usuário, anteriormente tentou várias vezes alterar sua senha fazendo uso de token incorreto.<br/><br/>Qual comportamento esperado do front end? Redirecionar o usuário para a tela de requisição de desbloqueio de token."),
			@ApiResponse(responseCode = "404", description = "Status: 'Usuário novo no sistema' <br/><br/> Quando ocorre? Quando o e-mail do usuário é desconhecido por este banco de dados. <br/><br/>Qual comportamento esperado do front end? Redirecionar o usuário para a tela de confirmação de e-mail."),
			@ApiResponse(responseCode = "409", description = "Status: 'Usuário já logado' <br/><br/> Quando ocorre? Quando já está registrada uma sessão corrente para o usuário que está tentando fazer login neste sistema. <br/><br/>Qual comportamento esperado do front end? Redirecionar o usuário para a tela de alteração de senha."),
			})
	@RequestMapping(value = "/token", method = RequestMethod.HEAD)
	public void existsLoginEmail(@RequestBody String body) {
		CcpJsonRepresentation json = new CcpJsonRepresentation(body);
		JnServiceLogin.INSTANCE.existsLoginEmail(json);
	}

	@Operation(summary = "Executar logout no sistema", description = "Quando ocorre? Quando por qualquer razão, o usuário quis não mais ter acesso a informações onde ele precisava estar devidamente identificado (logado) neste sistema. Para que serve? Serve para o usuário previamente se desassociar das próximas ações que serão feitas por este front end.")
	@ApiResponses(value = { @ApiResponse(content = {
			@Content(schema = @Schema(example = "")) }, responseCode = "200", description = "Status: 'Usuário executou logout com sucesso' <br/><br/> Quando ocorre? Quando o usuário de fato estavacom sessão ativa (logado) neste sistema<br/><br/>Qual comportamento esperado do front end? Encerramento do modal de login."),
			@ApiResponse(content = {
					@Content(schema = @Schema(example = "")) }, responseCode = "400", description = "Status: 'Email inválido' <br/><br/> Quando ocorre? Quando a url path recebe um conjunto de caracteres que não representa um e-mail válido.<br/><br/>Qual comportamento esperado do front end? Apresentar erro genérico de sistema para o usuário."),
			@ApiResponse(content = {
					@Content(schema = @Schema(example = "")) }, responseCode = "404", description = "Status: 'Usuário não logado no sistema' <br/><br/> Quando ocorre? Quando o o usuário não está com sessão ativa neste sistema. <br/><br/>Qual comportamento esperado do front end? Encerramento do modal de login."), })
	@DeleteMapping("/{sessionToken}")
	public void executeLogout(@RequestBody String body, @PathVariable("sessionToken") String sessionToken) {
		CcpJsonRepresentation incompleteSessionValues = new CcpJsonRepresentation(body);
		CcpJsonRepresentation completeSessionValues = incompleteSessionValues.put(JnEntityLoginSessionValidation.Fields.token.name(), sessionToken);
		JnServiceLogin.INSTANCE.executeLogout(completeSessionValues);
	}

	@Operation(summary = "Salvar pré registro", description = "Quando ocorre? Logo após o usuário tentar executar login e o sistema constatar ausência de dados de pré registro. Para que serve? Serve para o usuário cadadtrar dados de pré registro.")
	@ApiResponses(value = { @ApiResponse(content = {
			@Content(schema = @Schema(example = "")) }, responseCode = "200", description = "Status: 'Usuário sem pendências de cadastro' <br/><br/> Quando ocorre? Quando o usuário previamente cadastrou todos os dados de pre requisitos (token, senha e pré registro)<br/><br/>Qual comportamento esperado do front end? Redirecionamento para a tela que pede senha para executar login."),
			@ApiResponse(content = {
					@Content(schema = @Schema(example = "{'sessionToken': 'tokengeradoaleatoriamente'}")) }, responseCode = "202", description = "Status: 'O cadastro de  senha está pendente' <br/><br/> Quando ocorre? Quando o usuário deixou cadastrar senha<br/><br/>Qual comportamento esperado do front end? Redirecionamento para a tela de cadastro da senha."),
			@ApiResponse(content = {
					@Content(schema = @Schema(example = "")) }, responseCode = "400", description = "Status: 'Email inválido' <br/><br/> Quando ocorre? Quando a url path recebe um conjunto de caracteres que não representa um e-mail válido.<br/><br/>Qual comportamento esperado do front end? Apresentar erro genérico de sistema para o usuário."),
			@ApiResponse(content = {
					@Content(schema = @Schema(example = "")) }, responseCode = "421", description = "Status: 'Senha pendente de desbloqueio' <br/><br/> Quando ocorre? Quando o usuário, anteriormente tentou logar várias vezes com a mesma senha incorreta.<br/><br/>Qual comportamento esperado do front end? Redirecionar o usuário para a tela de alteração de senha."),
			@ApiResponse(content = {
					@Content(schema = @Schema(example = "")) }, responseCode = "403", description = "Status: 'Token bloqueado' <br/><br/> Quando ocorre? Quando o usuário, anteriormente tentou várias vezes alterar sua senha fazendo uso de token incorreto.<br/><br/>Qual comportamento esperado do front end? Redirecionar o usuário para a tela de requisição de desbloqueio de token."),
			@ApiResponse(content = {
					@Content(schema = @Schema(example = "")) }, responseCode = "404", description = "Status: 'Usuário novo no sistema' <br/><br/> Quando ocorre? Quando o e-mail do usuário é desconhecido por este banco de dados. <br/><br/>Qual comportamento esperado do front end? Redirecionar o usuário para a tela de confirmação de e-mail."),
			@ApiResponse(content = {
					@Content(schema = @Schema(example = "")) }, responseCode = "409", description = "Status: 'Usuário já logado' <br/><br/> Quando ocorre? Quando já está registrada uma sessão corrente para o usuário que está tentando fazer login neste sistema. <br/><br/>Qual comportamento esperado do front end? Redirecionar o usuário para a tela de alteração de senha."),
			})
	@PostMapping("/pre-registration")
	public void saveAnswers(@RequestBody Map<String, Object> body) {
		CcpJsonFieldsValidations.validate(JnJsonFieldsValidationLoginAnswers.class, body, "savePreRegistration");
		CcpJsonRepresentation json = new CcpJsonRepresentation(body);
		JnServiceLogin.INSTANCE.saveAnswers(json);
	}

	@Operation(summary = "Salvamento de senha", description = "Quando ocorre? Logo após o sistema constatar que o usuário está com senha bloqueada ou faltando, login já em uso ou se o usuário quer alterar senha. Para que serve? Serve para o usuário cadastrar senha de acesso no sistema. O parametro words hash é informado pelo front end (ou nao) por query parameter, se acaso ele for informado e estiver igual ao que o back end tem, o wordsHash não será devolvido na response desse método. Caso este parâmetro não for informado, ou se não for o mesmo que está no back end, então a lista do wordsHash é retornada juntamente com o novo wordsHash e o front deverá salvar no application storage (memória de longa duração do navegador)")
	@ApiResponses(value = { @ApiResponse(content = {
			@Content(schema = @Schema(example = "{\r\n"
					+ "    \"sessionToken\": \"{valorDoToken}\",\r\n"
					+ "    \"wordsHash\": \"{wordsHash}\",\r\n"
					+ "    \"words\": [\r\n"
					+ "      {\r\n"
					+ "        \"word\": \"java\",\r\n"
					+ "        \"type\": \"IT\"\r\n"
					+ "      }\r\n"
					+ "    ]\r\n"
					+ "  }")) }, responseCode = "200", description = "Status: 'Usuário sem pendências de cadastro' <br/><br/> Quando ocorre? Quando o usuário previamente cadastrou todos os dados de pre requisitos (token, senha e pré registro)<br/><br/>Qual comportamento esperado do front end? Redirecionamento para a tela que pede senha para executar login."),
			@ApiResponse(content = {
					@Content(schema = @Schema(example = "{\r\n"
							+ "    \"sessionToken\": \"{valorDoToken}\",\r\n"
							+ "    \"wordsHash\": \"{wordsHash}\",\r\n"
							+ "    \"words\": [\r\n"
							+ "      {\r\n"
							+ "        \"word\": \"java\",\r\n"
							+ "        \"type\": \"IT\"\r\n"
							+ "      }\r\n"
							+ "    ]\r\n"
							+ "  }")) }, responseCode = "201", description = "Status: 'O cadastro de Pre registro está pendente' <br/><br/> Quando ocorre? Quando o usuário deixou de cadastrar dados do pré registro<br/><br/>Qual comportamento esperado do front end? Redirecionamento para a tela de cadastro do pré registro."),
			@ApiResponse(content = {
					@Content(schema = @Schema(example = "")) }, responseCode = "400", description = "Status: 'Email inválido' <br/><br/> Quando ocorre? Quando a url path recebe um conjunto de caracteres que não representa um e-mail válido.<br/><br/>Qual comportamento esperado do front end? Apresentar erro genérico de sistema para o usuário."),
			@ApiResponse(content = {
					@Content(schema = @Schema(example = "")) }, responseCode = "421", description = "Status: 'Token digitado incorretamente' <br/><br/> Quando ocorre? Quando o usuário, digitou incorretamente o token, mas ainda não excedeu o máximo de tentativas de senhas incorretas. <br/><br/>Qual comportamento esperado do front end? Exibir mensagem de erro informando o número de tentativas incorretas de digitação de token."),
			@ApiResponse(content = {
					@Content(schema = @Schema(example = "")) }, responseCode = "403", description = "Status: 'Token bloqueado' <br/><br/> Quando ocorre? Quando o usuário, anteriormente tentou várias vezes alterar sua senha fazendo uso de token incorreto.<br/><br/>Qual comportamento esperado do front end? Redirecionar o usuário para a tela de requisição de desbloqueio de token."),
			@ApiResponse(content = {
					@Content(schema = @Schema(example = "")) }, responseCode = "404", description = "Status: 'Usuário novo no sistema' <br/><br/> Quando ocorre? Quando o e-mail do usuário é desconhecido por este banco de dados. <br/><br/>Qual comportamento esperado do front end? Redirecionar o usuário para a tela de confirmação de e-mail."),
			@ApiResponse(content = {
					@Content(schema = @Schema(example = "")) }, responseCode = "423", description = "Status: 'A senha não cumpre requisitos para ser uma senha forte' <br/><br/> Quando ocorre? Quando a combinação de caracteres digitadas pelo usuário, não cumpre os requisitos para ser considerada uma senha forte. <br/><br/>Qual comportamento esperado do front end? Redirecionar o usuário para tela de confirmação de senha fraca."), })
	
	@PostMapping("/password")
	public Map<String, Object> savePassword(@RequestBody Map<String, Object> body) {
		
		CcpJsonRepresentation json = new CcpJsonRepresentation(body);
		CcpJsonRepresentation execute = JnServiceLogin.INSTANCE.savePassword(json);
		return execute.content;
	}
	@Operation(summary = "Criar email para login", description = "Quando ocorre? Logo após ser constatado que é primeiro acesso deste usuário e ele confirmar o e-mail. Para que serve? Serve para o usuário requisitar envio de token para o seu e-mail e ele poder usar esse token para cadastrar senha. "
			+ " (nas próximas requisições) que o requisitante (frontend), merece ter leitura ou escrita de certos recursos deste bando de dados. Passo anterior: 'Verificação de e-mail'.")
	@ApiResponses(value = { @ApiResponse(content = {
			@Content(schema = @Schema(example = "")) }, responseCode = "200", description = "Status: 'Senha já cadastrada, usuário sem pendências"
					+ " de cadastro' <br/><br/> Quando ocorre? Quando o usuário previamente cadastrou todos os dados de pre requisitos "
					+ "(token, senha e pré registro)<br/><br/>"
					+ "Qual comportamento esperado do front end? Redirecionamento para a tela que pede senha para executar login."),
			@ApiResponse(content = {
					@Content(schema = @Schema(example = "")) }, responseCode = "201", description = "Status: 'O cadastro de Pre registro está pendente' <br/><br/> Quando ocorre? Quando o usuário deixou de cadastrar dados do pré registro<br/><br/>Qual comportamento esperado do front end? Redirecionamento para a tela de cadastro do pré registro."),
			@ApiResponse(content = {
					@Content(schema = @Schema(example = 
							" {\"asyncTaskId\": \"-484333a30ec794b6c5490290cfda0486e7c31c89\"}"
							)) }, responseCode = "202", description = "Status: 'Token para cadastro de senha enviado ao e-mail do usuário' <br/><br/> Quando ocorre? Quando o usuário acaba de requisitar com sucesso, o cadastro de senha<br/><br/>Qual comportamento esperado do front end? Redirecionamento para a tela de cadastro da senha."),
			@ApiResponse(content = {
					@Content(schema = @Schema(example = "")) }, responseCode = "400", description = "Status: 'Email inválido' <br/><br/> Quando ocorre? Quando a url path recebe um conjunto de caracteres que não representa um e-mail válido.<br/><br/>Qual comportamento esperado do front end? Apresentar erro genérico de sistema para o usuário."),
			@ApiResponse(content = {
					@Content(schema = @Schema(example = "")) }, responseCode = "421", description = "Status: 'Senha pendente de desbloqueio' <br/><br/> Quando ocorre? Quando o usuário, anteriormente tentou logar várias vezes com a mesma senha incorreta.<br/><br/>Qual comportamento esperado do front end? Redirecionar o usuário para a tela de alteração de senha."),
			@ApiResponse(content = {
					@Content(schema = @Schema(example = "")) }, responseCode = "403", description = "Status: 'Token bloqueado' <br/><br/> Quando ocorre? Quando o usuário, anteriormente tentou várias vezes alterar sua senha fazendo uso de token incorreto.<br/><br/>Qual comportamento esperado do front end? Redirecionar o usuário para a tela de requisição de desbloqueio de token."),
			@ApiResponse(content = {
					@Content(schema = @Schema(example = "")) }, responseCode = "409", description = "Status: 'Usuário já logado' <br/><br/> Quando ocorre? Quando já está registrada uma sessão corrente para o usuário que está tentando fazer login neste sistema. <br/><br/>Qual comportamento esperado do front end? Redirecionar o usuário para a tela de alteração de senha."),
			})
	@PostMapping("/token/language/{language}")
	public Map<String, Object> createLoginToken(
			@RequestBody Map<String, Object> body
			) {
		
		CcpJsonRepresentation json = new CcpJsonRepresentation(body);
		
		CcpJsonRepresentation createLoginToken = JnServiceLogin.INSTANCE.createLoginToken(json);
		
		return createLoginToken.content;
	}

	@GetMapping("/erro")
	public void apenasDeErro() {
		throw new RuntimeException("erro de teste");
	}
}

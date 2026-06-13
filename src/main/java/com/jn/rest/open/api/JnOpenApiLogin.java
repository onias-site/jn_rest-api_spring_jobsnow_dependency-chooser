package com.jn.rest.open.api;

import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Full OpenAPI contract for all login endpoints at path {@code /login/{email}}.
 * Documents all possible HTTP response codes and the expected frontend behavior for each case.
 * All endpoints share the path variable <b>email</b>: required, valid email address format
 * (^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$), min 7 / max 100 characters.
 */
@RequestMapping("/login/{email}")
@Tag(name = "Login", description = "Login controls for token and password registration, pre-registration, "
		+ "and various lock management flows such as: token lock, password lock, and token unlock requests.")
public interface JnOpenApiLogin {

	@Operation(
		summary = "Validate session token",
		description = "When does it occur? When the frontend needs to confirm that an existing session is still active. "
			+ "What does it do? Verifies the combination of email (path) + session token against the active session registry."
			+ "<br/><br/><b>Path variables:</b><ul>"
			+ "<li><b>email</b> – Required. Valid email address. "
			+ "Format: ^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$. Min 7, max 100 characters.</li>"
			+ "<li><b>sessionToken</b> – Required. Must be exactly 8 characters.</li>"
			+ "</ul>"
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Session is valid."),
		@ApiResponse(responseCode = "400", description = "Invalid email format. The frontend should display a generic system error."),
		@ApiResponse(responseCode = "401", description = "Session token is invalid or expired."),
	})
	@GetMapping("/{sessionToken}")
	void validateLogin(@PathVariable("sessionToken") String sessionToken, @RequestBody String body);

	@Operation(
		summary = "Execute login",
		description = "When does it occur? Right after the user types their password. "
			+ "What does it do? Authenticates the user and generates a session token to be used in subsequent authenticated requests. "
			+ "The optional query parameter <b>wordsHash</b> can be sent by the frontend: if it matches the backend value, "
			+ "the word list is not returned. Otherwise, the full word list and its new hash are returned and must be stored "
			+ "in application storage (long-term browser storage)."
			+ "<br/><br/><b>Request body fields (JSON):</b><ul>"
			+ "<li><b>userAgent</b> – Required. Must be a non-empty string.</li>"
			+ "<li><b>ip</b> – Required. String, min 7 / max 15 characters (IPv4 format).</li>"
			+ "<li><b>email</b> – Required. Valid email address. Min 7, max 100 characters.</li>"
			+ "<li><b>password</b> – Required. Strong password: minimum 8 characters, at least 1 uppercase letter, "
			+ "1 lowercase letter, 1 digit, and 1 special character.</li>"
			+ "</ul>"
	)
	@ApiResponses({
		@ApiResponse(content = {
			@Content(schema = @Schema(example = "{\"sessionToken\": \"{tokenValue}\"}")) },
			responseCode = "200",
			description = "User logged in successfully. "
				+ "The frontend should dismiss the login modal and store the 'sessionToken' returned in the JSON."),
		@ApiResponse(content = {
			@Content(schema = @Schema(example = "{\"sessionToken\": \"{tokenValue}\"}")) },
			responseCode = "201",
			description = "Pre-registration data is pending. "
				+ "The frontend should redirect the user to the pre-registration screen."),
		@ApiResponse(content = {
			@Content(schema = @Schema(example = "{\"sessionToken\": \"{tokenValue}\"}")) },
			responseCode = "202",
			description = "Password registration is pending. "
				+ "The frontend should redirect the user to the password registration screen."),
		@ApiResponse(responseCode = "400",
			description = "Invalid email format. The frontend should display a generic system error."),
		@ApiResponse(responseCode = "403",
			description = "Token is locked. The frontend should redirect the user to the token unlock request screen."),
		@ApiResponse(responseCode = "404",
			description = "New user — email not found in the system. "
				+ "The frontend should redirect the user to the email confirmation screen."),
		@ApiResponse(responseCode = "409",
			description = "User already logged in. The frontend should redirect the user to the password change screen."),
		@ApiResponse(responseCode = "423",
			description = "Wrong password entered. Still within the allowed attempt limit. "
				+ "The frontend should display an error message with the remaining attempt count."),
		@ApiResponse(responseCode = "427",
			description = "Password pending unlock. The frontend should redirect the user to the password change screen."),
		@ApiResponse(responseCode = "429",
			description = "Password just locked — maximum wrong attempts exceeded. "
				+ "The frontend should redirect the user to the password re-registration screen."),
	})
	@PostMapping
	Map<String, Object> executeLogin(@RequestBody Map<String, Object> body);

	@Operation(
		summary = "Request login token by email",
		description = "When does it occur? Right after confirming it is the user's first access and they have confirmed their email. "
			+ "What does it do? Sends a token to the user's email so they can use it to register a password."
			+ "<br/><br/><b>Request body fields (JSON):</b><ul>"
			+ "<li><b>userAgent</b> – Required. Must be a non-empty string.</li>"
			+ "<li><b>ip</b> – Required. String, min 7 / max 15 characters (IPv4 format).</li>"
			+ "<li><b>email</b> – Required. Valid email address. Min 7, max 100 characters.</li>"
			+ "</ul>"
	)
	@ApiResponses({
		@ApiResponse(content = {
			@Content(schema = @Schema(example = "")) },
			responseCode = "200",
			description = "Password already registered, user has no pending registrations. "
				+ "The frontend should redirect the user to the password entry screen."),
		@ApiResponse(content = {
			@Content(schema = @Schema(example = "")) },
			responseCode = "201",
			description = "Pre-registration is pending. "
				+ "The frontend should redirect the user to the pre-registration screen."),
		@ApiResponse(content = {
			@Content(schema = @Schema(example = "{\"asyncTaskId\": \"-484333a30ec794b6c5490290cfda0486e7c31c89\"}")) },
			responseCode = "202",
			description = "Token for password registration sent to user's email. "
				+ "The frontend should redirect the user to the password registration screen."),
		@ApiResponse(content = {
			@Content(schema = @Schema(example = "")) },
			responseCode = "400",
			description = "Invalid email format. The frontend should display a generic system error."),
		@ApiResponse(content = {
			@Content(schema = @Schema(example = "")) },
			responseCode = "403",
			description = "Token is locked. The frontend should redirect the user to the token unlock request screen."),
		@ApiResponse(content = {
			@Content(schema = @Schema(example = "")) },
			responseCode = "409",
			description = "User already logged in. The frontend should redirect the user to the password change screen."),
		@ApiResponse(content = {
			@Content(schema = @Schema(example = "")) },
			responseCode = "427",
			description = "Password pending unlock. The frontend should redirect the user to the password change screen."),
	})
	@PostMapping("/token")
	Map<String, Object> createLoginEmail(@RequestBody Map<String, Object> body);

	@Operation(
		summary = "Request login token by email with language preference",
		description = "When does it occur? When the user needs a token and wants to receive the email in a specific language. "
			+ "What does it do? Sends a token to the user's email in the requested language so they can register a password."
			+ "<br/><br/><b>Path variables:</b><ul>"
			+ "<li><b>language</b> – Required. Accepted values: 'portuguese', 'english', 'spanish'.</li>"
			+ "</ul>"
			+ "<b>Request body fields (JSON):</b><ul>"
			+ "<li><b>userAgent</b> – Required. Must be a non-empty string.</li>"
			+ "<li><b>ip</b> – Required. String, min 7 / max 15 characters (IPv4 format).</li>"
			+ "<li><b>email</b> – Required. Valid email address. Min 7, max 100 characters.</li>"
			+ "<li><b>language</b> – Required. Accepted values: 'portuguese', 'english', 'spanish'.</li>"
			+ "</ul>"
	)
	@ApiResponses({
		@ApiResponse(content = {
			@Content(schema = @Schema(example = "")) },
			responseCode = "200",
			description = "Password already registered, user has no pending registrations. "
				+ "The frontend should redirect the user to the password entry screen."),
		@ApiResponse(content = {
			@Content(schema = @Schema(example = "")) },
			responseCode = "201",
			description = "Pre-registration is pending. "
				+ "The frontend should redirect the user to the pre-registration screen."),
		@ApiResponse(content = {
			@Content(schema = @Schema(example = "{\"asyncTaskId\": \"-484333a30ec794b6c5490290cfda0486e7c31c89\"}")) },
			responseCode = "202",
			description = "Token sent to user's email. "
				+ "The frontend should redirect the user to the password registration screen."),
		@ApiResponse(content = {
			@Content(schema = @Schema(example = "")) },
			responseCode = "400",
			description = "Invalid email format. The frontend should display a generic system error."),
		@ApiResponse(content = {
			@Content(schema = @Schema(example = "")) },
			responseCode = "403",
			description = "Token is locked. The frontend should redirect the user to the token unlock request screen."),
		@ApiResponse(content = {
			@Content(schema = @Schema(example = "")) },
			responseCode = "404",
			description = "Email not registered. The frontend should redirect the user to the email confirmation screen."),
	})
	@PostMapping("/token/language/{language}")
	Map<String, Object> createLoginToken(@RequestBody Map<String, Object> body);

	@Operation(
		summary = "Request token resend (unlock flow)",
		description = "When does it occur? When the user's token is locked due to too many wrong attempts and they want a new token. "
			+ "What does it do? Registers a resend request for the locked token so a support bot can process it."
			+ "<br/><br/><b>Request body fields (JSON):</b><ul>"
			+ "<li><b>userAgent</b> – Required. Must be a non-empty string.</li>"
			+ "<li><b>ip</b> – Required. String, min 7 / max 15 characters (IPv4 format).</li>"
			+ "<li><b>email</b> – Required. Valid email address. Min 7, max 100 characters.</li>"
			+ "</ul>"
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Resend request registered successfully."),
		@ApiResponse(responseCode = "404",
			description = "Token is not locked — resend is not applicable."),
		@ApiResponse(responseCode = "409",
			description = "Resend or unlock request already submitted."),
		@ApiResponse(responseCode = "429",
			description = "Token has already been resent or unlocked."),
	})
	@PostMapping("/token/request/resending")
	Map<String, Object> unlockLoginToken(@RequestBody Map<String, Object> body);

	@Operation(
		summary = "Request token unlock",
		description = "When does it occur? When the user needs to unlock a previously locked token. "
			+ "What does it do? Registers an unlock request for the locked token so a support bot can process it."
			+ "<br/><br/><b>Request body fields (JSON):</b><ul>"
			+ "<li><b>userAgent</b> – Required. Must be a non-empty string.</li>"
			+ "<li><b>ip</b> – Required. String, min 7 / max 15 characters (IPv4 format).</li>"
			+ "<li><b>email</b> – Required. Valid email address. Min 7, max 100 characters.</li>"
			+ "</ul>"
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Unlock request registered successfully."),
		@ApiResponse(responseCode = "404",
			description = "Token is not locked — unlock is not applicable."),
		@ApiResponse(responseCode = "409",
			description = "Unlock request already submitted."),
		@ApiResponse(responseCode = "429",
			description = "Token already unlocked or already resent."),
	})
	@PostMapping("/token/request/unlocking")
	Map<String, Object> resendLoginToken(@RequestBody Map<String, Object> body);

	@Operation(
		summary = "Check user registration status",
		description = "When does it occur? Right after the user shows interest in accessing authenticated resources. "
			+ "What does it do? Checks whether the user exists and whether there are any pending registration steps "
			+ "(password, pre-registration)."
			+ "<br/><br/><b>Request body fields (JSON):</b><ul>"
			+ "<li><b>userAgent</b> – Required. Must be a non-empty string.</li>"
			+ "<li><b>ip</b> – Required. String, min 7 / max 15 characters (IPv4 format).</li>"
			+ "<li><b>email</b> – Required. Valid email address. Min 7, max 100 characters.</li>"
			+ "</ul>"
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200",
			description = "Password registered, no pending registrations. "
				+ "The frontend should redirect the user to the password entry screen."),
		@ApiResponse(responseCode = "201",
			description = "Pre-registration is pending. "
				+ "The frontend should redirect the user to the pre-registration screen."),
		@ApiResponse(responseCode = "202",
			description = "Password registration is pending. "
				+ "The frontend should redirect the user to the password registration screen."),
		@ApiResponse(responseCode = "400",
			description = "Invalid email format. The frontend should display a generic system error."),
		@ApiResponse(responseCode = "403",
			description = "Token is locked. The frontend should redirect the user to the token unlock request screen."),
		@ApiResponse(responseCode = "404",
			description = "New user — email not found. The frontend should redirect the user to the email confirmation screen."),
		@ApiResponse(responseCode = "409",
			description = "User already logged in. The frontend should redirect the user to the password change screen."),
		@ApiResponse(responseCode = "427",
			description = "Password pending unlock. The frontend should redirect the user to the password change screen."),
	})
	@RequestMapping(value = "/token", method = RequestMethod.HEAD)
	void existsLoginEmail(@RequestBody String body);

	@Operation(
		summary = "Execute logout",
		description = "When does it occur? When the user intentionally ends their session or the frontend needs to disassociate from it. "
			+ "What does it do? Removes the active session so subsequent authenticated requests are rejected."
			+ "<br/><br/><b>Path variables:</b><ul>"
			+ "<li><b>sessionToken</b> – Required. Must be exactly 8 characters.</li>"
			+ "</ul>"
			+ "<b>Request body fields (JSON):</b><ul>"
			+ "<li><b>userAgent</b> – Required. Must be a non-empty string.</li>"
			+ "<li><b>ip</b> – Required. String, min 7 / max 15 characters (IPv4 format).</li>"
			+ "<li><b>email</b> – Required. Valid email address. Min 7, max 100 characters.</li>"
			+ "<li><b>token</b> – Required. The session token to invalidate.</li>"
			+ "</ul>"
	)
	@ApiResponses({
		@ApiResponse(content = {
			@Content(schema = @Schema(example = "")) },
			responseCode = "200",
			description = "Logout executed successfully. The frontend should close the login modal."),
		@ApiResponse(content = {
			@Content(schema = @Schema(example = "")) },
			responseCode = "400",
			description = "Invalid email format. The frontend should display a generic system error."),
		@ApiResponse(content = {
			@Content(schema = @Schema(example = "")) },
			responseCode = "404",
			description = "User is not logged in. The frontend should close the login modal."),
	})
	@DeleteMapping("/{sessionToken}")
	void executeLogout(@RequestBody String body, @PathVariable("sessionToken") String sessionToken);

	@Operation(
		summary = "Save pre-registration answers",
		description = "When does it occur? Right after the system detects missing pre-registration data during the login flow. "
			+ "What does it do? Records the user's onboarding questionnaire answers."
			+ "<br/><br/><b>Request body fields (JSON):</b><ul>"
			+ "<li><b>userAgent</b> – Required. Must be a non-empty string.</li>"
			+ "<li><b>ip</b> – Required. String, min 7 / max 15 characters (IPv4 format).</li>"
			+ "<li><b>email</b> – Required. Valid email address. Min 7, max 100 characters.</li>"
			+ "<li><b>channel</b> – Required. Accepted values: 'linkedin', 'telegram', 'friends', 'others'.</li>"
			+ "<li><b>goal</b> – Required. Accepted values: 'jobs', 'recruiting'.</li>"
			+ "</ul>"
	)
	@ApiResponses({
		@ApiResponse(content = {
			@Content(schema = @Schema(example = "")) },
			responseCode = "200",
			description = "Answers saved, user has no pending registrations. "
				+ "The frontend should redirect the user to the password entry screen."),
		@ApiResponse(content = {
			@Content(schema = @Schema(example = "{\"sessionToken\": \"tokenValue\"}")) },
			responseCode = "202",
			description = "Password registration is pending. "
				+ "The frontend should redirect the user to the password registration screen."),
		@ApiResponse(content = {
			@Content(schema = @Schema(example = "")) },
			responseCode = "400",
			description = "Invalid email format. The frontend should display a generic system error."),
		@ApiResponse(content = {
			@Content(schema = @Schema(example = "")) },
			responseCode = "403",
			description = "Token is locked. The frontend should redirect the user to the token unlock request screen."),
		@ApiResponse(content = {
			@Content(schema = @Schema(example = "")) },
			responseCode = "404",
			description = "Email not found. The frontend should redirect the user to the email confirmation screen."),
		@ApiResponse(content = {
			@Content(schema = @Schema(example = "")) },
			responseCode = "409",
			description = "User already logged in. The frontend should redirect the user to the password change screen."),
		@ApiResponse(content = {
			@Content(schema = @Schema(example = "")) },
			responseCode = "427",
			description = "Password pending unlock. The frontend should redirect the user to the password change screen."),
	})
	@PostMapping("/pre-registration")
	void saveAnswers(@RequestBody Map<String, Object> body);

	@Operation(
		summary = "Save password",
		description = "When does it occur? After the system detects a locked or missing password, "
			+ "or when the user wants to change their password. "
			+ "What does it do? Registers the user's access password using a valid one-time token sent by email. "
			+ "The optional query parameter <b>wordsHash</b> can be sent to avoid re-downloading the full word list; "
			+ "if the hash matches, the word list is not returned."
			+ "<br/><br/><b>Request body fields (JSON):</b><ul>"
			+ "<li><b>userAgent</b> – Required. Must be a non-empty string.</li>"
			+ "<li><b>ip</b> – Required. String, min 7 / max 15 characters (IPv4 format).</li>"
			+ "<li><b>email</b> – Required. Valid email address. Min 7, max 100 characters.</li>"
			+ "<li><b>password</b> – Required. Strong password: minimum 8 characters, at least 1 uppercase letter, "
			+ "1 lowercase letter, 1 digit, and 1 special character.</li>"
			+ "<li><b>token</b> – Optional. The one-time token sent by email. "
			+ "Must be a non-empty string if provided.</li>"
			+ "</ul>"
	)
	@ApiResponses({
		@ApiResponse(content = {
			@Content(schema = @Schema(example = "{\r\n"
				+ "    \"sessionToken\": \"{tokenValue}\",\r\n"
				+ "    \"wordsHash\": \"{wordsHash}\",\r\n"
				+ "    \"words\": [{\"word\": \"java\", \"type\": \"IT\"}]\r\n"
				+ "  }")) },
			responseCode = "200",
			description = "Password saved, user has no pending registrations. "
				+ "The frontend should redirect the user to the password entry screen."),
		@ApiResponse(content = {
			@Content(schema = @Schema(example = "{\r\n"
				+ "    \"sessionToken\": \"{tokenValue}\",\r\n"
				+ "    \"wordsHash\": \"{wordsHash}\",\r\n"
				+ "    \"words\": [{\"word\": \"java\", \"type\": \"IT\"}]\r\n"
				+ "  }")) },
			responseCode = "201",
			description = "Pre-registration is pending. "
				+ "The frontend should redirect the user to the pre-registration screen."),
		@ApiResponse(content = {
			@Content(schema = @Schema(example = "")) },
			responseCode = "400",
			description = "Invalid email format. The frontend should display a generic system error."),
		@ApiResponse(content = {
			@Content(schema = @Schema(example = "")) },
			responseCode = "403",
			description = "Token is locked. The frontend should redirect the user to the token unlock request screen."),
		@ApiResponse(content = {
			@Content(schema = @Schema(example = "")) },
			responseCode = "404",
			description = "Email not found. The frontend should redirect the user to the email confirmation screen."),
		@ApiResponse(content = {
			@Content(schema = @Schema(example = "")) },
			responseCode = "423",
			description = "Weak password: the submitted password does not meet strong-password requirements. "
				+ "The frontend should redirect the user to the weak password confirmation screen."),
		@ApiResponse(content = {
			@Content(schema = @Schema(example = "")) },
			responseCode = "427",
			description = "Wrong token entered. Still within the allowed attempt limit. "
				+ "The frontend should display an error with the remaining attempt count."),
		@ApiResponse(content = {
			@Content(schema = @Schema(example = "")) },
			responseCode = "429",
			description = "Token just locked — maximum wrong attempts exceeded. "
				+ "The frontend should redirect the user to the token resend screen."),
	})
	@PostMapping("/password")
	Map<String, Object> savePassword(@RequestBody Map<String, Object> body);
}

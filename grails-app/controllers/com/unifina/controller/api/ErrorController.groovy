package com.unifina.controller.api

import com.unifina.api.*
import com.unifina.exceptions.CanvasUnreachableException
import com.unifina.security.AuthLevel
import com.unifina.security.StreamrApi
import grails.converters.JSON
import groovy.transform.CompileStatic

import javax.servlet.http.HttpServletResponse

class ErrorController {

	static final Map<String, Closure<ApiError>> errorMappings = [
		InvalidStateException: { InvalidStateException e -> new ApiError(500, "STATE_NOT_ALLOWED", e.message) },
		ValidationException: validationException(),
		CanvasUnreachableException: { CanvasUnreachableException e -> new ApiError(500, "CANVAS_UNREACHABLE", e.message) },
		CanvasCommunicationException: { CanvasCommunicationException e -> new ApiError(503, "CANVAS_COMMUNICATION_ERROR", e.message)},
		CannotRemoveEthereumKeyException: { CannotRemoveEthereumKeyException e -> new ApiError(409, "ETHEREUM_KEY_REMOVAL_ERROR", e.message)},
		InvalidSessionTokenException: { InvalidSessionTokenException e -> new ApiError(401, "INVALID_SESSION_TOKEN_ERROR", e.message)},
		ChallengeVerificationFailedException: { ChallengeVerificationFailedException e -> new ApiError(401, "CHALLENGE_VERIFICATION_FAILED_ERROR", e.message)},
		DisabledUserException: {DisabledUserException e -> new ApiError(401, "DISABLED_USER_EXCEPTION", e.message)},
		InvalidUsernameAndPasswordException: { InvalidUsernameAndPasswordException e -> new ApiError(401, "INVALID_USERNAME_PASSWORD_ERROR", e.message)},
		InvalidAPIKeyException: { InvalidAPIKeyException e -> new ApiError(401, "INVALID_API_KEY_ERROR", e.message)},
		BadRequestException: { BadRequestException e -> new ApiError(400, "PARAMETER_MISSING", e.message)},
		FieldCannotBeUpdatedException: { FieldCannotBeUpdatedException e -> new ApiError(422, "FIELD_CANNOT_BE_UPDATED", e.message)}
	]

	private static Closure<ApiError> validationException() {
		return { Exception e ->
			if (e instanceof grails.validation.ValidationException) {
				grails.validation.ValidationException ve = (grails.validation.ValidationException) e
				ValidationException v = new ValidationException(ve.errors)
				return new ApiError(422, "VALIDATION_ERROR", v.message)
			}
			ValidationException ve = (ValidationException) e
			return new ApiError(422, "VALIDATION_ERROR", ve.message)
		}
	}

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def index() {
		try {
			Exception exception = request.exception.cause ?: request.exception
			renderAsJson(exception)
		} catch (Exception e) {
			// Avoid infinite loop by catching any "error while showing error" -type of situation
			log.error("Failed to render exception!", e)
			response.status = 500
			render ([code: "ERROR_WHILE_RENDERING_EXCEPTION", message: "An error occurred while rendering exception"] as JSON)
		}
	}

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def notFound() {
		response.sendError(HttpServletResponse.SC_NOT_FOUND)
		return
	}

	@CompileStatic
	def renderAsJson(Exception exception) {
		def mapper = errorMappings[exception.class.simpleName]

		ApiError apiError
		if (mapper) {
			apiError = mapper.call(exception)
		} else if (exception instanceof ApiException) {
			apiError = ((ApiException) exception).asApiError()
		} else {
			apiError = new ApiError(500, exception.class.simpleName, exception.getMessage())
		}

		// Log internal errors
		if (apiError.statusCode >= 500 && apiError.statusCode < 600) {
			log.error("Unexpected error occurred on request to ${request?.getMethod()} ${request?.getRequestURL()}, returning status code 500", exception)
		}

		def extraHeaders = apiError.getHeaders()
		if (extraHeaders != null) {
			extraHeaders.each { key, value ->
				response.setHeader(key, value)
			}
		}

		response.status = apiError.statusCode
		render(apiError.toMap() as JSON)
	}
}

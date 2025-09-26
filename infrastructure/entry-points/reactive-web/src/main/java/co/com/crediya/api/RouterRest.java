package co.com.crediya.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import co.com.crediya.api.dto.CreateCreditApplicationRequestDto;
import co.com.crediya.api.dto.CreateCreditApplicationResponseDto;
import co.com.crediya.api.dto.ErrorResponseDto;
import co.com.crediya.api.dto.UpdateApplicationStatusRequestDto;
import co.com.crediya.api.dto.UpdateApplicationStatusResponseDto;
import co.com.crediya.api.dto.CalculateCapacityRequestDto;
import co.com.crediya.api.dto.CalculateCapacityResponseDto;
import co.com.crediya.usecase.creditapplication.dto.PaginatedCreditApplicationsDto;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterRest {

    @Value("${routes.paths.credit}")
    private String creditApplicationPath;

    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = "/createApplication",
                    method = RequestMethod.POST,
                    beanClass = Handler.class,
                    beanMethod = "createCreditApplication",
                    operation = @Operation(
                            operationId = "createCreditApplication",
                            summary = "Crear solicitud de crédito",
                            description = "Crea una nueva solicitud de crédito validando campos requeridos, existencia del usuario y tipo de préstamo válido",
                            tags = {"Solicitudes de Crédito"},
                            requestBody = @RequestBody(
                                    description = "Datos de la solicitud de crédito",
                                    required = true,
                                    content = @Content(
                                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            schema = @Schema(implementation = CreateCreditApplicationRequestDto.class)
                                    )
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "201", 
                                            description = "Solicitud de crédito creada exitosamente",
                                            content = @Content(
                                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                                    schema = @Schema(implementation = CreateCreditApplicationResponseDto.class)
                                            )
                                    ),
                                    @ApiResponse(
                                            responseCode = "400", 
                                            description = "Error de validación o formato inválido",
                                            content = @Content(
                                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                                    schema = @Schema(implementation = ErrorResponseDto.class)
                                            )
                                    ),
                                    @ApiResponse(
                                            responseCode = "404", 
                                            description = "Usuario no encontrado",
                                            content = @Content(
                                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                                    schema = @Schema(implementation = ErrorResponseDto.class)
                                            )
                                    ),
                                    @ApiResponse(
                                            responseCode = "409", 
                                            description = "Violación de regla de negocio",
                                            content = @Content(
                                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                                    schema = @Schema(implementation = ErrorResponseDto.class)
                                            )
                                    ),
                                    @ApiResponse(
                                            responseCode = "500", 
                                            description = "Error interno del servidor",
                                            content = @Content(
                                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                                    schema = @Schema(implementation = ErrorResponseDto.class)
                                            )
                                    )
                            }
                    )
            ),
            @RouterOperation(
                    path = "/creditApplication",
                    method = RequestMethod.GET,
                    beanClass = Handler.class,
                    beanMethod = "getCreditApplications",
                    operation = @Operation(
                            operationId = "getCreditApplications",
                            summary = "Consultar solicitudes de crédito",
                            description = "Obtiene una lista paginada de solicitudes de crédito con información del usuario y cálculo de cuota mensual",
                            tags = {"Solicitudes de Crédito"},
                            parameters = {
                                    @Parameter(
                                            name = "state",
                                            description = "Estado de la solicitud (PENDIENTE, APROBADA, RECHAZADA)",
                                            in = ParameterIn.QUERY,
                                            schema = @Schema(type = "string", example = "PENDIENTE")
                                    ),
                                    @Parameter(
                                            name = "page",
                                            description = "Número de página (mínimo 1)",
                                            in = ParameterIn.QUERY,
                                            schema = @Schema(type = "integer", example = "1", minimum = "1")
                                    ),
                                    @Parameter(
                                            name = "pageSize",
                                            description = "Tamaño de página (entre 1 y 100)",
                                            in = ParameterIn.QUERY,
                                            schema = @Schema(type = "integer", example = "10", minimum = "1", maximum = "100")
                                    )
                            },
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Lista de solicitudes de crédito obtenida exitosamente",
                                            content = @Content(
                                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                                    schema = @Schema(implementation = PaginatedCreditApplicationsDto.class)
                                            )
                                    ),
                                    @ApiResponse(
                                            responseCode = "400",
                                            description = "Parámetros de consulta inválidos",
                                            content = @Content(
                                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                                    schema = @Schema(implementation = ErrorResponseDto.class)
                                            )
                                    ),
                                    @ApiResponse(
                                            responseCode = "401",
                                            description = "Token de autenticación inválido o expirado",
                                            content = @Content(
                                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                                    schema = @Schema(implementation = ErrorResponseDto.class)
                                            )
                                    ),
                                    @ApiResponse(
                                            responseCode = "403",
                                            description = "No tiene permisos para acceder a este recurso",
                                            content = @Content(
                                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                                    schema = @Schema(implementation = ErrorResponseDto.class)
                                            )
                                    ),
                                    @ApiResponse(
                                            responseCode = "500",
                                            description = "Error interno del servidor",
                                            content = @Content(
                                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                                    schema = @Schema(implementation = ErrorResponseDto.class)
                                            )
                                    )
                            }
                    )
            ),
            @RouterOperation(
                    path = "/application/{id}",
                    method = RequestMethod.PUT,
                    beanClass = Handler.class,
                    beanMethod = "updateApplicationStatus",
                    operation = @Operation(
                            operationId = "updateApplicationStatus",
                            summary = "Actualizar estado de solicitud de crédito",
                            description = "Permite a un asesor aprobar o rechazar una solicitud de crédito y enviar notificación",
                            tags = {"Solicitudes de Crédito"},
                            parameters = {
                                    @Parameter(
                                            name = "id",
                                            description = "ID de la solicitud de crédito",
                                            in = ParameterIn.PATH,
                                            required = true,
                                            schema = @Schema(type = "string", format = "uuid")
                                    )
                            },
                            requestBody = @RequestBody(
                                    description = "ID del nuevo estado y motivo de la solicitud",
                                    required = true,
                                    content = @Content(
                                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            schema = @Schema(implementation = UpdateApplicationStatusRequestDto.class)
                                    )
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Estado de solicitud actualizado exitosamente",
                                            content = @Content(
                                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                                    schema = @Schema(implementation = UpdateApplicationStatusResponseDto.class)
                                            )
                                    ),
                                    @ApiResponse(
                                            responseCode = "400",
                                            description = "Error de validación o formato inválido",
                                            content = @Content(
                                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                                    schema = @Schema(implementation = ErrorResponseDto.class)
                                            )
                                    ),
                                    @ApiResponse(
                                            responseCode = "404",
                                            description = "Solicitud de crédito no encontrada",
                                            content = @Content(
                                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                                    schema = @Schema(implementation = ErrorResponseDto.class)
                                            )
                                    ),
                                    @ApiResponse(
                                            responseCode = "403",
                                            description = "No tiene permisos para actualizar solicitudes",
                                            content = @Content(
                                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                                    schema = @Schema(implementation = ErrorResponseDto.class)
                                            )
                                    ),
                                    @ApiResponse(
                                            responseCode = "500",
                                            description = "Error interno del servidor",
                                            content = @Content(
                                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                                    schema = @Schema(implementation = ErrorResponseDto.class)
                                            )
                                    )
                            }
                    )
            ),
            @RouterOperation(
                    path = "/calculate-capacity",
                    method = RequestMethod.POST,
                    beanClass = Handler.class,
                    beanMethod = "calculateCapacity",
                    operation = @Operation(
                            operationId = "calculateCapacity",
                            summary = "Calcular capacidad de endeudamiento",
                            description = "Envía solicitud de cálculo de capacidad de endeudamiento a la cola SQS para evaluación automática",
                            tags = {"Cálculo de Capacidad"},
                            requestBody = @RequestBody(
                                    description = "Datos para el cálculo de capacidad de endeudamiento",
                                    required = true,
                                    content = @Content(
                                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            schema = @Schema(implementation = CalculateCapacityRequestDto.class)
                                    )
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Solicitud de cálculo enviada exitosamente",
                                            content = @Content(
                                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                                    schema = @Schema(implementation = CalculateCapacityResponseDto.class)
                                            )
                                    ),
                                    @ApiResponse(
                                            responseCode = "400",
                                            description = "Error de validación o formato inválido",
                                            content = @Content(
                                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                                    schema = @Schema(implementation = ErrorResponseDto.class)
                                            )
                                    ),
                                    @ApiResponse(
                                            responseCode = "500",
                                            description = "Error interno del servidor",
                                            content = @Content(
                                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                                    schema = @Schema(implementation = ErrorResponseDto.class)
                                            )
                                    )
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> routerFunction(Handler handler) {
        return route(POST(creditApplicationPath + "/createApplication"), handler::createCreditApplication)
                .andRoute(GET(creditApplicationPath + "/creditApplication"), handler::getCreditApplications)
                .andRoute(PUT(creditApplicationPath + "/application/{id}"), handler::updateApplicationStatus)
                .andRoute(POST(creditApplicationPath + "/calculate-capacity"), handler::calculateCapacity);
    }
}

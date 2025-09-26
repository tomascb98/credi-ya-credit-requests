package co.com.crediya.model.loancapacity;

import java.math.BigDecimal;
import java.util.List;

public record LoanCapacityRequest(
        String solicitudId,
        BigDecimal ingresosTotales,
        List<PrestamoActivo> prestamosActivos,
        NuevoPrestamo nuevoPrestamo,
        String recipientEmail,
        String traceId
) {
    public record PrestamoActivo(
            BigDecimal monto,
            BigDecimal tasaMensual,
            Integer plazoMeses
    ) {}

    public record NuevoPrestamo(
            BigDecimal monto,
            BigDecimal tasaMensual,
            Integer plazoMeses
    ) {}
}
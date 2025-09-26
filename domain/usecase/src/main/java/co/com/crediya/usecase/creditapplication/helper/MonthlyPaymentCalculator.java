package co.com.crediya.usecase.creditapplication.helper;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MonthlyPaymentCalculator {
    
    /**
     * Calcula el monto mensual aproximado usando la fórmula de cuota fija
     * PMT = P * [r(1+r)^n] / [(1+r)^n - 1]
     * Donde:
     * P = Principal (monto del préstamo)
     * r = Tasa de interés mensual (tasa anual / 12 / 100)
     * n = Número de meses
     */
    public static BigDecimal calculateMonthlyPayment(BigDecimal principal, BigDecimal annualInterestRate, Integer months) {
        if (principal == null || annualInterestRate == null || months == null || months <= 0) {
            return BigDecimal.ZERO;
        }
        
        // Convertir tasa anual a mensual (dividir por 12 y por 100)
        BigDecimal monthlyRate = annualInterestRate.divide(BigDecimal.valueOf(1200), 6, RoundingMode.HALF_UP);
        
        // Calcular (1 + r)^n
        BigDecimal onePlusRate = BigDecimal.ONE.add(monthlyRate);
        BigDecimal powerTerm = onePlusRate.pow(months);
        
        // Calcular el numerador: P * [r(1+r)^n]
        BigDecimal numerator = principal.multiply(monthlyRate).multiply(powerTerm);
        
        // Calcular el denominador: [(1+r)^n - 1]
        BigDecimal denominator = powerTerm.subtract(BigDecimal.ONE);
        
        // Evitar división por cero
        if (denominator.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP);
        }
        
        // Calcular PMT = numerador / denominador
        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }
}

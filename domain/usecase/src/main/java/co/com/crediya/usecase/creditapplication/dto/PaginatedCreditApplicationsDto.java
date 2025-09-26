package co.com.crediya.usecase.creditapplication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class PaginatedCreditApplicationsDto {
    private List<CreditApplicationWithUserDto> data;
    private PaginationMetaDto meta;
}

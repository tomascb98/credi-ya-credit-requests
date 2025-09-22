package co.com.crediya.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class PaginationMetaDto {
    private int page;
    private int pageSize;
    private int totalElements;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;
}

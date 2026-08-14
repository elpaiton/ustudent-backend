package co.edu.usta.ustudent.shared.api.dto;

import java.util.List;
import java.util.function.Function;
import org.springframework.data.domain.Page;

/**
 * Respuesta paginada de la API.
 *
 * <p>Es un tipo propio y no el {@code Page} de Spring Data a proposito: la
 * forma en que Spring serializa {@code Page} cambia entre versiones, y el
 * contrato publicado no puede depender de un detalle interno del framework.
 * Aqui la forma es nuestra y solo cambia si la cambiamos.
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last) {

    public static <E, T> PageResponse<T> from(Page<E> page, Function<E, T> mapper) {
        return new PageResponse<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast());
    }
}

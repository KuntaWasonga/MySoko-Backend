package com.dukani.productservice.dtos;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StandardResponse<T> implements Serializable {
    private static final long serialVersionUID = 8841027323012207948L;

    private T data;
    private String message;
    private Integer status;
    private LocalDateTime timestamp;
    private String path;
}

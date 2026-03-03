package com.dukani.userservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

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

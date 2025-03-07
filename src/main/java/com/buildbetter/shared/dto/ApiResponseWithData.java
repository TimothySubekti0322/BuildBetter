package com.buildbetter.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiResponseWithData<T> {
    private int code;
    private String status;
    private T data;
}

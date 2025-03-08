package com.buildbetter.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponseWithData<T> {
    private int code;
    private String status;
    private T data;
}

package com.buildbetter.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiResponseMessageOnly {
    private int code;
    private String status;
    private String message;
}

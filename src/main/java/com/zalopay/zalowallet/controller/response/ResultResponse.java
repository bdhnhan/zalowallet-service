package com.zalopay.zalowallet.controller.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResultResponse<T> {
    private Long status;
    private T result;
}

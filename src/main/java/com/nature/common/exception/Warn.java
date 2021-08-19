package com.nature.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Warn extends RuntimeException {

    private String message;

}

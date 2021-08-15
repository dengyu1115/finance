package com.nature.common.model;

import lombok.Getter;

@Getter
public class Res<T> extends BaseModel {

    private String code;

    private String massage;

    private T data;

    public Res() {

    }

    private Res(String code, String message, T data) {
        this.code = code;
        this.massage = message;
        this.data = data;
    }

    public static <T> Res<T> ok() {
        return ok(null);
    }

    public static <T> Res<T> ok(T data) {
        return new Res<>("200", null, data);
    }

    public static <T> Res<T> warn() {
        return warn(null);
    }

    public static <T> Res<T> warn(String message) {
        return new Res<>("400", message, null);
    }

    public static <T> Res<T> error() {
        return error(null);
    }

    public static <T> Res<T> error(String message) {
        return new Res<>("500", message, null);
    }
}

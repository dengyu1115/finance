package com.nature.common.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Req<T> extends BaseModel {

    private T data;

}

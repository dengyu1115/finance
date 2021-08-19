package com.nature.finance.model;

import com.nature.common.model.BaseModel;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.annotation.Id;
import org.apache.ibatis.annotation.Model;

@Getter
@Setter
@Model(table = "item")
public class Item extends BaseModel {

    @Id
    private String id;

    private String code;

    private String name;

}

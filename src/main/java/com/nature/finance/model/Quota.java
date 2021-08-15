package com.nature.finance.model;

import com.nature.common.model.BaseModel;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.annotation.Model;

@Getter
@Setter
@Model(table = "quota")
public class Quota extends BaseModel {

    private String id;

    private String code;

    private String date;

    private Double price;

    private Double roe;

    private Double peTtm;

    private Double peRatio;

    private Double profit;
}

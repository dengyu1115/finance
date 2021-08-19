package com.nature.finance.controller;

import com.nature.common.aop.annotation.Web;
import com.nature.common.exception.Warn;
import com.nature.common.model.Res;
import com.nature.finance.manager.ItemManager;
import com.nature.finance.model.Item;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("item")
@RestController
public class ItemController {

    @Autowired
    private ItemManager itemManager;

    @Web
    @PostMapping("merge")
    public Res<Integer> merge(@RequestBody Item item) {
        if (StringUtils.isBlank(item.getCode())) {
            throw new Warn("code is blank");
        }
        if (StringUtils.isBlank(item.getName())) {
            throw new Warn("name is blank");
        }
        item.setId(item.getCode());
        return Res.ok(itemManager.merge(item));
    }
}

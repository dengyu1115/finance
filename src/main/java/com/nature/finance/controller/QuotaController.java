package com.nature.finance.controller;

import com.nature.common.aop.annotation.Web;
import com.nature.common.model.Req;
import com.nature.common.model.Res;
import com.nature.finance.manager.QuotaManager;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("quota")
@RestController
public class QuotaController {

    @Autowired
    private QuotaManager quotaManager;

    @Web
    @PostMapping("loadByCode")
    public Res<Integer> loadByCode(@RequestBody Req<String> req) {
        String code = req.getData();
        if (StringUtils.isBlank(code)) {
            throw new RuntimeException("code");
        }
        return Res.ok(quotaManager.loadByCode(code));
    }

}

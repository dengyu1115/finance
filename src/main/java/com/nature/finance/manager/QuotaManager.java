package com.nature.finance.manager;

import com.nature.finance.http.QuotaHttp;
import com.nature.finance.mapper.QuotaMapper;
import com.nature.finance.model.Quota;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuotaManager {

    @Autowired
    private QuotaHttp quotaHttp;
    @Autowired
    private QuotaMapper quotaMapper;

    public int loadByCode(String code) {
        List<Quota> list = quotaHttp.listByCode(code);
        if (list.isEmpty()) {
            return 0;
        }
        return quotaMapper.batchMerge(list);
    }
}

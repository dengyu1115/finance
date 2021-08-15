package com.nature.finance.mapper;

import com.nature.finance.model.Quota;
import org.apache.ibatis.annotation.TableModel;
import org.apache.ibatis.function.BatchMerge;

@TableModel(Quota.class)
public interface QuotaMapper extends BatchMerge<Quota> {
}

package org.apache.ibatis.function;

import org.apache.ibatis.annotations.Param;

import java.util.List;

@FunctionalInterface
public interface BatchMerge<T> {

    int batchMerge(@Param("list") List<T> list);
}

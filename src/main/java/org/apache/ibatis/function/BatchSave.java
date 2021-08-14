package org.apache.ibatis.function;

import org.apache.ibatis.annotations.Param;

import java.util.List;

@FunctionalInterface
public interface BatchSave<T> {

    int batchSave(@Param("list") List<T> list);
}

package org.apache.ibatis.function;

import org.apache.ibatis.annotations.Param;

import java.util.List;

@FunctionalInterface
public interface DeleteByIds<T> {

    int deleteByIds(@Param("ids") List<T> ids);
}

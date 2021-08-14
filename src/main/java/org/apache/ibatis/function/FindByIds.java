package org.apache.ibatis.function;

import org.apache.ibatis.annotations.Param;

import java.util.List;

@FunctionalInterface
public interface FindByIds<T, I> {

    List<T> findByIds(@Param("ids") List<I> ids);
}

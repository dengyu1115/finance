package org.apache.ibatis.function;

import org.apache.ibatis.annotations.Param;

@FunctionalInterface
public interface FindById<T, I> {

    T findById(@Param("id") I id);
}

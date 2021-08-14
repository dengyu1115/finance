package org.apache.ibatis.function;

import org.apache.ibatis.annotations.Param;

@FunctionalInterface
public interface DeleteById<T> {

    int deleteById(@Param("id") T id);
}

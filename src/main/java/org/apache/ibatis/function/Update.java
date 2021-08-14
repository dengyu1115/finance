package org.apache.ibatis.function;

@FunctionalInterface
public interface Update<T> {

    int update(T datum);
}

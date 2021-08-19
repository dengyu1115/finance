package com.nature.finance.mapper;

import com.nature.finance.model.Item;
import org.apache.ibatis.annotation.TableModel;
import org.apache.ibatis.function.Merge;

@TableModel(Item.class)
public interface ItemMapper extends Merge<Item> {
}

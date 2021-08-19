package com.nature.finance.manager;

import com.nature.finance.mapper.ItemMapper;
import com.nature.finance.model.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ItemManager {

    @Autowired
    private ItemMapper itemMapper;

    public int merge(Item item) {
        return itemMapper.merge(item);
    }
}

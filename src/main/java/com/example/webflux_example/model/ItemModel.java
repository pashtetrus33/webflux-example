package com.example.webflux_example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemModel {

    private String id;
    private String name;
    private Integer count;

    private List<SubItemModel> subItems;
}

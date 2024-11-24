package ru.isands.test.estore.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ShopDto {

    private Long id;
    private String name;
    private String address;
    private String purchaseType;
    private Long totalSales;
}

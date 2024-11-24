package ru.isands.test.estore.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ElectroShopDTO {

    private Long shopId;
    private Long electroItemId;
    private Integer count;
}

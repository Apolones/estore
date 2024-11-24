package ru.isands.test.estore.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseDTO {

    private Long electroItemId;
    private Long employeeId;
    private Long shopId;
    private Long purchaseTypeId;
}
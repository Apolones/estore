package ru.isands.test.estore.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BestEmployeeDTO {

    private Long employeeId;
    private String firstName;
    private String lastName;
    private String positionName;
    private Long itemsSold;
    private Long totalSales;
}

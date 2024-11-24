package ru.isands.test.estore.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeDTO {

    private String lastName;
    private String firstName;
    private String patronymic;
    private Date birthDate;
    private Long positionId;
    private Long shopId;
    private boolean gender;
}

package ru.isands.test.estore.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ElectroItemDTO {

    private String name;
    @JsonProperty("eTypeId")
    private Long eTypeId;
    private Long price;
    private Integer count;
    private String description;
}

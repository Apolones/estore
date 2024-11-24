package ru.isands.test.estore.dao.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Embeddable
public class ElectroEmployeePK implements Serializable {

    private Long electroType;
    private Long employee;


    public ElectroEmployeePK() {
    }

    public ElectroEmployeePK(Long electroType, Long employee) {
        this.electroType = electroType;
        this.employee = employee;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ElectroEmployeePK that = (ElectroEmployeePK) o;
        return Objects.equals(electroType, that.electroType) &&
                Objects.equals(employee, that.employee);
    }

    @Override
    public int hashCode() {
        return Objects.hash(electroType, employee);
    }
}

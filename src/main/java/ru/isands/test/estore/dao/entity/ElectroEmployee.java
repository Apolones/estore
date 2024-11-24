package ru.isands.test.estore.dao.entity;

import java.io.Serializable;
import javax.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "store_electroEmployee")
public class ElectroEmployee implements Serializable {

    /**
     * Составной ключ
     */
    @EmbeddedId
    private ElectroEmployeePK id;

    /**
     * Идентификатор сотрудника
     */
    @ManyToOne
    @JoinColumn(name = "employeeId")
    private Employee employee;

    /**
     * Идентификатор тип электроники
     */
    @ManyToOne
    @JoinColumn(name = "electroTypeId")
    private ElectroType electroType;

}
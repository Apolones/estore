package ru.isands.test.estore.dao.entity;

import java.io.Serializable;
import javax.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "employee_position")
public class PositionType implements Serializable {

    /**
     * Идентификатор должности
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    /**
     * Наименование должности
     */
    @Column(name = "name", nullable = false, length = 150)
    private String name;
}

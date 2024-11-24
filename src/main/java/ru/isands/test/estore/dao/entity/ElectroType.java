package ru.isands.test.estore.dao.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "store_electroType")
public class ElectroType implements Serializable {

    /**
     * Идентификатор типа электроники
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    /**
     * Наименование типа электроники
     */
    @Column(name = "name", nullable = false, length = 150)
    private String name;
}

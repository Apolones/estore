package ru.isands.test.estore.dao.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "store_shop")
public class Shop implements Serializable {

    /**
     * Идентификатор магазина
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    /**
     * Наименование магазина
     */
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    /**
     * Адрес магазина
     */
    @Column(name = "address", columnDefinition = "TEXT")
    private String address;
}

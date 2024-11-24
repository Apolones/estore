package ru.isands.test.estore.dao.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "store_purchase_type")
public class PurchaseType implements Serializable {

    /**
     * Идентификатор типа покупки
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    /**
     * Наименование типа покупки
     */
    @Column(name = "name", nullable = false, length = 150)
    private String name;
}

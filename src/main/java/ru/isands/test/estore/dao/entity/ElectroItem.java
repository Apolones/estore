package ru.isands.test.estore.dao.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "store_electroItem")
public class ElectroItem implements Serializable {
    /**
     * Идентификатор электротовара
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    /**
     * Название товара
     */
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    /**
     * Ссылка на тип товара
     */
    @ManyToOne
    @JoinColumn(name = "etypeId", nullable = false)
    private ElectroType eType;

    /**
     * Цена товара в рублях
     */
    @Column(name = "price", nullable = false)
    private Long price;

    /**
     * Общее количество товара в наличии
     */
    @Column(name = "count", nullable = false)
    private Integer count;

    /**
     * Признак архивности товара
     * true - товара нет в наличии, false - товар в продаже
     */
    @Column(name = "archive", nullable = false)
    private boolean archive;

    /**
     * Описание товара
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

}

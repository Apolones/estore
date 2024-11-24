package ru.isands.test.estore.dao.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "store_purchase")
public class Purchase implements Serializable {

    /**
     * Идентификатор покупки
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    /**
     * Идентификатор товара
     */
    @ManyToOne
    @JoinColumn(name = "elecroId", nullable = false)
    private ElectroItem electroItem;

    /**
     * Идентификатор сотрудника
     */
    @ManyToOne
    @JoinColumn(name = "employeeId", nullable = false)
    private Employee employee;

    /**
     * Идентификатор магазина
     */
    @ManyToOne
    @JoinColumn(name = "shopId", nullable = false)
    private Shop shop;

    /**
     * Дата совершения покупки
     */
    @Column(name = "purchaseDate", nullable = false)
    private Date purchaseDate;

    /**
     * Способ оплаты
     */
    @ManyToOne
    @JoinColumn(name = "typeId", nullable = false)
    private PurchaseType purchaseType;

}
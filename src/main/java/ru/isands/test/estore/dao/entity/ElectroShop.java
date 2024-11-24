package ru.isands.test.estore.dao.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "store_eshop")
public class ElectroShop implements Serializable {

    /**
     * Составной ключ
     */
    @EmbeddedId
    private ElectroShopPK id;

    /**
     * Идентификатор магазина
     */
    @ManyToOne
    @JoinColumn(name = "shopId")
    private Shop shop;

    /**
     * Идентификатор электротовара
     */
    @ManyToOne
    @JoinColumn(name = "electroItemId")
    private ElectroItem electroItem;

    /**
     * Оставшееся количество
     */
    @Column(name = "count", nullable = false)
    private Integer count;
}

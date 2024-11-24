package ru.isands.test.estore.dao.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Embeddable
public class ElectroShopPK implements Serializable {

    private Long electroItem;
    private Long shop;


    public ElectroShopPK() {
    }

    public ElectroShopPK(Long electroItem, Long shop) {
        this.electroItem = electroItem;
        this.shop = shop;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ElectroShopPK that = (ElectroShopPK) o;
        return Objects.equals(electroItem, that.electroItem) &&
                Objects.equals(shop, that.shop);
    }

    @Override
    public int hashCode() {
        return Objects.hash(electroItem, shop);
    }
}

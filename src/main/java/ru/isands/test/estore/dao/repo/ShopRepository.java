package ru.isands.test.estore.dao.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.isands.test.estore.dao.entity.Shop;
import ru.isands.test.estore.dto.ShopDto;

import java.util.List;

public interface ShopRepository extends JpaRepository<Shop, Long> {

    @Query("SELECT new ru.isands.test.estore.dto.ShopDto(" +
            "s.id, s.name, s.address, p.purchaseType.name, SUM(p.electroItem.price)) " +
            "FROM Shop s " +
            "JOIN Purchase p ON p.shop.id = s.id " +
            "WHERE s.id = :id AND p.purchaseType.name = :purchaseType " +
            "GROUP BY s.id, s.name, s.address, p.purchaseType.name")
    List<ShopDto> findShopsByPurchaseType(@Param("id") Long id, @Param("purchaseType") String purchaseType);
}

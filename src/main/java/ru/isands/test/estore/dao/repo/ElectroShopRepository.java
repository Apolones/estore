package ru.isands.test.estore.dao.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.isands.test.estore.dao.entity.ElectroShop;
import ru.isands.test.estore.dao.entity.ElectroShopPK;

public interface ElectroShopRepository extends JpaRepository<ElectroShop, ElectroShopPK> {
    @Query("SELECT COUNT(e) > 0 " +
            "FROM ElectroShop e " +
            "WHERE e.shop.id = :shopId AND e.electroItem.id = :itemId AND e.count > 0")
    boolean isItemAvailable(@Param("shopId") Long shopId, @Param("itemId") Long itemId);
}

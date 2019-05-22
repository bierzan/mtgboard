package com.brzn.mtgboard.card.offer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface OfferRepo extends JpaRepository<Offer, Long> {

    @Query(value = "SELECT * FROM offers where card_id = ?1 AND offer_type= ?2 AND is_foiled = ?3", nativeQuery = true)
    List<Offer> findAllByCardIdAndOfferTypeAndFoiled(long cardId, OfferType type, boolean isFoiled);

    Offer findOneByCardId(long id);

    @Query(value = "SELECT * FROM offers where card_id = ?1 AND user_id = ?2", nativeQuery = true)
    List<Offer> findAllByCardIdAndUserId(long cardId, long userId);

    @Query(value = "SELECT * FROM offers\n" +
            "WHERE " +
            "language = ?1 AND " +
            "card_condition = ?2 AND " +
            "is_altered = ?3 AND " +
            "is_foiled = ?4 AND " +
            "is_signed = ?5 AND " +
            "user_id = ?6 AND " +
            "card_id = ?7 AND " +
            "price = ?8 AND " +
            "offer_type = ?9 " +
            "limit 1", nativeQuery = true)
    Offer findEqualOffer(String lang, String cond, boolean altered, boolean foiled, boolean signed, long userId, long cardId, BigDecimal price, String offerType);

    @Modifying
    @Query(value = "UPDATE offers SET " +
            "card_condition=?1, " +
            "language=?2, " +
            "quantity=?3, " +
            "comment=?4, " +
            "is_altered=?5, " +
            "is_foiled=?6, " +
            "is_signed=?7, " +
            "price=?8, " +
            "updated=?9 " +
            "WHERE id=?10", nativeQuery = true)
    void updateOfferByCardId(
            String cardCond,
            String lang,
            int quantity,
            String comment,
            boolean isAltered,
            boolean isFoiled,
            boolean isSigned,
            BigDecimal price,
            String localDateTime,
            long id);
}

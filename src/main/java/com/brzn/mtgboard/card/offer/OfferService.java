package com.brzn.mtgboard.card.offer;

import com.brzn.mtgboard.card.Card;
import com.brzn.mtgboard.card.CardService;
import com.brzn.mtgboard.user.User;
import com.brzn.mtgboard.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLDataException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class OfferService {

    private OfferRepo offerRepo;
    private CardPriceHistoryService wcPriceHistoryService;
    private CardService cardService;
    private UserService userService;

    @Autowired
    public OfferService(OfferRepo offerRepo, CardPriceHistoryService wcPriceHistoryService, CardService cardService, UserService userService) {
        this.offerRepo = offerRepo;
        this.wcPriceHistoryService = wcPriceHistoryService;
        this.cardService = cardService;
        this.userService = userService;
    }


    public void saveCardOffer(Offer offer) throws SQLDataException {

        Offer existingOffer = findExistingOffer(offer);

        if (existingOffer != null) {
            updateOfferQuantity(existingOffer, offer);
        } else {
            offer.setCreated(LocalDateTime.now());
            setUserAndCard(offer);
            offerRepo.save(offer);

        }

        updateAvgCardPrice(offer);
    }

    private Offer findExistingOffer(Offer offer) {
        return offerRepo.findEqualOffer(offer.getLanguage(),
                offer.getCardCondition().toString(),
                offer.isAltered(),
                offer.isFoiled(),
                offer.isSigned(),
                offer.getUser().getId(),
                offer.getCard().getId(),
                offer.getPrice(),
                offer.getOfferType().toString());
    }

    private void updateOfferQuantity(Offer existingOffer, Offer offer) {
        existingOffer.setQuantity(existingOffer.getQuantity() + offer.getQuantity());
        if (existingOffer.getComment() == null) {
            existingOffer.setComment(offer.getComment());
        }
        updateEntityRecord(existingOffer);
    }

    private void updateEntityRecord(Offer offer) {
        offer.updateDate();
        offerRepo.updateOfferByCardId(
                offer.getCardCondition().toString(),
                offer.getLanguage(),
                offer.getQuantity(),
                offer.getComment(),
                offer.isAltered(),
                offer.isFoiled(),
                offer.isSigned(),
                offer.getPrice(),
                offer.getUpdated().toString(),
                offer.getId()
        );
    }
    //todo protected na metodach i testy wszystkiego!

    private void setUserAndCard(Offer offer) throws SQLDataException {
        Card cardToSet = cardService.getCardById(offer.getCard().getId());
        User userToSet = userService.getUserById(offer.getUser().getId());
        offer.setUser(userToSet);
        offer.setCard(cardToSet);
    }

    private void updateAvgCardPrice(Offer offer) {
        Card card = offer.getCard();
        CardPriceHistory priceHistory = new CardPriceHistory(card, getCardsAvgPrice(card, offer), offer.isFoiled(), offer.getOfferType());
        wcPriceHistoryService.updatedAvgPrice(priceHistory);
    }

    private BigDecimal getCardsAvgPrice(Card card, Offer offer) {
        List<Offer> offers = offerRepo.findAllByCardIdAndOfferTypeAndFoiled(card.getId(), offer.getOfferType(), offer.isFoiled());
        int totalQuantity = 0;
        BigDecimal sumOfPrices = new BigDecimal(0);
        for (Offer o : offers) {
            totalQuantity += o.getQuantity();
            sumOfPrices = sumOfPrices.add(o.getPrice().multiply(new BigDecimal(o.getQuantity())));
        }
        if (totalQuantity == 0) {
            return offer.getPrice();
        }

        return sumOfPrices.divide(new BigDecimal(totalQuantity), 2, RoundingMode.HALF_DOWN);
    }
}

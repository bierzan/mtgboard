package com.brzn.mtgboard.card;

import com.brzn.mtgboard.card.counter.SearchCounter;
import com.brzn.mtgboard.card.counter.SearchCounterService;
import com.brzn.mtgboard.card.counter.transfer.NumberOfSearchesWithCardId;
import com.brzn.mtgboard.cardsSet.CardSetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping("/cards")
class CardController {

    CardService cardService;
    CardSetService cardSetService;
    SearchCounterService searchCounterService;

    @Autowired
    public CardController(CardService cardService, CardSetService cardSetService, SearchCounterService searchCounterService) {
        this.cardService = cardService;
        this.cardSetService = cardSetService;
        this.searchCounterService = searchCounterService;
    }

    @GetMapping("/name/like/{name}")
    ResponseEntity<List<CardForSearchResult>> getCardByPartialName(@PathVariable("name") String name) throws IOException {
        List<CardForSearchResult> cards = cardService.findAllByPartialName(name);
        if (cards.size() == 0) {
            cards = cardService.findAllByPartialNameFromApi(name);
        }
        return ResponseEntity.ok(cards);
    }

    @GetMapping("/name/set/{name}/{setName}")
    ResponseEntity<CardForCardPage> getCardByNameAndSetName(@PathVariable("name") String name,
                                                            @PathVariable("setName") String setName) {
        CardForCardPage card = cardService.getDtoCardByNameAndSetName(name, setName);
        if (card == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(card);
    }

    @PostMapping("/name/set/{name}/{setName}")
    ResponseEntity<CardForCardPage> postCardByNameAndSetName(@PathVariable("name") String name,
                                                             @PathVariable("setName") String setName) throws IOException {
        CardForCardPage card = cardService.postCardByNameAndSetName(name, setName);
        if (card == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(card);
    }

    @PostMapping("/name/{name}")
    ResponseEntity<List<Card>> postCardsByName(@PathVariable("name") String name) throws IOException {
        List<Card> cards = cardService.postCardsByName(name);
        if (cards == null || cards.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(cards);
    }

    @PostMapping("/counter/{cardName}/{setName}")
    ResponseEntity<Void> addSearch(@PathVariable("cardName") String cardName,
                                            @PathVariable("setName") String setName) throws URISyntaxException {
        SearchCounter searchCounter = searchCounterService.addSearch(cardService.getCardByNameAndSetName(cardName, setName));
        return ResponseEntity.created(new URI("/cards/counter/"+searchCounter.getId())).build();
    }

    @GetMapping("/counter/{cardId}")
    ResponseEntity<NumberOfSearchesWithCardId> getCountedSearches(@PathVariable("cardId") long cardId) {
        return ResponseEntity.ok(searchCounterService.getCountedSearch(cardId));
    }


}


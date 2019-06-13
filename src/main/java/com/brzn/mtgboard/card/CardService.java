package com.brzn.mtgboard.card;

import com.brzn.mtgboard.card.cardsSet.CardSet;
import com.brzn.mtgboard.card.cardsSet.CardSetRepo;
import com.brzn.mtgboard.card.cardsSet.CardSetService;
import com.brzn.mtgboard.card.dto.CardForCardPage;
import com.brzn.mtgboard.card.dto.CardNameAndSetName;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.transaction.Transactional;
import java.io.IOException;
import java.sql.SQLDataException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CardService {

    private CardRepo cardRepo;
    private CardSetRepo cardSetRepo;
    private CardSetService cardSetService;
    private String cardApi = "https://api.magicthegathering.io/v1/cards?";

    @Autowired
    public CardService(CardRepo cardRepo, CardSetRepo cardSetRepo, CardSetService cardSetService) {
        this.cardRepo = cardRepo;
        this.cardSetRepo = cardSetRepo;
        this.cardSetService = cardSetService;
    }

    public void save(Card card) {
        cardRepo.save(card);
    }

    List<CardNameAndSetName> findAllByPartialName(String name) throws IOException {
        return cardRepo.findAllByPartialNameForSearchResult(name);
    }

    List<CardNameAndSetName> findAllByPartialNameFromApi(String name) throws IOException {
        List<Card> cards = getCardsFromExternalAPI(name);
        return cards.stream().map(x -> new CardNameAndSetName(x.getName(), x.getSet().getName())).collect(Collectors.toList());
    }

    List<Card> getCardsFromExternalAPI(String name) throws IOException {
        String apiUrl = String.format("%sname=%s", cardApi, name);
        CardList cardsFromAPI = mapToCardListClassFromAPI(apiUrl);
        return cardsFromAPI.getCards();
    }

    CardForCardPage getDtoCardByNameAndSetName(String cardName, String setName) {
        return cardRepo.findDtoByNameAndSetName(cardName, setName);

    }

    Card getCardByNameAndSetName(String cardName, String setName) {
        return cardRepo.findByNameAndSetName(cardName, setName);

    }

    protected CardForCardPage postCardByNameAndSetName(String cardName, String setName) throws IOException {
        String apiUrl = String.format("%sname=%s&setName=%s", cardApi, cardName, setName);
        Card card = mapToCardListClassFromAPI(apiUrl).getCards().stream().findFirst().orElseThrow(IOException::new);
        setCardSetForCard(card);
        cardRepo.save(card);
        return new CardForCardPage(card);
    }

    private void setCardSetForCard(Card card) throws IOException {
        CardSet set = cardSetRepo.findByName(card.getSet().getName());
        if (set == null) {
            set = cardSetService.getCardSetByNameFromAPI(card.getSet().getName());
            cardSetService.saveCardSet(set);
        }
        card.setSet(set);
        cardSetService.saveCardSet(set);
    }

    protected List<Card> postCardsByName(String name) throws IOException {
        List<Card> cardsFromAPI = getCardsFromExternalAPI(name).stream()
                .filter(card -> card.getName().equalsIgnoreCase(name))
                .collect(Collectors.toList());

        if (cardsFromAPI.isEmpty()) {
            return cardsFromAPI;
        }

        List<Card> postedCards = new ArrayList<>();

        for (Card card : cardsFromAPI) {
            setCardSetForCard(card);
            cardRepo.save(card);
            postedCards.add(card);
        }

        return postedCards;
    }

    private CardList mapToCardListClassFromAPI(String apiUrl) throws IOException {
        RestTemplate restTemplate = getRestTemplateWithHeaders();

        String jsonString = restTemplate.getForObject(apiUrl, String.class);

        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(jsonString, CardList.class);
    }

    private RestTemplate getRestTemplateWithHeaders() {
        RestTemplate restTemplate = new RestTemplate();

        restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().set("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");
            return execution.execute(request, body);
        });
        return restTemplate;
    }

    public Card getCardById(long id) throws SQLDataException {
        return cardRepo.findById(id).orElseThrow(SQLDataException::new);
    }

    private CardNameAndSetName mapToSearchByNameResult(Card card) {
        return new CardNameAndSetName(card.getName(), card.getSet().getName());
    }
}

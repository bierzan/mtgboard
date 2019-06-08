package com.brzn.mtgboard.card;

import com.brzn.mtgboard.card.cardsSet.CardSet;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class CardListDeserializer extends StdDeserializer<CardList> {

    public CardListDeserializer() {
        super(CardSet.class);
    }


    public CardListDeserializer(Class<?> vc) {
        super(vc);
    }

    public CardListDeserializer(JavaType valueType) {
        super(valueType);
    }

    public CardListDeserializer(StdDeserializer<?> src) {
        super(src);
    }

    @Override
    public CardList deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        CardList cl = new CardList();
        List<Card> cards = new ArrayList<>();
        JsonNode jsonNode = jsonParser.getCodec().readTree(jsonParser);
        JsonNode jsonCards = jsonNode.get("cards");

        for (JsonNode node : jsonCards) {
            Card card = new Card();
            card.setName(node.get("name").asText());

            if (node.has("manaCost")) {
                card.setManaCost(node.get("manaCost").asText());
            }

            if (node.has("names")) {
                StringBuilder sb = new StringBuilder();
                for (JsonNode nameNode : node.get("names")) {
                    sb.append(nameNode.asText());
                    sb.append(" / ");
                }
                String names = sb.toString();
                if (names.length() > 0) card.setNames(names.substring(0, names.length() - 3));
            }

            card.setCmc(node.get("cmc").asText());
            if (node.get("colors").size() > 0) {
                StringBuilder sb = new StringBuilder();
                for (JsonNode colorNode : node.get("colors")) {
                    sb.append(colorNode.asText());
                    sb.append(", ");
                }
                String colors = sb.toString();
                card.setColors(colors.substring(0, colors.length() - 2));
            }

            card.setType(node.get("type").asText());
            card.setRarity(node.get("rarity").asText());
            card.setArtist(node.get("artist").asText());
            card.setNumber(node.get("number").asText());
            card.setLayout(node.get("layout").asText());
            card.setSet(new CardSet(node.get("setName").asText()));

            if (node.has("text")) card.setText(node.get("text").asText());
            if (node.hasNonNull("flavor")) card.setFlavor(node.get("flavor").asText());
            if (node.hasNonNull("power")) card.setPower(node.get("power").asText());
            if (node.hasNonNull("toughness")) card.setToughness(node.get("toughness").asText());
            if (node.hasNonNull("multiverseid")) card.setMultiverseId(node.get("multiverseid").asLong());
            if (node.hasNonNull("imageUrl")) card.setImageUrl(new URL(node.get("imageUrl").asText()));

            if (node.get("foreignNames").size() > 0) {
                StringBuilder langs = new StringBuilder();
                langs.append("English, ");
                for (JsonNode langNode : node.get("foreignNames")) {
                    langs.append(langNode.get("language").asText());
                    langs.append(", ");
                }
                String languages = langs.toString();
                card.setLanguages(languages.substring(0, languages.length() - 2));
            } else{
                card.setLanguages("English");
            }

            cards.add(card);
        }

        cl.setCards(cards);
        return cl;

    }
}

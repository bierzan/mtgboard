package com.brzn.mtgboard.card.counter;

import com.brzn.mtgboard.card.Card;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "search_count")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
public class SearchCounter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @Setter
    private Long id;

    @OneToOne
    @JoinColumn(name = "card_id")
    @Getter
    @Setter
    private Card card;

    @Getter
    @Setter
    private long countedSearches = 1;

    public SearchCounter() {
    }

    public SearchCounter(Card card) {
        this.card = card;
    }
}

package com.brzn.mtgboard.card.cardsSet;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CardSetRepo extends JpaRepository<CardSet, Long> {

    CardSet findByCode(String code);
    CardSet findByName(String name);
}

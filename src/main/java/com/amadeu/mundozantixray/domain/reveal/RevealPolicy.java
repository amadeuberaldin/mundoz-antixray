package com.amadeu.mundozantixray.domain.reveal;

import com.amadeu.mundozantixray.domain.model.RevealDecision;

public interface RevealPolicy {

    RevealDecision evaluate(
            RevealContext context
    );
}

package com.skillswap.matching;

import java.util.UUID;

/**
 * Spring Data projection : interface que Spring remplit automatiquement
 * avec les colonnes retournées par la requête native dans MatchingRepository.
 *
 * Chaque getter correspond exactement à un alias de colonne dans le SELECT.
 */
public interface MatchCandidateProjection {

    UUID getId();

    String getDisplayName();

    Double getAverageRating();

    String getBio();

    String getAvatarUrl();

    Long getTheyTeachMe();

    Long getITeachThem();
    
}

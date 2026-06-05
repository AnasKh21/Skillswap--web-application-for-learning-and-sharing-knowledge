package com.skillswap.matching;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.skillswap.user.User;

import java.util.List;
import java.util.UUID;

@Repository
public interface MatchingRepository extends JpaRepository<User, UUID> {

    // ─────────────────────────────────────────────────────────────────────────
    // TODO 1 — Requête de base : qui offre ce qu'Alice veut ?
    //
    // Tu as déjà écrit cette requête sur papier :
    //
    //   SELECT DISTINCT u.id, u.display_name, u.average_rating
    //   FROM users u
    //   JOIN user_skills us ON us.user_id = u.id
    //   WHERE us.type = 'OFFERED'
    //     AND u.id != :alice_id
    //     AND us.skill_id IN (
    //         SELECT skill_id
    //         FROM user_skills
    //         WHERE user_id = :alice_id
    //           AND type = 'WANTED'
    //     );
    //
    // Transforme-la en @Query Spring Data ci-dessous.
    // Utilise nativeQuery = true et le paramètre @Param("myId").
    // Le type de retour est List<MatchCandidateProjection>.
    // ─────────────────────────────────────────────────────────────────────────

    // TODO 1 — écris ici la @Query avec nativeQuery = true
   @Query(value = """
        SELECT DISTINCT u.id, u.display_name, u.average_rating, u.bio
        FROM users u
        JOIN user_skills us ON us.user_id = u.id
        WHERE us.type = 'OFFERED'
          AND u.id != :myId
          AND us.skill_id IN (
              SELECT skill_id
              FROM user_skills
              WHERE user_id = :myId
                AND type = 'WANTED'
          )
        """, nativeQuery = true)
    List<MatchCandidateProjection> findCandidatesWhoTeachMe(@Param("myId") UUID myId);


    // ─────────────────────────────────────────────────────────────────────────
    // TODO 2 — Requête de scoring : intersection complète
    //
    // Pars de la requête du TODO 1, et modifie-la pour :
    //
    //   a) Ajouter dans le SELECT :
    //      - COUNT des skills que le candidat offre ET que moi je veux   → alias "they_teach_me"
    //      - COUNT des skills que moi j'offre ET que le candidat veut    → alias "i_teach_them"
    //
    //   b) Remplacer le JOIN simple par une double jointure sur user_skills :
    //      - us_them : les skills du candidat
    //      - us_me   : mes skills, jointure sur skill_id = us_them.skill_id ET user_id = :myId
    //
    //   c) Ajouter un GROUP BY sur u.id, u.display_name, u.average_rating
    //
    //   d) Garder le HAVING pour n'inclure que les candidats qui peuvent m'apprendre quelque chose
    //      (they_teach_me > 0)
    //
    // Le résultat est mappé sur MatchCandidateProjection (qui aura les 5 getters après TODO 3/4).
    // ─────────────────────────────────────────────────────────────────────────

    // TODO 2 — écris ici la @Query de scoring
    @Query(value = """
        SELECT
          u.id,
          u.display_name,
          u.average_rating,
          u.bio,
          u.avatar_url,
          COUNT(CASE WHEN us_them.type = 'OFFERED' AND us_me.type = 'WANTED' THEN 1 END) AS they_teach_me,
          COUNT(CASE WHEN us_me.type = 'OFFERED' AND us_them.type = 'WANTED' THEN 1 END) AS i_teach_them
        FROM users u
        JOIN user_skills us_them ON us_them.user_id = u.id
        JOIN user_skills us_me   ON us_me.skill_id = us_them.skill_id
                    AND us_me.user_id = :myId
        WHERE u.id != :myId
        GROUP BY u.id, u.display_name, u.average_rating, u.bio, u.avatar_url
        HAVING COUNT(CASE WHEN us_them.type = 'OFFERED' AND us_me.type = 'WANTED' THEN 1 END) > 0
        """, nativeQuery = true)
    List<MatchCandidateProjection> findCandidatesWithScore(@Param("myId") UUID myId);
}

-- V5 : ajoute les colonnes pour la photo de profil et la bannière
ALTER TABLE users ADD COLUMN avatar_url VARCHAR(500);
ALTER TABLE users ADD COLUMN banner_url  VARCHAR(500);

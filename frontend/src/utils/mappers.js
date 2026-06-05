// ─────────────────────────────────────────────────────────────────────────────
// mappers.js — fonctions de transformation entre les données API et l'UI
//
// L'API retourne des objets avec certains champs (ex: displayName, averageRating)
// mais les composants React attendent des formes différentes.
// Ces fonctions font la conversion.
//
// 4 exercices sont cachés ici (2, 3, 4, 5) — cherche les TODO !
// ─────────────────────────────────────────────────────────────────────────────

// Dégradés de couleur pour les avatars (générés depuis l'ID utilisateur)
const AVATAR_GRADIENTS = [
  'from-orange-400 to-pink-500',
  'from-blue-400 to-purple-500',
  'from-green-400 to-teal-500',
  'from-yellow-400 to-orange-500',
  'from-pink-400 to-red-500',
  'from-indigo-400 to-blue-500',
]

// Génère une couleur d'avatar déterministe à partir de l'ID
function avatarBgFromId(id = '') {
  const n = parseInt(id.replace(/-/g, '').slice(-2), 16)
  return AVATAR_GRADIENTS[n % AVATAR_GRADIENTS.length]
}

// ─────────────────────────────────────────────────────────────────────────────
// EXERCICE 2 — Adapte un utilisateur API au format attendu par SwipeCard
//
// Ce que l'API retourne (UserResponseDto) :
//   { id, displayName, email, bio, averageRating, createdAt }
//
// Ce que SwipeCard attend :
//   { id, displayName, email, bio, averageRating,
//     avatarBg, avatarInitials, teachSkills, learnSkills }
//
// Hints :
//   - avatarBg     → utilise avatarBgFromId(apiUser.id)
//   - avatarInitials → premiere lettre de displayName en majuscule
//                      apiUser.displayName?.charAt(0).toUpperCase()
//   - teachSkills  → [] pour l'instant (l'API ne les envoie pas dans ce endpoint)
//   - learnSkills  → []
//
// Quand c'est bon, DiscoverPage affichera les vrais utilisateurs de la DB !
// ─────────────────────────────────────────────────────────────────────────────
export function mapApiUserToCard(apiUser) {
  // TODO Exercice 2 — complète ce return
  // return {
  //   id:              ...,
  //   displayName:     ...,
  //   email:           ...,
  //   bio:             ...,
  //   averageRating:   ...,
  //   avatarBg:        avatarBgFromId(apiUser.id),
  //   avatarInitials:  ...,
  //   teachSkills:     [],
  //   learnSkills:     [],
  // }
  return {
    id: apiUser.id,
    displayName: apiUser.displayName,
    email: apiUser.email,
    bio: apiUser.bio,
    averageRating: apiUser.averageRating,
    avatarBg: avatarBgFromId(apiUser.id),
    avatarInitials: apiUser.displayName?.charAt(0).toUpperCase(),
    teachSkills: [],
    learnSkills: [],
  }
}

// ─────────────────────────────────────────────────────────────────────────────
// EXERCICE 3 — Sépare les sessions en "à venir" et "historique"
//
// L'API retourne un tableau de sessions avec un champ status :
//   'SCHEDULED'  → session planifiée (à venir)
//   'COMPLETED'  → session terminée
//   'CANCELLED'  → session annulée
//
// Tu dois retourner un objet avec deux tableaux :
//   { upcoming: [...], history: [...] }
//
// Hints :
//   - upcoming  → sessions où status === 'SCHEDULED'
//   - history   → sessions où status !== 'SCHEDULED'
//   - Utilise array.filter(s => ...)
//
// Exemple :
//   splitSessions([{status:'SCHEDULED'}, {status:'COMPLETED'}])
//   → { upcoming: [{status:'SCHEDULED'}], history: [{status:'COMPLETED'}] }
// ─────────────────────────────────────────────────────────────────────────────
export function splitSessions(sessions) {
  // TODO Exercice 3 — complète
  const upcoming = sessions.filter(s => s.status === 'SCHEDULED')
  const history  = sessions.filter(s => s.status !== 'SCHEDULED')
  return { upcoming, history }
}

// ─────────────────────────────────────────────────────────────────────────────
// EXERCICE 4 — Transforme la liste de skills API au format de l'UI
//
// L'API retourne (UserSkillDto) :
//   [{ id, skillId, skillName, type: 'OFFERED' | 'WANTED', level }]
//
// L'UI (ProfilePage) attend :
//   { teachSkills: ['Java', 'Python'], learnSkills: ['Guitar'] }
//
// Hints :
//   - teachSkills → skills où type === 'OFFERED'  → extrait juste le .skillName
//   - learnSkills → skills où type === 'WANTED'   → extrait juste le .skillName
//   - Utilise array.filter(...).map(s => s.skillName)
// ─────────────────────────────────────────────────────────────────────────────
export function mapSkillsToUI(apiSkills) {
  // TODO Exercice 4 — complète
  const teachSkills = apiSkills.filter(s => s.type === 'OFFERED').map(s => s.skillName)
  const learnSkills = apiSkills.filter(s => s.type === 'WANTED').map(s => s.skillName)
  return { teachSkills, learnSkills }
}

// ─────────────────────────────────────────────────────────────────────────────
// EXERCICE 5 — Extrait les noms de skills d'une demande d'échange
//
// L'API retourne (SessionRequestResponseDto) :
//   {
//     id, initiator: { displayName, ... }, receiver: { ... },
//     offeredSkill: { id, name, category },   ← objet imbriqué
//     wantedSkill:  { id, name, category },   ← objet imbriqué
//     status, message, createdAt
//   }
//
// RequestCard attend :
//   { ...request, offeredSkill: 'Java', wantedSkill: 'Guitar' }
//   (des strings, pas des objets)
//
// Hints :
//   - offeredSkill du résultat = request.offeredSkill.name
//   - wantedSkill du résultat  = request.wantedSkill.name
//   - Utilise le spread ...request pour garder tous les autres champs
// ─────────────────────────────────────────────────────────────────────────────
export function mapRequestToCard(request) {
  // TODO Exercice 5 — complète
    return {
    ...request,
    offeredSkill: request.offeredSkill.name,
    wantedSkill: request.wantedSkill.name,
  }

}

// ─────────────────────────────────────────────────────────────────────────────
// Adapte un MatchCandidateDto au format attendu par SwipeCard.
//
// Le backend retourne aujourd'hui :
//   { userId, displayName, averageRating, theyTeachMe, iTeachThem, score }
//
// Les champs manquants (bio, teachSkills, learnSkills) seront ajoutés
// au MatchCandidateDto côté backend quand on enrichira le moteur —
// les ?? null / ?? [] ici les prendront automatiquement sans modifier ce fichier.
// ─────────────────────────────────────────────────────────────────────────────
export function mapCandidateToCard(candidate) {
  return {
    id:             candidate.userId,
    displayName:    candidate.displayName,
    bio:            candidate.bio ?? null,
    averageRating:  candidate.averageRating,
    avatarUrl:      candidate.avatarUrl ?? null,
    avatarBg:       avatarBgFromId(candidate.userId),
    avatarInitials: candidate.displayName?.charAt(0).toUpperCase(),
    teachSkills:    candidate.teachSkills ?? [],
    learnSkills:    candidate.learnSkills ?? [],
    score:          candidate.score,
    theyTeachMe:    candidate.theyTeachMe,
    iTeachThem:     candidate.iTeachThem,
  }
}

// ─────────────────────────────────────────────────────────────────────────────
// Fonction fournie (pas un exercice)
// Adapte une session API au format de SessionCard
// currentUser = l'utilisateur connecté (pour trouver "l'autre" participant)
// ─────────────────────────────────────────────────────────────────────────────
export function mapApiSessionToCard(session, currentUser) {
  const { request, scheduledAt, mode, status, location } = session
  // "otherUser" = l'autre participant (pas nous)
  const otherUser = request.initiator.id === currentUser.id
    ? request.receiver
    : request.initiator
  // Enrichit otherUser avec les champs visuels
  const otherUserWithAvatar = {
    ...otherUser,
    avatarBg: avatarBgFromId(otherUser.id),
    avatarInitials: otherUser.displayName?.charAt(0).toUpperCase(),
  }
  return {
    id:           session.id,
    otherUser:    otherUserWithAvatar,
    offeredSkill: request.offeredSkill.name,
    wantedSkill:  request.wantedSkill.name,
    scheduledAt,
    mode,
    status,
    location,
    rated: false,
  }
}

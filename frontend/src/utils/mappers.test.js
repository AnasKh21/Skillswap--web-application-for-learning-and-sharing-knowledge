import { describe, it, expect } from 'vitest'
import { mapApiUserToCard, splitSessions, mapSkillsToUI, mapRequestToCard } from './mappers'

// ─────────────────────────────────────────────────────────────────────────────
// Tests pour les exercices 2, 3, 4, 5
// Lance avec : npm test
//
// Un test "passe" (✅) quand ton code retourne exactement ce qui est attendu.
// Un test "échoue" (❌) quand ton code throw ou retourne autre chose.
// ─────────────────────────────────────────────────────────────────────────────

// ── Exercice 2 : mapApiUserToCard ─────────────────────────────────────────────
describe('Exercice 2 — mapApiUserToCard', () => {
  const apiUser = {
    id: 'a0000000-0000-0000-0000-000000000001',
    displayName: 'Alice',
    email: 'alice@test.com',
    bio: 'Je code en Java',
    averageRating: 4.5,
    createdAt: '2026-06-04T00:00:00Z',
  }

  it('conserve id, displayName, email, bio, averageRating', () => {
    const result = mapApiUserToCard(apiUser)
    expect(result.id).toBe(apiUser.id)
    expect(result.displayName).toBe('Alice')
    expect(result.email).toBe('alice@test.com')
    expect(result.bio).toBe('Je code en Java')
    expect(result.averageRating).toBe(4.5)
  })

  it('génère avatarInitials = première lettre du displayName en majuscule', () => {
    const result = mapApiUserToCard(apiUser)
    expect(result.avatarInitials).toBe('A')
  })

  it('génère avatarBg (string non vide)', () => {
    const result = mapApiUserToCard(apiUser)
    expect(typeof result.avatarBg).toBe('string')
    expect(result.avatarBg.length).toBeGreaterThan(0)
  })

  it('teachSkills et learnSkills sont des tableaux vides', () => {
    const result = mapApiUserToCard(apiUser)
    expect(Array.isArray(result.teachSkills)).toBe(true)
    expect(Array.isArray(result.learnSkills)).toBe(true)
    expect(result.teachSkills).toHaveLength(0)
    expect(result.learnSkills).toHaveLength(0)
  })
})

// ── Exercice 3 : splitSessions ────────────────────────────────────────────────
describe('Exercice 3 — splitSessions', () => {
  const sessions = [
    { id: '1', status: 'SCHEDULED' },
    { id: '2', status: 'COMPLETED' },
    { id: '3', status: 'CANCELLED' },
    { id: '4', status: 'SCHEDULED' },
  ]

  it('upcoming contient uniquement les sessions SCHEDULED', () => {
    const { upcoming } = splitSessions(sessions)
    expect(upcoming).toHaveLength(2)
    expect(upcoming.every(s => s.status === 'SCHEDULED')).toBe(true)
  })

  it('history contient COMPLETED et CANCELLED', () => {
    const { history } = splitSessions(sessions)
    expect(history).toHaveLength(2)
    expect(history.map(s => s.status)).toContain('COMPLETED')
    expect(history.map(s => s.status)).toContain('CANCELLED')
  })

  it('retourne { upcoming, history }', () => {
    const result = splitSessions(sessions)
    expect(result).toHaveProperty('upcoming')
    expect(result).toHaveProperty('history')
  })

  it('fonctionne avec un tableau vide', () => {
    const result = splitSessions([])
    expect(result.upcoming).toHaveLength(0)
    expect(result.history).toHaveLength(0)
  })
})

// ── Exercice 4 : mapSkillsToUI ────────────────────────────────────────────────
describe('Exercice 4 — mapSkillsToUI', () => {
  const apiSkills = [
    { id: '1', skillId: 'sk1', skillName: 'Java',   type: 'OFFERED', level: 5 },
    { id: '2', skillId: 'sk2', skillName: 'Python',  type: 'OFFERED', level: 3 },
    { id: '3', skillId: 'sk3', skillName: 'Guitar',  type: 'WANTED',  level: 1 },
  ]

  it('teachSkills contient les noms des skills OFFERED', () => {
    const { teachSkills } = mapSkillsToUI(apiSkills)
    expect(teachSkills).toContain('Java')
    expect(teachSkills).toContain('Python')
    expect(teachSkills).toHaveLength(2)
  })

  it('learnSkills contient les noms des skills WANTED', () => {
    const { learnSkills } = mapSkillsToUI(apiSkills)
    expect(learnSkills).toContain('Guitar')
    expect(learnSkills).toHaveLength(1)
  })

  it('retourne { teachSkills, learnSkills }', () => {
    const result = mapSkillsToUI(apiSkills)
    expect(result).toHaveProperty('teachSkills')
    expect(result).toHaveProperty('learnSkills')
  })
})

// ── Exercice 5 : mapRequestToCard ─────────────────────────────────────────────
describe('Exercice 5 — mapRequestToCard', () => {
  const apiRequest = {
    id: 'req-1',
    initiator: { id: 'u1', displayName: 'Alice' },
    receiver:  { id: 'u2', displayName: 'Bob' },
    offeredSkill: { id: 'sk1', name: 'Java', category: 'Programming' },
    wantedSkill:  { id: 'sk2', name: 'Guitar', category: 'Music' },
    status: 'PENDING',
    message: 'Hey!',
    createdAt: '2026-06-04T00:00:00Z',
  }

  it('offeredSkill devient une string (le nom)', () => {
    const result = mapRequestToCard(apiRequest)
    expect(result.offeredSkill).toBe('Java')
    expect(typeof result.offeredSkill).toBe('string')
  })

  it('wantedSkill devient une string (le nom)', () => {
    const result = mapRequestToCard(apiRequest)
    expect(result.wantedSkill).toBe('Guitar')
    expect(typeof result.wantedSkill).toBe('string')
  })

  it('conserve tous les autres champs (id, initiator, status, message)', () => {
    const result = mapRequestToCard(apiRequest)
    expect(result.id).toBe('req-1')
    expect(result.initiator.displayName).toBe('Alice')
    expect(result.status).toBe('PENDING')
    expect(result.message).toBe('Hey!')
  })
})

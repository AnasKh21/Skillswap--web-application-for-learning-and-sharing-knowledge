#!/bin/bash
# seed.sh — popule la base avec des skills et des utilisateurs fictifs
# Usage : bash seed.sh

API="http://localhost:8080"
GREEN='\033[0;32m'; YELLOW='\033[1;33m'; RED='\033[0;31m'; NC='\033[0m'
# >&2 = stderr, jamais capturé par TOKEN=$(login ...)
ok()   { echo -e "${GREEN}✓ $1${NC}" >&2; }
warn() { echo -e "${YELLOW}⚠ $1${NC}" >&2; }
fail() { echo -e "${RED}✗ $1${NC}" >&2; }

# ── Crée un skill (pas d'auth requise), ignore si déjà existant ───────────────
create_skill() {
  local name="$1" category="$2"
  local status
  status=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$API/api/skills" \
    -H "Content-Type: application/json" \
    -d "{\"name\":\"$name\",\"category\":\"$category\"}")
  case "$status" in
    201) ok "Skill : $name ($category)" ;;
    409) warn "Déjà existant : $name" ;;
    *)   fail "Erreur skill $name : HTTP $status" ;;
  esac
}

# ── Récupère l'id d'un skill par son nom ──────────────────────────────────────
skill_id() {
  local name="$1"
  curl -s "$API/api/skills" \
    | python3 -c "import sys,json; skills=json.load(sys.stdin); print(next((s['id'] for s in skills if s['name']=='$name'), ''))"
}

# ── Register + login, retourne le token JWT extrait du cookie ─────────────────
login() {
  local email="$1" password="$2" name="$3"
  # Register (ignore si déjà existant)
  curl -s -o /dev/null -X POST "$API/api/auth/register" \
    -H "Content-Type: application/json" \
    -d "{\"email\":\"$email\",\"password\":\"$password\",\"displayName\":\"$name\"}"

  # Login → extrait access_token depuis le header Set-Cookie
  local token
  token=$(curl -s -D - -X POST "$API/api/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"email\":\"$email\",\"password\":\"$password\"}" \
    | grep -i "set-cookie: access_token" \
    | grep -o "access_token=[^;]*" \
    | cut -d= -f2 | tr -d '[:space:]')

  if [ -n "$token" ]; then
    ok "Connecté : $name ($email)"
    echo "$token"
  else
    fail "Login échoué pour $name"
    echo ""
  fi
}

# ── Ajoute un skill à l'utilisateur via son token JWT ────────────────────────
add_skill() {
  local token="$1" skill_name="$2" type="$3" level="$4"
  local id
  id=$(skill_id "$skill_name")
  if [ -z "$id" ]; then
    fail "  Skill introuvable : $skill_name"
    return 1
  fi
  local status
  status=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$API/api/users/me/skills" \
    -H "Content-Type: application/json" \
    -H "Cookie: access_token=$token" \
    -d "{\"skillId\":\"$id\",\"type\":\"$type\",\"level\":$level}")
  case "$status" in
    201) ok "  $type $skill_name (niveau $level)" ;;
    400) warn "  Déjà ajouté : $skill_name" ;;
    *)   fail "  Erreur $skill_name : HTTP $status" ;;
  esac
}

# ═══════════════════════════════════════════════════════════════
# 1. SKILLS
# ═══════════════════════════════════════════════════════════════
echo ""
echo "━━━ Création des skills ━━━"
create_skill "Java"         "Programming"
create_skill "Python"       "Programming"
create_skill "React"        "Programming"
create_skill "TypeScript"   "Programming"
create_skill "Spring Boot"  "Programming"
create_skill "SQL"          "Programming"
create_skill "Guitar"       "Music"
create_skill "Piano"        "Music"
create_skill "Drums"        "Music"
create_skill "Spanish"      "Language"
create_skill "French"       "Language"
create_skill "Arabic"       "Language"
create_skill "Photography"  "Art"
create_skill "Watercolor"   "Art"
create_skill "Illustration" "Art"
create_skill "Yoga"         "Sport"
create_skill "Cooking"      "Lifestyle"
create_skill "3D Modeling"  "Design"

# ═══════════════════════════════════════════════════════════════
# 2. UTILISATEURS + SKILLS
# ═══════════════════════════════════════════════════════════════

echo ""
echo "━━━ Alice — offre Java/Python, veut Guitar/Spanish ━━━"
TOKEN=$(login "alice@test.com" "password123" "Alice")
[ -n "$TOKEN" ] && {
  add_skill "$TOKEN" "Java"    "OFFERED" 4
  add_skill "$TOKEN" "Python"  "OFFERED" 3
  add_skill "$TOKEN" "Guitar"  "WANTED"  2
  add_skill "$TOKEN" "Spanish" "WANTED"  1
}

echo ""
echo "━━━ Bob — offre Guitar/French, veut Java/Python (match parfait Alice) ━━━"
TOKEN=$(login "bob@test.com" "password123" "Bob")
[ -n "$TOKEN" ] && {
  add_skill "$TOKEN" "Guitar" "OFFERED" 5
  add_skill "$TOKEN" "French" "OFFERED" 4
  add_skill "$TOKEN" "Java"   "WANTED"  3
  add_skill "$TOKEN" "Python" "WANTED"  2
}

echo ""
echo "━━━ Carol — offre Spanish/Photography, veut Python (match partiel Alice) ━━━"
TOKEN=$(login "carol@test.com" "password123" "Carol")
[ -n "$TOKEN" ] && {
  add_skill "$TOKEN" "Spanish"     "OFFERED" 5
  add_skill "$TOKEN" "Photography" "OFFERED" 3
  add_skill "$TOKEN" "Python"      "WANTED"  2
}

echo ""
echo "━━━ David — offre React/TypeScript, veut Java (unilatéral Alice→David) ━━━"
TOKEN=$(login "david@test.com" "password123" "David")
[ -n "$TOKEN" ] && {
  add_skill "$TOKEN" "React"      "OFFERED" 4
  add_skill "$TOKEN" "TypeScript" "OFFERED" 3
  add_skill "$TOKEN" "Java"       "WANTED"  2
}

echo ""
echo "━━━ Emma — offre Yoga/Cooking, veut Photography (aucun match avec Alice) ━━━"
TOKEN=$(login "emma@test.com" "password123" "Emma")
[ -n "$TOKEN" ] && {
  add_skill "$TOKEN" "Yoga"        "OFFERED" 5
  add_skill "$TOKEN" "Cooking"     "OFFERED" 4
  add_skill "$TOKEN" "Photography" "WANTED"  1
}

echo ""
echo "━━━ Farid — offre SQL/Spring Boot/Java, veut Guitar/Piano ━━━"
TOKEN=$(login "farid@test.com" "password123" "Farid")
[ -n "$TOKEN" ] && {
  add_skill "$TOKEN" "SQL"         "OFFERED" 5
  add_skill "$TOKEN" "Spring Boot" "OFFERED" 4
  add_skill "$TOKEN" "Java"        "OFFERED" 3
  add_skill "$TOKEN" "Guitar"      "WANTED"  1
  add_skill "$TOKEN" "Piano"       "WANTED"  1
}

# ═══════════════════════════════════════════════════════════════
# 3. RÉSULTAT ATTENDU pour Alice connectée
# ═══════════════════════════════════════════════════════════════
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo " Résultat attendu : GET /api/matching/candidates (Alice)"
echo ""
echo "  1. Bob   — theyTeachMe=2, iTeachThem=2  → score élevé (échange mutuel)"
echo "  2. Farid — theyTeachMe=1, iTeachThem=2  → score moyen-haut"
echo "  3. Carol — theyTeachMe=1, iTeachThem=1  → score moyen"
echo "  4. David — theyTeachMe=0, iTeachThem=1  → score bas (unilatéral)"
echo "  Emma n'apparaît PAS (aucun skill en commun)"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

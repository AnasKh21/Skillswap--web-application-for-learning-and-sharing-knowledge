# SkillSwap — Frontend

React + Vite + Tailwind CSS frontend for the SkillSwap platform.  
Tinder-inspired skill matching with swipeable profile cards.

## Stack

| Tool | Purpose |
|---|---|
| React 18 | UI framework |
| Vite 5 | Build tool / dev server |
| Tailwind CSS 3 | Utility-first styling |
| Framer Motion | Swipe animations & transitions |
| React Router v6 | Client-side routing |
| Lucide React | Icon library |
| Axios | HTTP client |

## Color System

| Token | Hex | Usage |
|---|---|---|
| `primary` | `#FF6B35` | CTAs, active nav, skill tags |
| `danger` | `#E63946` | Pass action, decline button |
| `accent` | `#FFB703` | Stars, badges, highlights |
| `surface` | `#FFFBF5` | App background |
| `dark` | `#1A1A2E` | Body text |

## Getting started

```bash
cd frontend
cp .env.example .env
npm install
npm run dev        # → http://localhost:3000
```

The dev server proxies `/api` to `http://localhost:8080` (Spring Boot).

## Project structure

```
src/
├── api/            # Axios calls (connects to Spring Boot REST controllers)
├── components/
│   ├── cards/      # SwipeCard, RequestCard, SessionCard
│   ├── layout/     # Layout, Sidebar, BottomNav
│   └── ui/         # Button, Badge, Avatar, StarRating, SkillTag
├── context/        # AuthContext (JWT-ready, mock for now)
├── data/           # mockData.js — realistic sample data
└── pages/          # AuthPage, DiscoverPage, RequestsPage, SessionsPage, ProfilePage
uml/                # PlantUML diagrams (components, navigation, sequences)
```

---

## UML Diagrams (Mermaid)

### Component Architecture

```mermaid
graph TD
  main["main.jsx"] --> App
  App --> AuthContext
  App --> Layout
  App --> AuthPage

  Layout --> Sidebar
  Layout --> BottomNav
  Layout --> Pages

  subgraph Pages
    DiscoverPage
    RequestsPage
    SessionsPage
    ProfilePage
  end

  DiscoverPage --> SwipeCard
  RequestsPage --> RequestCard
  SessionsPage --> SessionCard
  SessionsPage --> RatingModal

  SwipeCard --> Avatar
  RequestCard --> Avatar
  SessionCard --> Avatar

  subgraph UI["UI Components"]
    Button
    Badge
    Avatar
    StarRating
    SkillTag
  end

  style DiscoverPage fill:#FF6B35,color:#fff
  style SwipeCard fill:#FFB703,color:#1A1A2E
```

---

### Page Navigation Flow

```mermaid
stateDiagram-v2
  [*] --> AuthPage : First visit (not logged in)

  AuthPage --> DiscoverPage : Sign in / Register

  state Authenticated {
    DiscoverPage --> RequestsPage : Bottom nav / Sidebar
    DiscoverPage --> SessionsPage : Bottom nav / Sidebar
    DiscoverPage --> ProfilePage  : Bottom nav / Sidebar

    RequestsPage --> SessionsPage : Accept request → session created
    SessionsPage --> SessionsPage : Rate modal (overlay)
  }

  Authenticated --> AuthPage : Logout
```

---

### Sequence: Swipe Right → Session Request

```mermaid
sequenceDiagram
  actor UserA as User A (Swiper)
  participant Discover as DiscoverPage
  participant Card as SwipeCard
  participant API as api/sessions.js
  participant Backend as Spring Boot
  actor UserB as User B (Receiver)

  UserA->>Card: Drags card right (x > 120px)
  Card->>Discover: onLike(user)
  Discover->>API: sendRequest(initiatorId, receiverId, skills, message)
  API->>Backend: POST /api/sessions/requests
  Backend-->>API: 201 Created { requestId }
  API-->>Discover: { id, status: PENDING }
  Discover-->>Discover: Card flies off screen (animation)

  UserB->>Backend: GET /api/sessions/requests/pending/{userId}
  Backend-->>UserB: List of pending requests

  UserB->>Backend: POST /api/sessions/requests/{id}/accept
  Backend-->>UserB: Session created { sessionId, status: SCHEDULED }

  Note over UserA,UserB: Both see the session in SessionsPage
```

---

### Swipe Card State Machine

```mermaid
stateDiagram-v2
  [*] --> Idle : Card rendered

  Idle --> Dragging : mousedown / touchstart

  Dragging --> LikeZone  : x > +120px
  Dragging --> PassZone  : x < -120px
  Dragging --> Idle      : release (x within threshold) → spring back

  LikeZone --> FlyRight  : mouse/touch release
  PassZone --> FlyLeft   : mouse/touch release

  FlyRight --> [*] : onLike(user) — next card shown
  FlyLeft  --> [*] : onPass(user) — next card shown
```

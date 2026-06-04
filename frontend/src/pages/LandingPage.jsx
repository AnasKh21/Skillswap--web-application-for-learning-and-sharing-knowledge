import { useRef } from 'react'
import { Link } from 'react-router-dom'
import { motion, useInView } from 'framer-motion'
import { ArrowRight, Users, Zap, RefreshCw, Star, BookOpen, Lightbulb } from 'lucide-react'

// Animation variants réutilisables
const fadeUp = {
  hidden:  { opacity: 0, y: 40 },
  visible: { opacity: 1, y: 0, transition: { duration: 0.6, ease: 'easeOut' } },
}

const stagger = {
  visible: { transition: { staggerChildren: 0.15 } },
}

function Section({ children, className = '' }) {
  const ref  = useRef(null)
  const inView = useInView(ref, { once: true, margin: '-80px' })
  return (
    <motion.section
      ref={ref}
      variants={stagger}
      initial="hidden"
      animate={inView ? 'visible' : 'hidden'}
      className={className}
    >
      {children}
    </motion.section>
  )
}

// ── Données ──────────────────────────────────────────────────────────────────

const STEPS = [
  {
    number: '01',
    title: 'Build your profile',
    desc: 'List what you can teach and what you want to learn. No résumé needed — just your real skills.',
    icon: BookOpen,
  },
  {
    number: '02',
    title: 'Discover and connect',
    desc: 'Browse people whose skills complement yours. When there is a mutual match, reach out.',
    icon: Users,
  },
  {
    number: '03',
    title: 'Exchange and grow',
    desc: 'Schedule a session, share what you know, and walk away knowing more than before.',
    icon: RefreshCw,
  },
]

const FEATURES = [
  {
    icon: Zap,
    title: 'Bilateral matching',
    desc: 'The algorithm finds people where each person has what the other wants. Both sides win.',
  },
  {
    icon: Star,
    title: 'Reputation system',
    desc: 'Every session ends with a rating. Quality teachers rise to the top naturally.',
  },
  {
    icon: Lightbulb,
    title: 'Any skill counts',
    desc: 'Code, guitar, language, cooking, photography. If you can teach it, someone wants to learn it.',
  },
]

const TESTIMONIALS = [
  {
    quote: 'I traded three Python lessons for six guitar sessions. Best deal I ever made.',
    name: 'Marcus L.',
    role: 'Software engineer',
    rating: 5,
  },
  {
    quote: 'Found a Spanish tutor who needed help with their CV. We have been meeting weekly for four months.',
    name: 'Priya S.',
    role: 'Product designer',
    rating: 5,
  },
  {
    quote: 'The quality of teaching is better than any course I paid for. People here actually care.',
    name: 'Thomas R.',
    role: 'Freelance photographer',
    rating: 5,
  },
]

// ── Composants ───────────────────────────────────────────────────────────────

function Navbar() {
  return (
    <nav className="fixed top-0 left-0 right-0 z-50 flex items-center justify-between px-6 md:px-12 py-4 bg-surface/80 backdrop-blur-md border-b border-orange-100">
      <div className="flex items-center gap-2">
        <div className="w-8 h-8 rounded-lg bg-gradient-brand flex items-center justify-center">
          <Zap size={16} className="text-white fill-white" />
        </div>
        <span className="font-black text-dark text-lg tracking-tight">
          Skill<span className="text-primary">Swap</span>
        </span>
      </div>
      <div className="flex items-center gap-3">
        <Link
          to="/auth"
          className="text-sm font-semibold text-muted hover:text-dark transition-colors px-4 py-2"
        >
          Sign in
        </Link>
        <Link
          to="/auth"
          className="text-sm font-semibold text-white bg-gradient-brand px-5 py-2 rounded-full shadow-card hover:shadow-card-hover hover:scale-105 transition-all"
        >
          Get started
        </Link>
      </div>
    </nav>
  )
}

function Hero() {
  return (
    <section className="relative min-h-screen flex items-center justify-center overflow-hidden pt-20">
      {/* Background orbs */}
      <div className="absolute top-1/4 left-1/4 w-96 h-96 bg-primary/10 rounded-full blur-3xl pointer-events-none" />
      <div className="absolute bottom-1/4 right-1/4 w-80 h-80 bg-accent/10 rounded-full blur-3xl pointer-events-none" />

      <div className="relative z-10 text-center px-6 max-w-4xl mx-auto">
        <motion.div
          initial={{ opacity: 0, y: -20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5 }}
          className="inline-flex items-center gap-2 px-4 py-1.5 rounded-full border border-orange-200 bg-orange-50 text-primary text-xs font-bold uppercase tracking-widest mb-8"
        >
          <Users size={12} /> Human to human
        </motion.div>

        <motion.h1
          initial={{ opacity: 0, y: 30 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.7, delay: 0.1 }}
          className="text-5xl md:text-7xl font-black text-dark leading-[1.05] tracking-tight mb-6"
        >
          Trade what you know.
          <br />
          <span className="bg-gradient-brand bg-clip-text text-transparent">
            Learn what you don't.
          </span>
        </motion.h1>

        <motion.p
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, delay: 0.25 }}
          className="text-lg md:text-xl text-muted max-w-2xl mx-auto leading-relaxed mb-10"
        >
          SkillSwap connects people who have something to teach with people
          who have something to offer in return. No money. No platform fees.
          Just humans sharing knowledge with each other.
        </motion.p>

        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, delay: 0.4 }}
          className="flex flex-col sm:flex-row gap-4 justify-center"
        >
          <Link
            to="/auth"
            className="inline-flex items-center justify-center gap-2 px-8 py-4 rounded-full bg-gradient-brand text-white font-bold text-base shadow-card hover:shadow-card-hover hover:scale-105 transition-all"
          >
            Create your account <ArrowRight size={18} />
          </Link>
          <Link
            to="/auth"
            className="inline-flex items-center justify-center gap-2 px-8 py-4 rounded-full border-2 border-orange-200 text-dark font-bold text-base hover:border-primary hover:text-primary transition-all"
          >
            Sign in
          </Link>
        </motion.div>

        {/* Social proof strip */}
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ duration: 0.8, delay: 0.7 }}
          className="mt-14 flex items-center justify-center gap-6 text-sm text-muted"
        >
          <span className="flex items-center gap-1.5">
            <span className="w-2 h-2 rounded-full bg-green-400 inline-block" />
            Free forever
          </span>
          <span className="text-orange-200">|</span>
          <span>No credit card needed</span>
          <span className="text-orange-200">|</span>
          <span>Any skill accepted</span>
        </motion.div>
      </div>
    </section>
  )
}

function HowItWorks() {
  return (
    <Section className="py-24 px-6 max-w-6xl mx-auto">
      <motion.div variants={fadeUp} className="text-center mb-16">
        <p className="text-primary font-bold uppercase tracking-widest text-xs mb-3">How it works</p>
        <h2 className="text-3xl md:text-5xl font-black text-dark">
          Three steps to your first exchange
        </h2>
      </motion.div>

      <div className="grid md:grid-cols-3 gap-8">
        {STEPS.map((step) => (
          <motion.div
            key={step.number}
            variants={fadeUp}
            className="relative bg-white border border-orange-100 rounded-3xl p-8 shadow-sm hover:shadow-card transition-shadow group"
          >
            <div className="absolute top-6 right-6 text-4xl font-black text-orange-100 group-hover:text-orange-200 transition-colors select-none">
              {step.number}
            </div>
            <div className="w-12 h-12 rounded-2xl bg-gradient-brand flex items-center justify-center mb-5 shadow-card">
              <step.icon size={22} className="text-white" />
            </div>
            <h3 className="text-xl font-bold text-dark mb-3">{step.title}</h3>
            <p className="text-muted text-sm leading-relaxed">{step.desc}</p>
          </motion.div>
        ))}
      </div>
    </Section>
  )
}

function Features() {
  return (
    <Section className="py-24 px-6 bg-dark">
      <div className="max-w-6xl mx-auto">
        <motion.div variants={fadeUp} className="text-center mb-16">
          <p className="text-primary font-bold uppercase tracking-widest text-xs mb-3">Why SkillSwap</p>
          <h2 className="text-3xl md:text-5xl font-black text-white">
            Built for real exchanges,<br />not transactions
          </h2>
        </motion.div>

        <div className="grid md:grid-cols-3 gap-6">
          {FEATURES.map((f) => (
            <motion.div
              key={f.title}
              variants={fadeUp}
              className="bg-white/5 border border-white/10 rounded-3xl p-8 hover:bg-white/8 transition-colors"
            >
              <div className="w-11 h-11 rounded-xl bg-gradient-brand flex items-center justify-center mb-5">
                <f.icon size={20} className="text-white" />
              </div>
              <h3 className="text-lg font-bold text-white mb-2">{f.title}</h3>
              <p className="text-white/50 text-sm leading-relaxed">{f.desc}</p>
            </motion.div>
          ))}
        </div>
      </div>
    </Section>
  )
}

function Testimonials() {
  return (
    <Section className="py-24 px-6 max-w-6xl mx-auto">
      <motion.div variants={fadeUp} className="text-center mb-16">
        <p className="text-primary font-bold uppercase tracking-widest text-xs mb-3">Real exchanges</p>
        <h2 className="text-3xl md:text-5xl font-black text-dark">
          What people are saying
        </h2>
      </motion.div>

      <div className="grid md:grid-cols-3 gap-6">
        {TESTIMONIALS.map((t) => (
          <motion.div
            key={t.name}
            variants={fadeUp}
            className="bg-white border border-orange-100 rounded-3xl p-7 shadow-sm hover:shadow-card transition-shadow"
          >
            <div className="flex gap-0.5 mb-4">
              {Array.from({ length: t.rating }).map((_, i) => (
                <Star key={i} size={14} className="text-accent fill-accent" />
              ))}
            </div>
            <p className="text-dark text-sm leading-relaxed mb-5 font-medium">
              "{t.quote}"
            </p>
            <div className="border-t border-orange-50 pt-4">
              <p className="text-sm font-bold text-dark">{t.name}</p>
              <p className="text-xs text-muted">{t.role}</p>
            </div>
          </motion.div>
        ))}
      </div>
    </Section>
  )
}

function CTA() {
  return (
    <Section className="py-24 px-6">
      <motion.div
        variants={fadeUp}
        className="max-w-3xl mx-auto text-center bg-gradient-hero rounded-3xl p-14 shadow-card-hover relative overflow-hidden"
      >
        {/* Decorative circles */}
        <div className="absolute -top-10 -right-10 w-48 h-48 bg-white/10 rounded-full pointer-events-none" />
        <div className="absolute -bottom-10 -left-10 w-32 h-32 bg-white/10 rounded-full pointer-events-none" />

        <div className="relative z-10">
          <h2 className="text-3xl md:text-5xl font-black text-white mb-4 leading-tight">
            Your next skill is one conversation away
          </h2>
          <p className="text-white/80 mb-10 text-lg">
            Join a community where knowledge is the currency.
          </p>
          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <Link
              to="/auth"
              className="inline-flex items-center justify-center gap-2 px-8 py-4 rounded-full bg-white text-primary font-bold text-base hover:scale-105 transition-transform shadow-lg"
            >
              Create your account <ArrowRight size={18} />
            </Link>
            <Link
              to="/auth"
              className="inline-flex items-center justify-center gap-2 px-8 py-4 rounded-full border-2 border-white/40 text-white font-bold text-base hover:border-white transition-colors"
            >
              Sign in
            </Link>
          </div>
        </div>
      </motion.div>
    </Section>
  )
}

function Footer() {
  return (
    <footer className="py-8 px-6 border-t border-orange-100 text-center">
      <div className="flex items-center justify-center gap-2 mb-2">
        <div className="w-6 h-6 rounded-md bg-gradient-brand flex items-center justify-center">
          <Zap size={12} className="text-white fill-white" />
        </div>
        <span className="font-black text-dark text-sm">
          Skill<span className="text-primary">Swap</span>
        </span>
      </div>
      <p className="text-xs text-muted">Skills are the new currency. Share yours.</p>
    </footer>
  )
}

// ── Page principale ───────────────────────────────────────────────────────────

export default function LandingPage() {
  return (
    <div className="bg-surface min-h-screen">
      <Navbar />
      <Hero />
      <HowItWorks />
      <Features />
      <Testimonials />
      <CTA />
      <Footer />
    </div>
  )
}

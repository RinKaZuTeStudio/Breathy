#!/usr/bin/env python3
"""
Breathy Quit-Smoking App — Complete Project Documentation PDF Generator
Covers: PRD, UI/UX, Architecture, Data Model, Testing, Deployment, Store Listing, Post-Launch
"""
import os, sys, hashlib
from reportlab.lib.pagesizes import A4
from reportlab.lib.units import inch, mm
from reportlab.lib import colors
from reportlab.lib.enums import TA_LEFT, TA_CENTER, TA_JUSTIFY
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle,
    PageBreak, KeepTogether, CondPageBreak, Image
)
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.pdfbase.pdfmetrics import registerFontFamily

# ── Palette (cascade V2) ──
ACCENT       = colors.HexColor('#967edc')
ACCENT_SEC   = colors.HexColor('#64d39c')
TEXT_PRIMARY  = colors.HexColor('#eeeeed')
TEXT_MUTED    = colors.HexColor('#9a9791')
BG_PAGE      = colors.HexColor('#0b0a09')
BG_SECTION   = colors.HexColor('#22211d')
BG_CARD      = colors.HexColor('#2b2923')
TABLE_STRIPE = colors.HexColor('#181714')
HEADER_FILL  = colors.HexColor('#3b372b')
BORDER_COLOR = colors.HexColor('#5f5a4b')
SUCCESS      = colors.HexColor('#60b97d')
WARNING      = colors.HexColor('#c4a870')
ERROR_C      = colors.HexColor('#c88882')
INFO_C       = colors.HexColor('#718fac')

# ── Fonts ──
pdfmetrics.registerFont(TTFont('LiberSerif', '/usr/share/fonts/truetype/liberation/LiberationSerif-Regular.ttf'))
pdfmetrics.registerFont(TTFont('LiberSerif-Bold', '/usr/share/fonts/truetype/liberation/LiberationSerif-Bold.ttf'))
pdfmetrics.registerFont(TTFont('LiberSans', '/usr/share/fonts/truetype/liberation/LiberationSans-Regular.ttf'))
pdfmetrics.registerFont(TTFont('LiberSans-Bold', '/usr/share/fonts/truetype/liberation/LiberationSans-Bold.ttf'))
pdfmetrics.registerFont(TTFont('DejaVuMono', '/usr/share/fonts/truetype/dejavu/DejaVuSansMono.ttf'))
pdfmetrics.registerFont(TTFont('DejaVuMono-Bold', '/usr/share/fonts/truetype/dejavu/DejaVuSansMono-Bold.ttf'))
registerFontFamily('LiberSerif', normal='LiberSerif', bold='LiberSerif-Bold')
registerFontFamily('LiberSans', normal='LiberSans', bold='LiberSans-Bold')
registerFontFamily('DejaVuMono', normal='DejaVuMono', bold='DejaVuMono-Bold')

# ── Page setup ──
PAGE_W, PAGE_H = A4
MARGIN = 0.85 * inch
AVAIL_W = PAGE_W - 2 * MARGIN

# ── Styles ──
styles = getSampleStyleSheet()

cover_title = ParagraphStyle('CoverTitle', fontName='LiberSerif', fontSize=38, leading=46,
    textColor=TEXT_PRIMARY, alignment=TA_CENTER, spaceAfter=12)
cover_sub = ParagraphStyle('CoverSub', fontName='LiberSans', fontSize=16, leading=22,
    textColor=TEXT_MUTED, alignment=TA_CENTER, spaceAfter=6)
cover_meta = ParagraphStyle('CoverMeta', fontName='LiberSans', fontSize=12, leading=16,
    textColor=ACCENT, alignment=TA_CENTER, spaceAfter=4)

h1 = ParagraphStyle('H1', fontName='LiberSerif', fontSize=22, leading=28,
    textColor=ACCENT, spaceBefore=18, spaceAfter=10)
h2 = ParagraphStyle('H2', fontName='LiberSerif', fontSize=17, leading=22,
    textColor=TEXT_PRIMARY, spaceBefore=14, spaceAfter=8)
h3 = ParagraphStyle('H3', fontName='LiberSerif', fontSize=13, leading=18,
    textColor=ACCENT_SEC, spaceBefore=10, spaceAfter=6)

body = ParagraphStyle('Body', fontName='LiberSerif', fontSize=10.5, leading=17,
    textColor=TEXT_PRIMARY, alignment=TA_JUSTIFY, spaceAfter=6)
body_left = ParagraphStyle('BodyLeft', fontName='LiberSerif', fontSize=10.5, leading=17,
    textColor=TEXT_PRIMARY, alignment=TA_LEFT, spaceAfter=4)
bullet = ParagraphStyle('Bullet', fontName='LiberSerif', fontSize=10.5, leading=17,
    textColor=TEXT_PRIMARY, alignment=TA_LEFT, leftIndent=18, bulletIndent=6, spaceAfter=3)
caption_style = ParagraphStyle('Caption', fontName='LiberSans', fontSize=9, leading=13,
    textColor=TEXT_MUTED, alignment=TA_CENTER, spaceBefore=3, spaceAfter=6)
code_style = ParagraphStyle('Code', fontName='DejaVuMono', fontSize=8.5, leading=12,
    textColor=TEXT_MUTED, alignment=TA_LEFT, leftIndent=12, spaceAfter=4,
    backColor=BG_SECTION, borderPadding=4)

header_cell = ParagraphStyle('HeaderCell', fontName='LiberSerif', fontSize=10, leading=14,
    textColor=colors.white, alignment=TA_CENTER)
cell_style = ParagraphStyle('Cell', fontName='LiberSerif', fontSize=9.5, leading=14,
    textColor=TEXT_PRIMARY, alignment=TA_LEFT)
cell_center = ParagraphStyle('CellCenter', fontName='LiberSerif', fontSize=9.5, leading=14,
    textColor=TEXT_PRIMARY, alignment=TA_CENTER)

toc_h1 = ParagraphStyle('TOCH1', fontName='LiberSerif', fontSize=13, leftIndent=20, leading=20, textColor=TEXT_PRIMARY)
toc_h2 = ParagraphStyle('TOCH2', fontName='LiberSerif', fontSize=11, leftIndent=40, leading=18, textColor=TEXT_MUTED)

# ── Helpers ──
def P(text, style=body):
    return Paragraph(text, style)

def H1(text):
    return P(f'<b>{text}</b>', h1)

def H2(text):
    return P(f'<b>{text}</b>', h2)

def H3(text):
    return P(f'<b>{text}</b>', h3)

def B(text):
    return P(f'<bullet>&bull;</bullet>{text}', bullet)

def make_table(headers, rows, col_widths=None):
    """Create a styled table with palette colors."""
    data = [[P(f'<b>{h}</b>', header_cell) for h in headers]]
    for row in rows:
        data.append([P(str(c), cell_style) if not isinstance(c, Paragraph) else c for c in row])
    if not col_widths:
        n = len(headers)
        col_widths = [AVAIL_W / n] * n
    t = Table(data, colWidths=col_widths, hAlign='CENTER')
    style_cmds = [
        ('BACKGROUND', (0,0), (-1,0), HEADER_FILL),
        ('TEXTCOLOR', (0,0), (-1,0), colors.white),
        ('GRID', (0,0), (-1,-1), 0.5, BORDER_COLOR),
        ('VALIGN', (0,0), (-1,-1), 'MIDDLE'),
        ('LEFTPADDING', (0,0), (-1,-1), 8),
        ('RIGHTPADDING', (0,0), (-1,-1), 8),
        ('TOPPADDING', (0,0), (-1,-1), 5),
        ('BOTTOMPADDING', (0,0), (-1,-1), 5),
    ]
    for i in range(1, len(data)):
        bg = colors.white if i % 2 == 1 else TABLE_STRIPE
        style_cmds.append(('BACKGROUND', (0,i), (-1,i), bg))
    t.setStyle(TableStyle(style_cmds))
    return t

def hr():
    return P('<font color="#5f5a4b">________________________________________________________________________________</font>', body)

# ── Build Document ──
output_path = '/home/z/my-project/download/breathy/docs/Breathy_Complete_Documentation.pdf'
os.makedirs(os.path.dirname(output_path), exist_ok=True)

doc = SimpleDocTemplate(
    output_path, pagesize=A4,
    leftMargin=MARGIN, rightMargin=MARGIN,
    topMargin=MARGIN, bottomMargin=MARGIN
)

story = []

# ════════════════════════════════════════════════════════════════
# COVER PAGE
# ════════════════════════════════════════════════════════════════
story.append(Spacer(1, 120))
story.append(P('<b>Breathy</b>', cover_title))
story.append(Spacer(1, 8))
story.append(P('Complete Project Documentation', cover_sub))
story.append(Spacer(1, 6))
story.append(P('Quit-Smoking Android Application', cover_sub))
story.append(Spacer(1, 30))
story.append(P('From Concept to Production', cover_meta))
story.append(P('PRD | UI/UX | Architecture | Data Model | Testing | Deployment | Store Listing', cover_meta))
story.append(Spacer(1, 20))
story.append(P('Version 1.0 | May 2026', cover_meta))
story.append(P('Package: com.breathy | Platform: Android (SDK 24-34)', cover_meta))
story.append(Spacer(1, 40))
story.append(hr())
story.append(Spacer(1, 10))
story.append(P('Kotlin + Jetpack Compose | Firebase | OpenAI API', ParagraphStyle('CoverTech',
    fontName='LiberSans', fontSize=11, leading=15, textColor=TEXT_MUTED, alignment=TA_CENTER)))
story.append(P('AdMob | Play Billing | CameraX | Konfetti', ParagraphStyle('CoverTech2',
    fontName='LiberSans', fontSize=11, leading=15, textColor=TEXT_MUTED, alignment=TA_CENTER)))

story.append(PageBreak())

# ════════════════════════════════════════════════════════════════
# TABLE OF CONTENTS
# ════════════════════════════════════════════════════════════════
story.append(H1('Table of Contents'))
story.append(Spacer(1, 12))

toc_items = [
    ('1', 'Product Requirements Document (PRD)'),
    ('1.1', 'User Personas'),
    ('1.2', 'Functional Requirements'),
    ('1.3', 'Non-Functional Requirements'),
    ('2', 'UI/UX Specifications'),
    ('2.1', 'Design System'),
    ('2.2', 'Screen Wireframes & Flows'),
    ('2.3', 'Animation Specifications'),
    ('3', 'System Architecture'),
    ('3.1', 'High-Level Architecture'),
    ('3.2', 'Data Flow Diagrams'),
    ('3.3', 'Offline-First Strategy'),
    ('4', 'Firestore Data Model'),
    ('5', 'Testing Plan'),
    ('5.1', 'Unit Tests'),
    ('5.2', 'Integration Tests'),
    ('5.3', 'Edge Cases'),
    ('6', 'Deployment Guide'),
    ('6.1', 'Signed APK/AAB Generation'),
    ('6.2', 'Google Play Console Setup'),
    ('6.3', 'Firebase Production Setup'),
    ('7', 'App Store Listing'),
    ('8', 'Post-Launch Plan'),
]

for num, title in toc_items:
    style = toc_h1 if '.' not in num else toc_h2
    story.append(P(f'{num}  {title}', style))

story.append(PageBreak())

# ════════════════════════════════════════════════════════════════
# SECTION 1: PRD
# ════════════════════════════════════════════════════════════════
story.append(H1('1. Product Requirements Document (PRD)'))
story.append(Spacer(1, 6))

story.append(P('Breathy is a comprehensive Android application designed to help smokers quit their habit through a combination of health tracking, community support, gamification, and AI-powered coaching. The app targets the global smoking cessation market, which is valued at over $3 billion annually, with a growing demand for digital-first solutions that go beyond simple trackers and counters. Breathy differentiates itself through an integrated approach that combines real-time health milestone tracking, social accountability, competitive events, and personalized AI coaching, all wrapped in a visually striking dark-mode interface with neon accents that appeals to a younger, health-conscious demographic.'))

story.append(H2('1.1 User Personas'))

story.append(H3('Persona 1: "Determined Dana" (Primary)'))
story.append(P('Dana is a 28-year-old marketing professional who has been smoking for 8 years, averaging 15 cigarettes per day. She has tried to quit multiple times using nicotine patches and willpower alone, but each attempt lasted less than two weeks. Dana is tech-savvy, uses her smartphone for over 4 hours daily, and is active on social media. She thrives on community support and accountability. Her primary motivation for quitting is health improvement and saving money for travel. She needs a solution that provides daily motivation, tracks her progress visually, and connects her with others on the same journey. Dana would use the community features heavily, participate in events, and rely on the AI coach during cravings.'))

story.append(H3('Persona 2: "Private Pete" (Secondary)'))
story.append(P('Pete is a 42-year-old software engineer who smokes 20 cigarettes per day and has been smoking for over 20 years. He is introverted, values privacy, and prefers self-directed solutions over social features. Pete wants a straightforward tracker that shows him measurable health improvements and financial savings. He is willing to pay for a premium, ad-free experience. Pete would primarily use the home dashboard for tracking, the craving coping tools, and the AI coach for private guidance. He would likely purchase the "Support Me" subscription to remove ads and access priority AI responses.'))

story.append(H3('Persona 3: "Competitive Carla" (Secondary)'))
story.append(P('Carla is a 24-year-old fitness enthusiast and former athlete who recently started smoking socially but quickly escalated to 10 cigarettes per day. She is highly competitive, participates in fitness challenges, and is motivated by leaderboards and achievements. Carla would be drawn to the events system, the global leaderboard, and the gamification elements like XP, levels, and achievement badges. She would participate in push-up challenges, aim for top leaderboard positions, and share her achievements on social media.'))

story.append(H2('1.2 Functional Requirements'))

story.append(H3('FR-1: Authentication & Onboarding'))
story.append(B('FR-1.1: Email/password registration and login with field validation and error messages'))
story.append(B('FR-1.2: Google Sign-In integration via Firebase Auth with one-tap sign-in'))
story.append(B('FR-1.3: 4-step onboarding flow: quit date selection, quit type (instant/gradual), smoking habits (cigarettes/day, price/pack, cigarettes/pack), and profile setup (nickname, photo)'))
story.append(B('FR-1.4: Password reset via email with confirmation feedback'))
story.append(B('FR-1.5: Automatic navigation to onboarding for new users, home for returning users'))

story.append(H3('FR-2: Home Dashboard & Health Tracking'))
story.append(B('FR-2.1: Days smoke-free counter with large animated display'))
story.append(B('FR-2.2: Stat cards for money saved, cigarettes avoided, and life regained with animated counters'))
story.append(B('FR-2.3: Health timeline showing 12 milestones (20 min to 15 years) with progress indicators'))
story.append(B('FR-2.4: Daily reward claim button with 24-hour cooldown timer'))
story.append(B('FR-2.5: Craving SOS button opening coping method selection (breathing exercise, mini game, AI coach)'))
story.append(B('FR-2.6: XP/level progress bar with level-up celebrations'))
story.append(B('FR-2.7: Pull-to-refresh for all dashboard data'))

story.append(H3('FR-3: Community & Stories'))
story.append(B('FR-3.1: Paginated community feed of success stories with cursor-based loading'))
story.append(B('FR-3.2: Story creation with content (max 2000 chars), optional life changes, and preview'))
story.append(B('FR-3.3: Like/unlike stories with animated heart and optimistic UI updates'))
story.append(B('FR-3.4: Threaded replies with parentReplyId support (max 3 indent levels)'))
story.append(B('FR-3.5: Public user profiles with stats, stories, and friend/message actions'))
story.append(B('FR-3.6: Search stories by content with debounced queries'))

story.append(H3('FR-4: Friends & Chat'))
story.append(B('FR-4.1: Friend request system (send, accept, reject) with real-time status updates'))
story.append(B('FR-4.2: 1:1 real-time chat with deterministic chat IDs (sorted UIDs joined by underscore)'))
story.append(B('FR-4.3: Typing indicators with 3-second auto-expiry'))
story.append(B('FR-4.4: Read receipts (single check = sent, double check = read)'))
story.append(B('FR-4.5: Message timestamp grouping (today, yesterday, date)'))
story.append(B('FR-4.6: Friend search by nickname with debounced prefix matching'))

story.append(H3('FR-5: Events & Leaderboard'))
story.append(B('FR-5.1: Active events listing with details, participant count, prize information'))
story.append(B('FR-5.2: Event participation with daily video check-ins via CameraX'))
story.append(B('FR-5.3: Admin review system for approving/rejecting check-in videos'))
story.append(B('FR-5.4: Event leaderboard ranked by total approved days and current streak'))
story.append(B('FR-5.5: Global XP leaderboard with period filtering (weekly, monthly, all-time)'))
story.append(B('FR-5.6: Top-3 podium display with gold, silver, bronze indicators'))

story.append(H3('FR-6: AI Coach'))
story.append(B('FR-6.1: Chat interface with AI coach powered by OpenAI GPT-4o-mini via Cloud Functions'))
story.append(B('FR-6.2: Context-aware suggestion chips based on user quit stage'))
story.append(B('FR-6.3: Rate limiting (5 messages per minute client-side, 10 per minute server-side)'))
story.append(B('FR-6.4: Conversation history persisted in Firestore coach_chats subcollection'))
story.append(B('FR-6.5: Medical disclaimer banner at top of chat'))
story.append(B('FR-6.6: Clear history option with confirmation dialog'))

story.append(H3('FR-7: Gamification & Achievements'))
story.append(B('FR-7.1: XP and coin reward system with level progression (19 levels)'))
story.append(B('FR-7.2: 19 achievement badges with unlock conditions and XP rewards'))
story.append(B('FR-7.3: Daily reward claim (50 coins, 25 XP) with 24-hour cooldown'))
story.append(B('FR-7.4: Confetti celebrations on achievements and milestone completions'))
story.append(B('FR-7.5: Achievement categories: quit milestones, craving defeat, community, events, consistency'))

story.append(H3('FR-8: Monetization'))
story.append(B('FR-8.1: "Support Me" one-time $1 purchase via Google Play Billing 6.0'))
story.append(B('FR-8.2: Benefits: ad-free experience, supporter badge, priority AI coach'))
story.append(B('FR-8.3: AdMob open-app ad on cold start'))
story.append(B('FR-8.4: AdMob interstitial ads with frequency capping (1 per 3 minutes)'))
story.append(B('FR-8.5: Subscription status tracking in Firestore'))
story.append(B('FR-8.6: Restore purchases functionality'))

story.append(H2('1.3 Non-Functional Requirements'))

story.append(H3('NFR-1: Performance'))
story.append(B('NFR-1.1: App cold start time under 3 seconds on mid-range devices (Snapdragon 680, 6GB RAM)'))
story.append(B('NFR-1.2: Screen transitions under 300ms with smooth 60fps animations'))
story.append(B('NFR-1.3: Firestore queries returning under 500ms for paginated lists'))
story.append(B('NFR-1.4: Image loading via Coil with memory and disk caching, placeholder and error states'))
story.append(B('NFR-1.5: Network timeout of 30 seconds for all API calls, 5 minutes for video uploads'))

story.append(H3('NFR-2: Offline Support'))
story.append(B('NFR-2.1: Firestore persistence enabled with 100MB cache for offline document access'))
story.append(B('NFR-2.2: Offline-friendly home dashboard showing cached user data and stats'))
story.append(B('NFR-2.3: Queued writes for mutations made offline, synced when connectivity restored'))
story.append(B('NFR-2.4: Clear offline indicator shown to users when network is unavailable'))
story.append(B('NFR-2.5: Graceful degradation: community feed shows cached stories, chat shows cached messages'))

story.append(H3('NFR-3: Security'))
story.append(B('NFR-3.1: Firebase Auth for user authentication with secure token management'))
story.append(B('NFR-3.2: Firestore Security Rules enforcing owner-only writes for private data'))
story.append(B('NFR-3.3: Firebase Storage Rules with file type and size validation'))
story.append(B('NFR-3.4: Cloud Functions verify authentication before processing callable requests'))
story.append(B('NFR-3.5: Rate limiting on AI coach endpoint (10 messages per minute per user)'))
story.append(B('NFR-3.6: No sensitive data stored locally; all private data in Firestore with security rules'))

story.append(H3('NFR-4: Reliability'))
story.append(B('NFR-4.1: Crashlytics for crash reporting with automatic breadcrumbs'))
story.append(B('NFR-4.2: Automatic retry with exponential backoff for transient network failures (max 3 retries)'))
story.append(B('NFR-4.3: Graceful error handling with user-friendly messages for all async operations'))
story.append(B('NFR-4.4: Memory leak prevention via DisposableEffect for listener cleanup and onCleared for ViewModels'))
story.append(B('NFR-4.5: Target crash-free rate of 99.5% within first month of launch'))

story.append(PageBreak())

# ════════════════════════════════════════════════════════════════
# SECTION 2: UI/UX SPECIFICATIONS
# ════════════════════════════════════════════════════════════════
story.append(H1('2. UI/UX Specifications'))
story.append(Spacer(1, 6))

story.append(P('The Breathy UI follows a dark-first design philosophy with neon green (#00E676) as the primary accent color, creating a visually striking experience that feels modern and engaging. The design language is inspired by gaming interfaces and fitness apps, combining the motivational energy of achievement systems with the calm reassurance needed during a quit journey. Every screen prioritizes clarity, with large typography for key metrics, smooth animations that reward progress, and a consistent visual hierarchy that guides users naturally through the app.'))

story.append(H2('2.1 Design System'))

story.append(H3('Color Palette'))
story.append(P('The color system uses a 4-tier dark background hierarchy with neon accents. Backgrounds range from the deepest black (#0D1117) used for full-screen containers, through medium-dark surfaces (#161B22) for cards and list items, to lighter surfaces (#1C2128, #21262D) for interactive elements and elevated components. The primary accent (#00E676 neon green) is used sparingly for CTAs, active states, and achievement indicators, ensuring it retains its visual impact. Secondary accents include coral red (#FF6B6B) for cravings and warnings, light blue (#4FC3F7) for informational elements, gold (#FFD54F) for achievements and premium features, and purple (#AB47BC) for AI coach elements.'))

color_data = [
    ['Background Primary', '#0D1117', 'Full-screen containers, main background'],
    ['Background Surface', '#161B22', 'Cards, list items, bottom sheets'],
    ['Background Elevated', '#1C2128', 'Interactive elements, hover states'],
    ['Background Highlight', '#21262D', 'Elevated components, FAB backgrounds'],
    ['Accent Primary', '#00E676', 'CTAs, active states, success indicators'],
    ['Accent Secondary', '#FF6B6B', 'Cravings, warnings, destructive actions'],
    ['Accent Info', '#4FC3F7', 'Links, informational badges'],
    ['Accent Warning', '#FFD54F', 'Achievements, premium, daily rewards'],
    ['Accent Purple', '#AB47BC', 'AI coach, special features'],
    ['Text Primary', '#E6EDF3', 'Headings, body text'],
    ['Text Secondary', '#8B949E', 'Captions, metadata, timestamps'],
    ['Text Disabled', '#484F58', 'Disabled states, inactive elements'],
]
story.append(Spacer(1, 6))
story.append(make_table(['Color Role', 'Hex Value', 'Usage'], color_data,
    col_widths=[AVAIL_W*0.25, AVAIL_W*0.20, AVAIL_W*0.55]))
story.append(Spacer(1, 6))
story.append(P('Table 1: Breathy Color Palette', caption_style))

story.append(H3('Typography'))
story.append(P('The typographic system uses three font families: Montserrat for headlines and display text (Bold weight for visual hierarchy), Inter for body text and UI elements (Regular for body, Medium for emphasis), and Space Mono for statistics and numerical data (Bold for impact). This combination creates clear visual separation between different content types while maintaining readability across all screen sizes. Headline sizes range from 24sp (H1) down to 14sp (H4), body text at 16sp and 14sp, and statistical counters at 48sp and 32sp for maximum impact on the home dashboard.'))

typo_data = [
    ['Headline 1', 'Montserrat Bold', '24sp', 'Screen titles, major headings'],
    ['Headline 2', 'Montserrat Bold', '20sp', 'Section headings'],
    ['Headline 3', 'Montserrat Bold', '18sp', 'Subsection headings'],
    ['Body Large', 'Inter Regular', '16sp', 'Primary body text'],
    ['Body Medium', 'Inter Regular', '14sp', 'Secondary text, descriptions'],
    ['Caption', 'Inter Light', '12sp', 'Metadata, timestamps'],
    ['Stat Large', 'Space Mono Bold', '48sp', 'Days smoke-free counter'],
    ['Stat Medium', 'Space Mono Bold', '32sp', 'Money saved, cigarettes avoided'],
    ['Button', 'Inter Medium', '16sp', 'CTA buttons'],
    ['Overline', 'Inter Medium', '10sp', 'Labels, badges'],
]
story.append(Spacer(1, 6))
story.append(make_table(['Style', 'Font', 'Size', 'Usage'], typo_data,
    col_widths=[AVAIL_W*0.20, AVAIL_W*0.25, AVAIL_W*0.15, AVAIL_W*0.40]))
story.append(Spacer(1, 6))
story.append(P('Table 2: Typography Scale', caption_style))

story.append(H2('2.2 Screen Wireframes & User Flows'))

story.append(H3('Auth Screen'))
story.append(P('The Auth Screen serves as the entry point for both new and returning users. The layout features the Breathy logo at the top with a subtle breathing glow animation that pulses slowly (2-second cycle), creating an immediate visual identity. Below the logo, a toggle control switches between "Login" and "Register" modes. The login form contains email and password fields with inline validation, a "Forgot Password?" text link, and a gradient CTA button. The register form adds a confirm password field. A horizontal divider with "OR" separates the email section from the Google Sign-In button, which uses the standard Google branding. Error messages appear in snackbar form at the bottom of the screen. The entire form uses Material 3 OutlinedTextField components with the dark theme.'))

story.append(H3('Onboarding Flow (4 Steps)'))
story.append(P('The onboarding flow uses a horizontal stepper with 4 dots at the top, filled progressively as the user advances. Each step occupies the full screen below the stepper, with a consistent layout of content in the center and navigation buttons (Back/Skip/Next) at the bottom. Step 1 (Welcome) shows an animated icon and a date picker for selecting the quit date. Step 2 (Quit Type) presents two large selectable cards for "Instant Quit" and "Gradual Reduction," each with an icon, title, and description. Step 3 (Smoking Habits) uses a stepper control for cigarettes per day, and text inputs for price per pack and cigarettes per pack, with a live savings preview card. Step 4 (Profile Setup) includes a circular photo picker, nickname field with validation, and a motivational card. The final "Let\'s Go!" button triggers a Firestore batch write saving all data atomically.'))

story.append(H3('Home Screen'))
story.append(P('The Home Screen is the primary dashboard and the most frequently visited screen. It follows a vertical scrolling layout with distinct sections. At the top, a personalized greeting ("Good morning, Dana!") sets a warm tone. Below, the days smoke-free counter dominates the screen with a large number in Space Mono Bold 48sp, with a scale+fade entrance animation. Three stat cards are arranged in a horizontal row: Money Saved (green), Cigarettes Avoided (coral), and Life Regained (blue). Each card uses an animated counter that increments from 0 to the current value on mount. The health timeline follows with a horizontal scrolling list of 12 milestones, each showing the time period, improvement description, and a checkmark if achieved. A circular progress bar shows overall health recovery percentage. The daily reward section shows a "Claim" button with a countdown timer if already claimed. The craving SOS button is a floating element at the bottom-right with a pulsing animation. Pull-to-refresh updates all dashboard data from Firestore.'))

story.append(H3('Community Screen'))
story.append(P('The Community Screen displays a paginated feed of success stories in a LazyColumn. At the top, a search bar allows filtering stories by content. Each story card shows the author\'s avatar (circular AsyncImage), nickname, days smoke-free badge, story content (expandable for long text), life changes as chips, like count with heart icon, and reply count. The like button animates with a scale effect (1.0 to 1.3 to 1.0). A floating action button in the bottom-right navigates to the post story screen. Pull-to-refresh reloads the feed. Empty states show an animated icon with a motivational message. The Story Detail Screen shows the full story with a like button, reply count, and a threaded reply list below. The reply input field stays fixed at the bottom with a send button.'))

story.append(H3('Friends & Chat'))
story.append(P('The Friends Screen uses a tab layout with two tabs: Friends List and Requests. The friends list shows each friend with avatar, nickname, days smoke-free badge, and an online indicator (green dot). Tapping a friend opens the 1:1 chat. Swiping reveals a delete action with confirmation. The Requests tab shows incoming requests with accept/reject buttons and outgoing requests with a "Pending" badge. A search icon in the top bar opens a dialog for searching users by nickname. The Chat Screen features a top bar with the friend\'s avatar, name, and typing indicator. Messages use right-aligned neon green bubbles for sent messages and left-aligned dark surface bubbles for received messages. Each bubble has a tail decoration. Timestamps are grouped with header pills ("Today", "Yesterday", or date). The typing indicator shows three animated dots with staggered bounce animations. Read receipts display as single or double checkmarks in the message tail.'))

story.append(H2('2.3 Animation Specifications'))

anim_data = [
    ['Stat counter mount', 'Scale 0.8 to 1.0 + Fade 0 to 1', '400ms', 'EaseOutCubic', 'Home stat cards, leaderboard entries'],
    ['Days counter mount', 'Scale 0.5 to 1.0 + Fade 0 to 1', '600ms', 'EaseOutBack', 'Days smoke-free counter'],
    ['Like button tap', 'Scale 1.0 to 1.3 to 1.0', '300ms', 'EaseInOut', 'Story like, reply like'],
    ['Heart fill', 'Outline to filled + scale pulse', '250ms', 'EaseOut', 'Like animation'],
    ['Achievement unlock', 'Scale 0 to 1.2 to 1.0 + confetti', '800ms', 'EaseOutBack', 'Achievement badge reveal'],
    ['Confetti burst', 'Konfetti smallBurst preset', '2000ms', 'Linear', 'Daily reward, craving defeat'],
    ['Big celebration', 'Konfetti bigCelebration preset', '3000ms', 'Linear', 'Level up, milestone reached'],
    ['Breathing circle', 'Expand/contract 4-7-8 cycle', '19s/cycle', 'EaseInOut', 'Craving breathing exercise'],
    ['Typing dots', 'Staggered vertical bounce', '900ms loop', 'EaseInOut', 'Chat typing indicator'],
    ['Tab switch', 'Fade 0 to 1 + slight slide', '200ms', 'EaseOut', 'Tab navigation'],
    ['Page transition', 'Slide + fade', '250ms', 'EaseInOut', 'Navigation between screens'],
    ['Pull indicator', 'Scale + rotation', '300ms', 'EaseOut', 'Pull-to-refresh'],
]
story.append(Spacer(1, 6))
story.append(make_table(['Animation', 'Effect', 'Duration', 'Easing', 'Trigger'], anim_data,
    col_widths=[AVAIL_W*0.18, AVAIL_W*0.28, AVAIL_W*0.12, AVAIL_W*0.15, AVAIL_W*0.27]))
story.append(Spacer(1, 6))
story.append(P('Table 3: Animation Specifications', caption_style))

story.append(PageBreak())

# ════════════════════════════════════════════════════════════════
# SECTION 3: SYSTEM ARCHITECTURE
# ════════════════════════════════════════════════════════════════
story.append(H1('3. System Architecture'))
story.append(Spacer(1, 6))

story.append(P('Breathy follows a client-server architecture with Firebase as the backend platform, providing real-time data synchronization, authentication, storage, and serverless compute via Cloud Functions. The Android client is built with a unidirectional data flow pattern where ViewModels expose state to Compose UI, and user actions flow back through repositories to Firebase services. This architecture prioritizes offline-first operation, leveraging Firestore\'s built-in persistence and synchronization to ensure the app remains functional without network connectivity.'))

story.append(H2('3.1 High-Level Architecture'))
story.append(P('The system consists of four primary layers: Presentation Layer (Jetpack Compose UI + ViewModels), Data Layer (Repositories with Firestore/Firebase), Cloud Layer (Cloud Functions for server-side logic), and External Services (OpenAI API for AI coach, AdMob for ads, Play Billing for payments). Each layer communicates through well-defined interfaces, with repositories serving as the single source of truth for application data. ViewModels observe repository state via Kotlin Flows and expose immutable state to Compose screens.'))

arch_data = [
    ['Presentation', 'Compose UI + ViewModels', 'MainActivity, AuthScreen, HomeScreen, CommunityScreen, etc.', 'Renders UI, handles user interactions'],
    ['Navigation', 'Compose Navigation', 'NavGraph, BreathyRoutes', 'Type-safe screen navigation with deep links'],
    ['Data', 'Repositories', 'AuthRepository, UserRepository, StoryRepository, FriendRepository, ChatRepository, EventRepository, RewardRepository, CoachRepository', 'CRUD operations, real-time listeners, data transformation'],
    ['DI', 'AppModule', 'Manual DI container', 'Provides Firebase services and repository singletons'],
    ['Cloud', 'Cloud Functions', 'updateDaysSmokeFree, onReplyCreated, onReplyDeleted, sendChatNotification, calculateEventRanks, openAIChat', 'Scheduled tasks, triggers, AI proxy'],
    ['Backend', 'Firebase', 'Auth, Firestore, Storage, Functions, Messaging, Analytics, Crashlytics', 'Authentication, data storage, file storage, push notifications'],
    ['External', 'Third-party APIs', 'OpenAI GPT-4o-mini, AdMob, Play Billing', 'AI coaching, monetization, payments'],
]
story.append(Spacer(1, 6))
story.append(make_table(['Layer', 'Component', 'Modules', 'Responsibility'], arch_data,
    col_widths=[AVAIL_W*0.12, AVAIL_W*0.18, AVAIL_W*0.40, AVAIL_W*0.30]))
story.append(Spacer(1, 6))
story.append(P('Table 4: Architecture Layer Summary', caption_style))

story.append(H2('3.2 Data Flow Diagrams'))

story.append(H3('Critical Path 1: User Opens App (Cold Start)'))
story.append(P('When the user launches the app, the following sequence occurs: (1) BreathyApplication.onCreate() initializes FirebaseApp, configures Firestore persistence (100MB cache), and sets up Timber logging trees. (2) MainActivity.onCreate() sets the content view with BreathyTheme and BreathyNavHost. (3) AuthRepository observes FirebaseAuth.authStateChanges via a StateFlow, emitting the current user or null. (4) NavGraph checks the auth state: if null, navigates to AuthScreen; if authenticated, checks Firestore for the user\'s quitDate to determine onboarding vs. home. (5) If the user is active, AdManager loads the open-app ad, which displays after a brief loading state. (6) HomeScreen\'s ViewModel subscribes to the user document in Firestore, receiving real-time updates for stats, XP, and achievements. (7) All data is served from the local Firestore cache if offline, with automatic sync when connectivity is restored.'))

story.append(H3('Critical Path 2: Creating a Story'))
story.append(P('The story creation flow: (1) User taps the FAB on CommunityScreen, navigating to PostStoryScreen. (2) User enters content (max 2000 chars) and optional life changes (max 5 chips). (3) On submit, PostStoryViewModel calls StoryRepository.createStory(), which writes a new document to the stories collection with userId, nickname, photoURL, content, lifeChanges, daysSmokeFree, likes=0, likedBy=[], replyCount=0, and createdAt=serverTimestamp. (4) Simultaneously, RewardRepository.checkAndUnlockAchievement() runs in a Firestore transaction to check if the user has unlocked any new achievements (e.g., "First Story" for the first post). (5) The community feed auto-updates via the existing Firestore query listener, and the new story appears at the top. (6) The user receives XP and coins for posting (50 XP, 10 coins), updated atomically in the user document via transaction.'))

story.append(H3('Critical Path 3: AI Coach Conversation'))
story.append(P('The AI coach flow involves both client and server components: (1) User types a message and taps send on AICoachScreen. (2) CoachViewModel performs client-side rate limiting (5 msg/min check), then calls CoachRepository.sendMessage(). (3) CoachRepository calls the Firebase callable function "openAIChat" with the message and user context. (4) The Cloud Function verifies authentication, checks server-side rate limits (10 msg/min via Firestore query), and retrieves the last 20 messages from coach_chats as context. (5) The function calls the OpenAI API with a system prompt defining the Breathy AI Coach persona and the conversation context. (6) The function saves both the user message and assistant response to the coach_chats subcollection. (7) The client observes the coach_chats collection via a real-time Flow listener, receiving the response automatically. (8) If the OpenAI API call fails, a fallback motivational response is saved and returned. (9) Rate limit exceeded results in a user-friendly countdown message.'))

story.append(H2('3.3 Offline-First Strategy'))
story.append(P('Breathy employs an offline-first strategy built on Firestore\'s built-in persistence capabilities. When the app is first installed, Firestore downloads and caches all documents that the user has access to, up to the configured 100MB cache limit. Subsequent reads are served from the local cache first, with Firestore automatically synchronizing changes in the background when network connectivity is available. This means the home dashboard, user stats, and health milestones are always available, even without an internet connection. Write operations made offline are queued locally and automatically synced when connectivity is restored, with Firestore handling conflict resolution using its last-write-wins strategy with server timestamps.'))

story.append(P('Key offline behaviors include: (1) Home dashboard displays cached user data with a subtle offline indicator. (2) Community feed shows previously loaded stories, with a "Connect to see more" message when paginating offline. (3) Chat displays cached messages, with queued outgoing messages marked as "pending" until synced. (4) Craving coping tools (breathing exercise, tap game) work fully offline since they are client-side only. (5) AI coach is unavailable offline, with a clear message directing users to breathing exercises instead. (6) Event check-in video uploads are queued and auto-resumed when connectivity returns. (7) All repository methods use withTimeoutOrNull(30_000) to prevent indefinite hangs, returning cached data on timeout.'))

story.append(PageBreak())

# ════════════════════════════════════════════════════════════════
# SECTION 4: FIRESTORE DATA MODEL
# ════════════════════════════════════════════════════════════════
story.append(H1('4. Firestore Data Model'))
story.append(Spacer(1, 6))

story.append(P('The Firestore data model is designed around a combination of top-level collections and subcollections, following Firestore best practices for query patterns and security rule granularity. The model separates private user data (users collection) from public-facing data (publicProfiles collection), enabling different security rules for each access pattern. Subcollections are used for hierarchical data (replies under stories, messages under chats) to enable efficient queries within a parent document\'s scope. All collections use Firestore server timestamps for consistency across devices.'))

story.append(H2('4.1 Collection: users/{userId}'))
story.append(P('The users collection stores private user data that only the owner can access. This includes authentication-related information, quit smoking configuration, and gamification state. The document ID matches the Firebase Auth UID for easy lookup.'))

users_data = [
    ['email', 'String', 'Required', 'User email from Firebase Auth'],
    ['nickname', 'String', 'Required', 'Display name (3-30 chars)'],
    ['age', 'Number (nullable)', 'Optional', 'User age for health calculations'],
    ['quitDate', 'Timestamp', 'Required', 'Date user quit smoking'],
    ['quitType', 'String (instant/gradual)', 'Required', 'Quit method chosen during onboarding'],
    ['cigarettesPerDay', 'Number', 'Required', 'Average daily cigarette consumption'],
    ['pricePerPack', 'Number', 'Required', 'Local currency price per pack'],
    ['cigarettesPerPack', 'Number', 'Required', 'Cigarettes per pack for savings calc'],
    ['xp', 'Number', 'Required', 'Total experience points (default 0)'],
    ['coins', 'Number', 'Required', 'Virtual currency balance (default 0)'],
    ['lastDailyClaim', 'Timestamp (nullable)', 'Optional', 'Last daily reward claim time'],
    ['createdAt', 'Timestamp', 'Required', 'Account creation timestamp'],
    ['achievements', 'Array<String>', 'Required', 'List of unlocked achievement IDs'],
    ['givenLikes', 'Array<String>', 'Required', 'Story IDs the user has liked'],
]
story.append(Spacer(1, 6))
story.append(make_table(['Field', 'Type', 'Required', 'Description'], users_data,
    col_widths=[AVAIL_W*0.20, AVAIL_W*0.22, AVAIL_W*0.13, AVAIL_W*0.45]))
story.append(Spacer(1, 6))
story.append(P('Table 5: users Collection Schema', caption_style))

story.append(H2('4.2 Collection: publicProfiles/{userId}'))
story.append(P('The publicProfiles collection stores publicly readable user data for community features. The daysSmokeFree field is updated daily by the updateDaysSmokeFree Cloud Function. This separation allows public data to be read by any authenticated user without exposing private information like email or smoking habits.'))

pub_data = [
    ['nickname', 'String', 'Required', 'Public display name'],
    ['photoURL', 'String', 'Optional', 'Profile photo download URL'],
    ['daysSmokeFree', 'Number', 'Required', 'Days since quit (updated by CF)'],
    ['xp', 'Number', 'Required', 'XP for leaderboard ranking'],
    ['location', 'String (nullable)', 'Optional', 'User location (city/country)'],
    ['quitDate', 'Timestamp', 'Required', 'Quit date for milestone calc'],
]
story.append(Spacer(1, 6))
story.append(make_table(['Field', 'Type', 'Required', 'Description'], pub_data,
    col_widths=[AVAIL_W*0.20, AVAIL_W*0.22, AVAIL_W*0.13, AVAIL_W*0.45]))
story.append(Spacer(1, 6))
story.append(P('Table 6: publicProfiles Collection Schema', caption_style))

story.append(H2('4.3 Collection: stories/{storyId}'))
story.append(P('Stories are the core content type for the community feature. Each story represents a user\'s quit smoking experience or milestone. The replyCount field is maintained atomically by Cloud Functions (onReplyCreated/onReplyDeleted) to avoid N+1 query patterns when displaying story lists. The likedBy array enables O(1) like-status checks for the current user.'))

story_data = [
    ['userId', 'String', 'Required', 'Author Firebase UID'],
    ['nickname', 'String', 'Required', 'Author display name (denormalized)'],
    ['photoURL', 'String', 'Optional', 'Author photo (denormalized)'],
    ['content', 'String', 'Required', 'Story text (max 2000 chars)'],
    ['lifeChanges', 'String', 'Optional', 'Positive changes since quitting'],
    ['daysSmokeFree', 'Number', 'Required', 'Days smoke-free at post time'],
    ['likes', 'Number', 'Required', 'Like count (atomic increment)'],
    ['likedBy', 'Array<String>', 'Required', 'User IDs who liked (for checking)'],
    ['replyCount', 'Number', 'Required', 'Reply count (maintained by CF)'],
    ['createdAt', 'Timestamp', 'Required', 'Post creation timestamp'],
]
story.append(Spacer(1, 6))
story.append(make_table(['Field', 'Type', 'Required', 'Description'], story_data,
    col_widths=[AVAIL_W*0.18, AVAIL_W*0.22, AVAIL_W*0.12, AVAIL_W*0.48]))
story.append(Spacer(1, 6))
story.append(P('Table 7: stories Collection Schema', caption_style))

story.append(H2('4.4 Additional Collections Summary'))
story.append(P('The remaining collections follow similar denormalization patterns for read performance. The replies collection is a subcollection under stories/{storyId}/replies/, enabling efficient querying within a story\'s scope. The friendRequests and friendships collections handle the friend relationship lifecycle. The chats collection uses deterministic IDs (sorted UIDs joined by underscore) to prevent duplicate chats. Messages are a subcollection under chats/{chatId}/messages/. The events, eventParticipants, and eventCheckins collections support the challenge system. The craving_logs collection records craving events for analytics. The subscriptions collection tracks payment status. The coach_chats collection stores AI coach conversations as a subcollection under coach_chats/{userId}/messages/.'))

coll_data = [
    ['replies (subcollection)', 'storyId, userId, nickname, photoURL, content, parentReplyId, createdAt', 'Threaded story replies'],
    ['friendRequests', 'fromUserId, toUserId, status, timestamp', 'Friend request lifecycle'],
    ['friendships', 'userIds (array), createdAt', 'Bidirectional friend relationships'],
    ['chats', 'participants, lastMessage, lastUpdated, typing', '1:1 chat metadata'],
    ['messages (subcollection)', 'senderId, text, timestamp, read', 'Chat messages with read receipts'],
    ['events', 'title, description, startDate, endDate, active, prizes, dailyRequired', 'Challenge events'],
    ['eventParticipants', 'userId, eventId, currentStreak, totalApprovedDays, completed, rank', 'Event participation tracking'],
    ['eventCheckins', 'userId, eventId, dayNumber, videoURL, status, reviewComment', 'Video check-in submissions'],
    ['craving_logs', 'userId, timestamp, copingMethod, success', 'Craving event logging'],
    ['subscriptions', 'active, plan, expiresAt, purchaseToken', 'Payment tracking'],
    ['coach_chats (subcollection)', 'role, content, timestamp', 'AI coach conversation history'],
]
story.append(Spacer(1, 6))
story.append(make_table(['Collection', 'Key Fields', 'Purpose'], coll_data,
    col_widths=[AVAIL_W*0.25, AVAIL_W*0.50, AVAIL_W*0.25]))
story.append(Spacer(1, 6))
story.append(P('Table 8: Additional Collections Summary', caption_style))

story.append(PageBreak())

# ════════════════════════════════════════════════════════════════
# SECTION 5: TESTING PLAN
# ════════════════════════════════════════════════════════════════
story.append(H1('5. Testing Plan'))
story.append(Spacer(1, 6))

story.append(P('The testing strategy for Breathy follows the testing pyramid model, with a strong emphasis on unit tests for business logic (particularly repository transactions and reward calculations), integration tests for Firebase interactions (using the Firebase Emulator Suite), and manual end-to-end testing for critical user flows. The Firebase Emulator Suite enables local testing of Firestore operations, Cloud Functions, and authentication without incurring billing charges or affecting production data.'))

story.append(H2('5.1 Unit Tests'))

story.append(H3('Test: Daily Reward Transaction'))
story.append(P('This test verifies the daily reward claim logic, which is one of the most critical transactions in the app. The test covers multiple scenarios: (1) Successful claim when no previous claim exists, awarding 50 coins and 25 XP atomically. (2) Successful claim when 24+ hours have passed since the last claim. (3) Rejected claim when less than 24 hours have passed, returning the remaining cooldown duration. (4) Concurrent claim attempts from multiple devices, ensuring only one succeeds per 24-hour window. (5) Transaction failure and rollback when Firestore is temporarily unavailable. The test uses mocked Firestore document snapshots and verifies that the coins, XP, and lastDailyClaim fields are updated atomically within a single transaction block.'))

story.append(H3('Test: XP Level Calculation'))
story.append(P('This test validates the level computation from XP, which uses a threshold table with 19 levels. The test verifies: (1) Level 1 at 0 XP, (2) Level 2 at 100 XP, (3) Level 5 at 1,000 XP, (4) Level 10 at 10,000 XP, (5) Level 19 at 100,000 XP, (6) Boundary values at each level threshold (one XP below and at the threshold), (7) Negative XP handling (clamped to Level 1), and (8) Very large XP values (clamped to Level 19). The level is computed as a property of the User data class, so tests can directly instantiate User objects and assert the computed level value.'))

story.append(H3('Test: Achievement Unlock Logic'))
story.append(P('This test verifies the 19 achievement definitions and their unlock conditions. Each achievement is tested for: (1) Correct unlock when the condition is met (e.g., "First Step" at daysSmokeFree >= 1), (2) No unlock when the condition is not met, (3) No duplicate unlocks (already-unlocked achievements are not re-awarded), (4) XP reward is correctly added to the user\'s total, (5) Achievement ID is appended to the user\'s achievements array, and (6) Transaction atomicity (both XP and achievement update succeed or both fail). Notable achievements tested include "First Story" (post first story), "Social Butterfly" (get 10 likes), "Iron Will" (defeat 50 cravings), and "Legend" (365 days smoke-free).'))

story.append(H2('5.2 Integration Tests'))

story.append(H3('Firebase Emulator Setup'))
story.append(P('Integration tests run against the Firebase Emulator Suite, which provides local instances of Firestore, Auth, and Cloud Functions. The setup involves: (1) Installing the Firebase CLI and running `firebase emulators:start` with the project configuration. (2) Setting the FIRESTORE_EMULATOR_HOST, FIREBASE_AUTH_EMULATOR_HOST, and FIREBASE_FUNCTIONS_EMULATOR_HOST environment variables to point the Android app\'s Firebase instances to the local emulators. (3) Running instrumented tests on an Android emulator or physical device. (4) Using Firebase Admin SDK in test setup to seed test data and verify write results. The emulator enables testing of real Firestore queries, security rules evaluation, Cloud Function triggers, and auth flows without any cost or production impact.'))

story.append(H3('Integration Test Scenarios'))

int_data = [
    ['Auth flow', 'Register, login, logout, password reset', 'Firebase Auth Emulator', 'Account created in Auth, user doc in Firestore'],
    ['Story lifecycle', 'Create, read, like, reply, delete', 'Firestore Emulator', 'Correct document state after each operation'],
    ['Friend flow', 'Send request, accept, chat, remove', 'Firestore + Functions Emulator', 'Friendship doc created, chat accessible'],
    ['Event participation', 'Join, check-in, review, rank update', 'Firestore + Functions Emulator', 'Correct participant stats and ranking'],
    ['AI Coach', 'Send message, rate limit, history', 'Functions Emulator (mocked OpenAI)', 'Messages saved, rate limit enforced'],
    ['Daily reward', 'Claim, cooldown, concurrent claims', 'Firestore Emulator', 'Coins/XP updated, 24h cooldown enforced'],
    ['Craving log', 'Log craving with each coping method', 'Firestore Emulator', 'Correct log entry, achievement check triggered'],
    ['Notification delivery', 'New message, friend request, achievement', 'Functions + Firestore Emulator', 'FCM payload correct, token managed'],
]
story.append(Spacer(1, 6))
story.append(make_table(['Scenario', 'Operations', 'Emulator', 'Expected Result'], int_data,
    col_widths=[AVAIL_W*0.18, AVAIL_W*0.27, AVAIL_W*0.25, AVAIL_W*0.30]))
story.append(Spacer(1, 6))
story.append(P('Table 9: Integration Test Scenarios', caption_style))

story.append(H2('5.3 Edge Cases'))

edge_data = [
    ['No internet', 'User opens app with airplane mode', 'Dashboard loads from cache, offline indicator shown, community shows cached stories, AI coach unavailable with message'],
    ['Network timeout', 'Firestore query takes > 30 seconds', 'Repository returns cached data via withTimeoutOrNull, user sees stale data with refresh prompt'],
    ['Duplicate requests', 'User taps "like" rapidly 5 times', 'Optimistic UI shows liked, Firestore transaction ensures final state is correct (liked, not multi-liked)'],
    ['Concurrent writes', 'Two devices claim daily reward simultaneously', 'Firestore transaction ensures only one claim succeeds per 24h window, second gets cooldown error'],
    ['Empty Firestore', 'New user with no stories, friends, or events', 'Empty states with motivational messages and CTAs to create first story or add friends'],
    ['Large video upload', 'User submits 90MB check-in video on slow connection', 'Upload progress shown, 5-minute timeout, auto-retry on transient failure, cancel option available'],
    ['OpenAI API failure', 'AI coach request fails with 500 error', 'Fallback motivational response saved to chat, user informed of temporary issue, retry suggested'],
    ['Expired auth token', 'User session expires mid-operation', 'Firebase Auth auto-refreshes token, operation retried, if refresh fails user redirected to login'],
    ['Malformed deep link', 'App opened with invalid deep link URL', 'Navigation falls back to home screen, error logged to Crashlytics, no crash'],
    ['Storage quota exceeded', 'User uploads profile image when storage limit reached', 'Error message shown, image not uploaded, user advised to use smaller image'],
]
story.append(Spacer(1, 6))
story.append(make_table(['Edge Case', 'Scenario', 'Expected Behavior'], edge_data,
    col_widths=[AVAIL_W*0.18, AVAIL_W*0.35, AVAIL_W*0.47]))
story.append(Spacer(1, 6))
story.append(P('Table 10: Edge Case Handling', caption_style))

story.append(PageBreak())

# ════════════════════════════════════════════════════════════════
# SECTION 6: DEPLOYMENT GUIDE
# ════════════════════════════════════════════════════════════════
story.append(H1('6. Deployment Guide'))
story.append(Spacer(1, 6))

story.append(P('This section covers the complete deployment process from generating a signed release build to publishing on Google Play and configuring Firebase for production. Each step must be completed in order, as later steps depend on earlier ones (e.g., you need a signed AAB before uploading to Play Console, and you need Firebase production setup before the app can function with real data).'))

story.append(H2('6.1 Signed APK/AAB Generation'))

story.append(P('Before generating a release build, you must create a signing keystore and configure the build.gradle.kts signing config. The keystore should be stored securely and backed up, as losing it means you cannot update the app on Google Play. The release build uses ProGuard/R8 for code shrinking and obfuscation, reducing the APK size and making reverse engineering more difficult.'))

story.append(H3('Step 1: Generate Keystore'))
story.append(P('Run the following command to generate a release keystore. Replace the values with your own information. Store the keystore file in a secure location (not in version control) and record the passwords in a password manager. The keystore is valid for 25 years, which is the minimum required by Google Play. The command generates a 2048-bit RSA key pair, which is the standard for Android app signing.'))

story.append(P('keytool -genkey -v -keystore breathy-release.keystore -alias breathy -keyalg RSA -keysize 2048 -validity 9125 -storetype PKCS12', code_style))

story.append(H3('Step 2: Configure Signing'))
story.append(P('Set the following environment variables (or hardcode them in local.properties, which should be in .gitignore): BREATHY_KEYSTORE_FILE, BREATHY_KEYSTORE_PASSWORD, BREATHY_KEY_ALIAS, BREATHY_KEY_PASSWORD. The app/build.gradle.kts reads these via `System.getenv()` or `project.properties`. The release build type uses these to sign the AAB automatically during the build process.'))

story.append(H3('Step 3: Build Release AAB'))
story.append(P('Generate the Android App Bundle (AAB) for Play Store upload. The AAB format is required by Google Play and produces smaller downloads for users by including only the resources and code needed for their specific device configuration. The command is: `./gradlew bundleRelease`. The output AAB is located at `app/build/outputs/bundle/release/app-release.aab`. Verify the AAB size is under 150MB (Google Play\'s limit) and test it on at least 3 device configurations using the bundletool before uploading.'))

story.append(H2('6.2 Google Play Console Setup'))

story.append(P('The Google Play Console configuration involves multiple steps across several pages. Each step requires careful attention to ensure compliance with Google Play policies and to maximize the app\'s discoverability and appeal to potential users.'))

play_data = [
    ['Store Listing', 'App name, short/full description, icons, screenshots, feature graphic, category (Health & Fitness), tags', 'Required for review'],
    ['Content Rating', 'IARC questionnaire (no violence, no gambling, health topic)', 'Required for review'],
    ['Privacy Policy', 'URL to privacy policy (must include Firebase Analytics, AdMob, OpenAI data processing)', 'Required for review'],
    ['Data Safety', 'Declare data collection: email, nickname, photos, chat messages, usage analytics', 'Required for review'],
    ['Pricing', 'Free with in-app products ($1 Support Me subscription, ads)', 'Required for review'],
    ['App Signing', 'Opt-in to Google Play App Signing (recommended)', 'One-time setup'],
    ['Release Track', 'Start with Internal Testing (up to 100 testers), then Open Testing, then Production', 'Recommended flow'],
    ['Review', 'Automated + manual review, typically 3-7 days for new apps', 'Wait period'],
]
story.append(Spacer(1, 6))
story.append(make_table(['Section', 'Configuration', 'Notes'], play_data,
    col_widths=[AVAIL_W*0.18, AVAIL_W*0.55, AVAIL_W*0.27]))
story.append(Spacer(1, 6))
story.append(P('Table 11: Google Play Console Configuration', caption_style))

story.append(H2('6.3 Firebase Production Setup'))

story.append(P('Firebase must be configured for production before the app can function with real users. This involves enabling billing for Cloud Functions, deploying security rules, and configuring monitoring and analytics. The Firebase project "breathy-healthy" must be upgraded from the Spark plan (free) to the Blaze plan (pay-as-you-go) to use Cloud Functions, which requires a credit card on file. The Blaze plan includes a generous free tier that should cover the initial user base without charges.'))

fb_data = [
    ['Billing', 'Upgrade to Blaze plan', 'Required for Cloud Functions (Node.js 18 runtime)'],
    ['Firestore', 'Deploy firestore.rules', 'firebase deploy --only firestore:rules'],
    ['Storage', 'Deploy storage.rules', 'firebase deploy --only storage'],
    ['Indexes', 'Deploy firestore.indexes.json', 'firebase deploy --only firestore:indexes'],
    ['Functions', 'Deploy all functions', 'firebase deploy --only functions'],
    ['Env vars', 'Set OPENAI_API_KEY', 'firebase functions:secrets:set OPENAI_API_KEY'],
    ['Auth', 'Enable Email/Password + Google providers', 'Firebase Console > Authentication > Sign-in method'],
    ['Analytics', 'Enable Google Analytics', 'Firebase Console > Analytics'],
    ['Crashlytics', 'Enable Crashlytics', 'Firebase Console > Crashlytics (auto-enabled with SDK)'],
    ['Messaging', 'Enable FCM + upload server key', 'Firebase Console > Cloud Messaging'],
    ['App Check', 'Enable App Check (Play Integrity)', 'Firebase Console > App Check (recommended for production)'],
]
story.append(Spacer(1, 6))
story.append(make_table(['Component', 'Action', 'Command/Notes'], fb_data,
    col_widths=[AVAIL_W*0.15, AVAIL_W*0.30, AVAIL_W*0.55]))
story.append(Spacer(1, 6))
story.append(P('Table 12: Firebase Production Setup', caption_style))

story.append(H3('Monitoring'))
story.append(P('Post-deployment monitoring is critical for catching issues early. Firebase Crashlytics provides real-time crash reporting with stack traces, device information, and custom breadcrumbs. Firebase Analytics tracks key user behaviors and funnels. The following metrics should be monitored daily during the first week: crash-free rate (target > 99.5%), daily active users, average session duration, craving tool usage, community engagement (stories posted, likes given), and AI coach message volume. Set up Crashlytics alerts for new fatal issues and Analytics alerts for significant drops in daily active users.'))

story.append(PageBreak())

# ════════════════════════════════════════════════════════════════
# SECTION 7: APP STORE LISTING
# ════════════════════════════════════════════════════════════════
story.append(H1('7. App Store Listing (Google Play)'))
story.append(Spacer(1, 6))

story.append(H2('Title & Description'))

story.append(H3('App Title'))
story.append(P('Breathy: Quit Smoking for Good', body_left))

story.append(H3('Short Description (80 chars max)'))
story.append(P('Track progress, beat cravings, join events, and get AI coaching to quit smoking.', body_left))

story.append(H3('Full Description'))
story.append(P('Breathy is your all-in-one companion for quitting smoking and staying smoke-free. Whether you are on day one or day one thousand, Breathy gives you the tools, community, and motivation you need to break free from cigarettes for good. Our app combines real-time health tracking, social support, competitive events, and AI-powered coaching in a beautifully designed dark-mode interface that makes your quit journey feel rewarding, not restrictive.'))

story.append(P('KEY FEATURES:'))
story.append(B('Real-Time Health Tracking: Watch your body recover with our detailed health timeline. See exactly how your heart rate, oxygen levels, taste, breathing, and more improve from 20 minutes to 15 years after quitting.'))
story.append(B('Craving SOS: When a craving hits, Breathy is there. Choose from a guided 4-7-8 breathing exercise, a fun tap game to distract your mind, or an instant chat with our AI coach for personalized support.'))
story.append(B('Community Support: Share your journey, read success stories, and connect with others who understand what you are going through. Like, comment, and build a support network.'))
story.append(B('Global Leaderboard: Compete with quitters worldwide on the XP leaderboard. Earn experience points for every positive action and climb the ranks.'))
story.append(B('Challenge Events: Join push-up challenges and other events with prizes. Submit daily video check-ins and earn streaks to stay motivated.'))
story.append(B('AI Coach: Get personalized advice and encouragement from our AI coach, available 24/7. Ask anything about quitting, coping strategies, or health concerns.'))
story.append(B('Gamification: Earn XP and coins, unlock 19 achievement badges, claim daily rewards, and celebrate milestones with confetti animations.'))
story.append(B('Money Saved Tracker: See exactly how much money you are saving by not smoking, with daily, monthly, and yearly projections.'))

story.append(P('WHY BREATHY: Unlike simple quit-smoking trackers, Breathy addresses every aspect of quitting. We do not just count days; we help you understand the health improvements happening in your body, connect you with a community for accountability, give you tools to beat cravings in the moment, and provide AI coaching for personalized guidance. All in one app, designed with a stunning dark theme that you will actually want to open every day.'))

story.append(H2('Keywords & Metadata'))

story.append(H3('Keywords'))
story.append(P('quit smoking, stop smoking, smoke free, smoking cessation, craving help, breathing exercise, health recovery, nicotine free, lung health, AI coach, quit smoking app, smoking tracker, craving tracker, health milestones, quit community'))

story.append(H3('Feature Graphic Description'))
story.append(P('A wide banner (1024x500) featuring the Breathy logo centered on a dark gradient background (#0D1117 to #161B22). The days smoke-free counter shows "365" in large neon green (#00E676) Space Mono Bold font, surrounded by subtle glow effects. Below the counter, three stat icons show money saved, cigarettes avoided, and life regained. A tagline reads "Quit Smoking for Good" in white Montserrat Bold. The overall feel is premium, motivational, and modern.'))

story.append(H3('Screenshot Descriptions'))
story.append(P('Screenshot 1 (Home Dashboard): Shows the main dashboard with days smoke-free counter at 127 days, three stat cards (money saved: $2,413, cigarettes avoided: 1,905, life regained: 14 days), and the health timeline with the first 5 milestones completed in green. The craving SOS button pulses in the bottom-right corner.'))
story.append(P('Screenshot 2 (Community Feed): Shows the community screen with 3-4 story cards visible. One story from "Sarah M." with 45 days smoke-free shows the heart animation mid-tap. The search bar is at the top, and the FAB is visible in the corner.'))
story.append(P('Screenshot 3 (AI Coach Chat): Shows a conversation with the AI coach where the user asked "I am struggling with cravings after meals." The coach responded with practical advice and encouragement. Suggestion chips for common questions are visible below.'))
story.append(P('Screenshot 4 (Leaderboard): Shows the global leaderboard with the top 3 on a podium (gold, silver, bronze) and a scrolling list below. The current user is highlighted with a neon green border at position #47.'))
story.append(P('Screenshot 5 (Event Challenge): Shows an active push-up challenge event with 342 participants, a countdown timer (12 days remaining), and the user\'s current streak of 7 days with a check-in button.'))
story.append(P('Screenshot 6 (Profile & Achievements): Shows the user profile with 127 days smoke-free, level 8, and a horizontal scroll of unlocked achievement badges with glow effects. Three locked badges appear grayed out at the end.'))

story.append(PageBreak())

# ════════════════════════════════════════════════════════════════
# SECTION 8: POST-LAUNCH PLAN
# ════════════════════════════════════════════════════════════════
story.append(H1('8. Post-Launch Plan'))
story.append(Spacer(1, 6))

story.append(P('The post-launch strategy focuses on three pillars: continuous product improvement through data-driven iteration, sustainable user acquisition through organic and paid channels, and reliable maintenance through proactive monitoring and community management. The first 90 days after launch are critical for establishing product-market fit and building a core user base that drives organic growth through word-of-mouth and social sharing.'))

story.append(H2('8.1 Version 1.1 Roadmap'))

story.append(P('Version 1.1 is planned for release 6-8 weeks after the initial launch, incorporating user feedback from the first month and addressing the most requested features and improvements. The development priority is determined by a combination of user request volume, impact on retention, and implementation complexity.'))

v11_data = [
    ['Share Cards', 'Generate shareable achievement/milestone cards with Breathy branding for social media', 'High', '1-2 weeks'],
    ['Widgets', 'Android home screen widget showing days smoke-free and daily stats', 'High', '2 weeks'],
    ['Push Notifications', 'Customizable reminders (morning motivation, craving tips, streak warnings)', 'High', '1 week'],
    ['Groups', 'Create/join quit-smoking groups with group chat and shared events', 'Medium', '3 weeks'],
    ['Streak Recovery', 'Allow one "grace day" per month to maintain streak after relapse', 'Medium', '1 week'],
    ['Multi-language', 'Support for Spanish, French, Arabic, Hindi, and Mandarin', 'Medium', '3 weeks'],
    ['Dark/Light Toggle', 'User-selectable light mode alternative', 'Low', '2 weeks'],
    ['Apple Watch', 'Companion app for quick craving logging and breathing exercises', 'Low', '4+ weeks'],
]
story.append(Spacer(1, 6))
story.append(make_table(['Feature', 'Description', 'Priority', 'Est. Effort'], v11_data,
    col_widths=[AVAIL_W*0.15, AVAIL_W*0.45, AVAIL_W*0.12, AVAIL_W*0.13]))
story.append(Spacer(1, 6))
story.append(P('Table 13: Version 1.1 Feature Roadmap', caption_style))

story.append(H2('8.2 User Acquisition Strategy'))

story.append(H3('Organic Growth'))
story.append(P('Organic growth is the most sustainable and cost-effective acquisition channel. Breathy\'s design inherently promotes sharing through several mechanisms: (1) Share Cards allow users to post their milestones on Instagram, Twitter, and WhatsApp with Breathy branding, creating viral loops. (2) The community feature creates content that is shareable outside the app (success stories, achievement screenshots). (3) The events system encourages users to invite friends to join challenges together, creating network effects. (4) App Store Optimization (ASO) ensures Breathy ranks well for keywords like "quit smoking," "smoke free," and "smoking cessation." The target is 40% of new users coming from organic sources within the first 3 months.'))

story.append(H3('Paid Acquisition'))
story.append(P('Paid acquisition will focus on Google UAC (Universal App Campaigns) targeting users who have shown interest in health and fitness apps, with ad creatives highlighting the health timeline, craving tools, and community features. The initial budget is $500/month, scaling based on CPI (cost per install) and Day-7 retention metrics. The target CPI is under $1.50, with a Day-7 retention rate above 25%. Additionally, influencer partnerships with health and wellness YouTubers and TikTok creators can provide high-quality installs at a lower CPI than traditional paid channels.'))

story.append(H3('Referral Program'))
story.append(P('A built-in referral program rewards both the referrer and the new user. When a user shares their unique referral code and a new user signs up with it, both receive 200 bonus coins and 100 XP. The referred user also starts with a "Friend Referred" achievement badge. This creates a strong incentive for existing users to promote the app within their social circles, driving organic growth with a measurable attribution model.'))

story.append(H2('8.3 Support & Maintenance'))

story.append(P('The support and maintenance plan ensures Breathy remains stable, up-to-date, and responsive to user needs. The plan covers four areas: technical maintenance (dependency updates, bug fixes, performance optimization), user support (in-app feedback, email support, community moderation), content updates (new achievements, seasonal events, health tip refreshes), and infrastructure monitoring (Firebase usage, Cloud Function costs, crash rates).'))

story.append(P('During the first month post-launch, the development team will monitor Crashlytics dashboards daily, respond to user reviews within 24 hours, and release hotfix builds for any critical bugs within 48 hours. After the first month, monitoring transitions to a weekly cadence with monthly review meetings to assess key metrics and plan the next sprint. Community moderation will rely on a combination of automated content filtering (profanity filters) and user reporting, with a dedicated moderator reviewing flagged content daily.'))

story.append(H2('8.4 Key Analytics Events'))

analytics_data = [
    ['user_signup', 'New user registration', 'userId, method (email/google)', 'Acquisition funnel'],
    ['onboarding_complete', 'Onboarding finished', 'userId, quitType, cigsPerDay', 'Onboarding completion rate'],
    ['daily_reward_claimed', 'Daily reward claimed', 'userId, daysSinceLastClaim', 'Engagement / retention'],
    ['craving_triggered', 'Craving SOS button pressed', 'userId, copingMethod, success', 'Feature usage'],
    ['story_created', 'New story posted', 'userId, contentLength', 'Community engagement'],
    ['story_liked', 'Story liked', 'userId, storyId', 'Community engagement'],
    ['friend_request_sent', 'Friend request sent', 'userId, toUserId', 'Social graph growth'],
    ['chat_message_sent', 'Chat message sent', 'userId, chatId, messageLength', 'Social engagement'],
    ['ai_coach_message', 'AI coach message sent', 'userId, messageLength, responseTime', 'Feature usage'],
    ['event_joined', 'User joined event', 'userId, eventId', 'Event participation'],
    ['checkin_submitted', 'Video check-in submitted', 'userId, eventId, dayNumber', 'Event engagement'],
    ['achievement_unlocked', 'Achievement badge earned', 'userId, achievementId', 'Gamification effectiveness'],
    ['subscription_purchased', 'Support Me purchased', 'userId, purchaseToken', 'Revenue'],
    ['ad_shown', 'Ad displayed', 'userId, adType (open/interstitial)', 'Revenue'],
    ['session_start', 'App session started', 'userId, sessionDuration', 'Retention / engagement'],
]
story.append(Spacer(1, 6))
story.append(make_table(['Event Name', 'Description', 'Parameters', 'Purpose'], analytics_data,
    col_widths=[AVAIL_W*0.20, AVAIL_W*0.22, AVAIL_W*0.32, AVAIL_W*0.26]))
story.append(Spacer(1, 6))
story.append(P('Table 14: Key Analytics Events', caption_style))

# ── Build ──
doc.build(story)
print(f"PDF generated: {output_path}")

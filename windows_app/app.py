import os
from io import BytesIO
from typing import List, Literal

import streamlit as st
from dotenv import load_dotenv
from google import genai
from google.genai import types
from pydantic import BaseModel, Field
from pypdf import PdfReader

load_dotenv()

st.set_page_config(
    page_title="AI Study Buddy – Smart Notes to Quiz",
    page_icon="🎓",
    layout="wide",
    initial_sidebar_state="collapsed",
)

# Custom CSS: Dark navy/purple gradient backdrop with crisp white & light-grey cards
st.markdown(
    """
    <style>
    @import url('https://fonts.googleapis.com/css2?family=Outfit:wght@600;700&family=Plus+Jakarta+Sans:wght@400;500;600;700&display=swap');

    html, body, [class*="css"] {
        font-family: 'Plus Jakarta Sans', sans-serif;
    }

    .stApp {
        background: linear-gradient(155deg, #090E23 0%, #12183B 42%, #1D1748 76%, #2D1B5E 100%);
        color: #F8FAFC;
    }

    .hero-banner {
        background: rgba(255, 255, 255, 0.08);
        border: 1px solid rgba(255, 255, 255, 0.16);
        border-radius: 22px;
        padding: 26px 30px;
        margin-bottom: 22px;
        backdrop-filter: blur(10px);
        box-shadow: 0 12px 32px rgba(9, 14, 35, 0.35);
    }

    .hero-title {
        font-family: 'Outfit', sans-serif;
        font-size: 2.2rem;
        font-weight: 700;
        color: #FFFFFF;
        margin: 0;
        letter-spacing: -0.5px;
    }

    .hero-subtitle {
        font-size: 1.05rem;
        color: #CBD5E1;
        margin: 4px 0 12px 0;
    }

    .tagline-pill {
        display: inline-block;
        background: rgba(109, 74, 255, 0.28);
        border: 1px solid rgba(6, 182, 212, 0.45);
        color: #F8FAFC;
        padding: 5px 14px;
        border-radius: 999px;
        font-size: 0.88rem;
        font-weight: 600;
        margin-bottom: 10px;
    }

    .content-card {
        background: #FFFFFF;
        color: #0F172A;
        border-radius: 20px;
        padding: 22px 24px;
        box-shadow: 0 10px 28px rgba(9, 14, 35, 0.22);
        margin-bottom: 18px;
        border: 1px solid #E2E8F0;
    }

    .soft-box {
        background: #F3F5FC;
        border: 1px solid #E2E8F0;
        border-radius: 14px;
        padding: 14px 16px;
        margin-bottom: 10px;
        color: #0F172A;
    }

    .card-heading {
        font-family: 'Outfit', sans-serif;
        font-size: 1.35rem;
        font-weight: 700;
        color: #0F172A;
        margin-bottom: 12px;
    }

    .stButton > button[kind="primary"] {
        background: linear-gradient(90deg, #6D4AFF 0%, #2563EB 100%);
        color: #FFFFFF;
        border: none;
        border-radius: 14px;
        font-weight: 700;
        padding: 0.65rem 1.2rem;
        transition: transform 0.15s ease, box-shadow 0.15s ease;
    }

    .stButton > button[kind="primary"]:hover {
        transform: translateY(-1px);
        box-shadow: 0 8px 20px rgba(109, 74, 255, 0.4);
    }

    .score-banner-good {
        background: #ECFDF5;
        border: 1.5px solid #6EE7B7;
        color: #065F46;
        border-radius: 14px;
        padding: 14px 18px;
        font-weight: 700;
        margin-bottom: 14px;
    }

    .score-banner-review {
        background: #FFFBEB;
        border: 1.5px solid #FCD34D;
        color: #92400E;
        border-radius: 14px;
        padding: 14px 18px;
        font-weight: 700;
        margin-bottom: 14px;
    }
    </style>
    """,
    unsafe_allow_html=True,
)


class SummaryBullet(BaseModel):
    icon_category: Literal["concept", "formula", "process", "key_point", "tip", "takeaway"]
    text: str = Field(description="Concise bullet point in simple English")


class QuizQuestion(BaseModel):
    question: str
    options: List[str] = Field(min_length=4, max_length=4)
    correct_index: int = Field(ge=0, le=3)
    explanation: str


class KeyTerm(BaseModel):
    term: str
    meaning: str
    example: str = ""


class RevisionPack(BaseModel):
    summary_bullets: List[SummaryBullet] = Field(min_length=5, max_length=6)
    practice_quiz: List[QuizQuestion] = Field(min_length=5, max_length=5)
    key_terms: List[KeyTerm] = Field(min_length=3, max_length=3)


BULLET_ICONS = {
    "concept": "💡",
    "formula": "🧮",
    "process": "🔄",
    "key_point": "⭐",
    "tip": "✨",
    "takeaway": "✅",
}


def extract_pdf_text(uploaded_file) -> str:
    try:
        raw_bytes = uploaded_file.getvalue()
        reader = PdfReader(BytesIO(raw_bytes))
        if reader.is_encrypted:
            raise ValueError("This PDF is password-protected. Please upload an unencrypted PDF.")
        if len(reader.pages) == 0:
            raise ValueError("This PDF has no readable pages.")
        pages_text = [page.extract_text() or "" for page in reader.pages]
        combined = "\n\n".join(pages_text).strip()
        if len(combined) < 20:
            raise ValueError(
                "No selectable text was found in this PDF. Try pasting your notes directly."
            )
        return combined[:45000]
    except ValueError:
        raise
    except Exception as exc:
        raise ValueError(f"Invalid or unreadable PDF file: {exc}") from exc


def generate_revision_pack(study_text: str) -> RevisionPack:
    api_key = os.getenv("GEMINI_API_KEY", "").strip()
    if not api_key or api_key == "your_api_key_here":
        raise RuntimeError(
            "GEMINI_API_KEY is missing. Add your Gemini API key to windows_app/.env and restart."
        )

    import time

    primary_model = os.getenv("GEMINI_MODEL", "gemini-2.5-flash")
    fallback_model = os.getenv("GEMINI_FALLBACK_MODEL", "gemini-2.5-flash-lite")
    client = genai.Client(api_key=api_key)

    prompt = (
        "You are AI Study Buddy, an exam and placement revision assistant.\n"
        "Convert the study material below into a quick revision pack:\n"
        "1. summary_bullets: 5 to 6 concise bullet points in simple English.\n"
        "2. practice_quiz: Exactly 5 multiple-choice questions with 4 options each, "
        "0-based correct_index (0..3), and a short explanation.\n"
        "3. key_terms: Exactly 3 important key terms with simple meanings and a short example.\n\n"
        f"STUDY MATERIAL:\n\"\"\"\n{study_text}\n\"\"\""
    )

    config = types.GenerateContentConfig(
        temperature=0.3,
        response_mime_type="application/json",
        response_schema=RevisionPack,
    )

    last_error = None
    for idx, model_name in enumerate([primary_model, fallback_model, fallback_model]):
        try:
            response = client.models.generate_content(
                model=model_name,
                contents=prompt,
                config=config,
            )
            if not response.text:
                raise RuntimeError("Gemini returned an empty response. Please try again.")
            return RevisionPack.model_validate_json(response.text)
        except Exception as exc:
            last_error = exc
            msg = str(exc).lower()
            transient = any(
                token in msg for token in ("high demand", "overloaded", "unavailable", "503", "429", "try again")
            )
            if not transient or idx == 2:
                raise
            time.sleep(1.2)

    raise RuntimeError(f"Generation failed: {last_error}")


def format_summary_txt(pack: RevisionPack, source_name: str) -> str:
    lines = [
        "AI STUDY BUDDY - SMART SUMMARY",
        f"Source: {source_name}",
        "=" * 50,
        "",
    ]
    for idx, bullet in enumerate(pack.summary_bullets, start=1):
        lines.append(f"{idx}. {bullet.text}")
    return "\n".join(lines)


def format_complete_pack_txt(pack: RevisionPack, source_name: str) -> str:
    letters = ["A", "B", "C", "D"]
    lines = [
        "AI STUDY BUDDY - COMPLETE REVISION PACK",
        f"Source: {source_name}",
        "=" * 50,
        "",
        "1. SMART SUMMARY",
        "-" * 50,
    ]
    for idx, bullet in enumerate(pack.summary_bullets, start=1):
        lines.append(f"{idx}. {bullet.text}")

    lines.extend(["", "2. PRACTICE QUIZ (5 MCQs)", "-" * 50])
    for idx, q in enumerate(pack.practice_quiz, start=1):
        lines.append(f"Q{idx}. {q.question}")
        for opt_idx, opt in enumerate(q.options):
            lines.append(f"   {letters[opt_idx]}) {opt}")
        lines.append(f"   Answer: {letters[q.correct_index]}) {q.options[q.correct_index]}")
        lines.append(f"   Explanation: {q.explanation}")
        lines.append("")

    lines.extend(["3. IMPORTANT KEY TERMS", "-" * 50])
    for idx, term in enumerate(pack.key_terms, start=1):
        lines.append(f"{idx}. {term.term}")
        lines.append(f"   Meaning: {term.meaning}")
        if term.example.strip():
            lines.append(f"   Example: {term.example}")
        lines.append("")
    return "\n".join(lines)


if "revision_pack" not in st.session_state:
    st.session_state.revision_pack = None
if "source_name" not in st.session_state:
    st.session_state.source_name = "Pasted Notes"
if "quiz_submitted" not in st.session_state:
    st.session_state.quiz_submitted = False
if "notes_text" not in st.session_state:
    st.session_state.notes_text = ""

# Header
st.markdown(
    """
    <div class="hero-banner">
        <div class="tagline-pill">✨ Study smarter, revise faster.</div>
        <h1 class="hero-title">🎓 AI STUDY BUDDY</h1>
        <p class="hero-subtitle">Turn your notes into smarter revisions.</p>
        <p style="margin:0; color:#E2E8F0; font-size:0.95rem;">
            Paste your chapter notes or upload a PDF document to instantly generate a smart summary,
            a 5-question interactive quiz, and key terms for exams and placements.
        </p>
    </div>
    """,
    unsafe_allow_html=True,
)

if st.session_state.revision_pack is None:
    st.markdown('<div class="content-card">', unsafe_allow_html=True)
    st.markdown('<div class="card-heading">📄 Upload Your Study Material</div>', unsafe_allow_html=True)

    tab_notes, tab_pdf = st.tabs(["✏️ Paste Notes", "📑 Upload PDF"])
    extracted_pdf_text = ""
    uploaded_pdf_name = None

    with tab_notes:
        notes_input = st.text_area(
            "Chapter Notes",
            key="notes_text",
            height=220,
            placeholder="Paste your chapter notes or study material here...",
            label_visibility="collapsed",
        )
        st.caption(f"Character count: {len(notes_input):,} chars")

    with tab_pdf:
        pdf_file = st.file_uploader("Choose a PDF file", type=["pdf"])
        if pdf_file is not None:
            uploaded_pdf_name = pdf_file.name
            try:
                extracted_pdf_text = extract_pdf_text(pdf_file)
                st.success(f"Loaded PDF: **{pdf_file.name}** ({len(extracted_pdf_text):,} chars extracted)")
                with st.expander("Preview Extracted PDF Text", expanded=True):
                    preview = extracted_pdf_text[:1500] + ("..." if len(extracted_pdf_text) > 1500 else "")
                    st.text(preview)
            except ValueError as err:
                st.error(str(err))

    col_gen, col_clear = st.columns([3, 1])
    with col_gen:
        generate_clicked = st.button("✨ GENERATE MY REVISION PACK", type="primary", use_container_width=True)
    with col_clear:
        if st.button("🗑️ Clear / Reset", use_container_width=True):
            st.session_state.notes_text = ""
            st.session_state.revision_pack = None
            st.session_state.quiz_submitted = False
            st.rerun()

    if generate_clicked:
        study_payload = ""
        source_label = "Pasted Notes"
        if extracted_pdf_text.strip():
            study_payload = extracted_pdf_text.strip()
            source_label = uploaded_pdf_name or "Uploaded PDF"
        elif st.session_state.notes_text.strip():
            study_payload = st.session_state.notes_text.strip()
            source_label = "Pasted Notes"

        if len(study_payload) < 20:
            st.warning("Please paste at least 20 characters of notes or upload a valid PDF first.")
        else:
            with st.spinner("Gemini AI is reading your notes and building your revision pack..."):
                try:
                    pack = generate_revision_pack(study_payload)
                    st.session_state.revision_pack = pack
                    st.session_state.source_name = source_label
                    st.session_state.quiz_submitted = False
                    st.rerun()
                except Exception as exc:
                    st.error(f"Could not generate revision pack: {exc}")

    st.markdown("</div>", unsafe_allow_html=True)

else:
    pack: RevisionPack = st.session_state.revision_pack
    source_name = st.session_state.source_name
    letters = ["A", "B", "C", "D"]

    left_col, right_col = st.columns([1.05, 1.15], gap="large")

    with left_col:
        # CARD 1: SMART SUMMARY
        st.markdown('<div class="content-card">', unsafe_allow_html=True)
        st.markdown('<div class="card-heading">💡 Smart Summary</div>', unsafe_allow_html=True)
        for bullet in pack.summary_bullets:
            icon = BULLET_ICONS.get(bullet.icon_category, "💡")
            st.markdown(f'<div class="soft-box">{icon} {bullet.text}</div>', unsafe_allow_html=True)
        st.download_button(
            "⬇️ Download Summary (.txt)",
            data=format_summary_txt(pack, source_name),
            file_name="study_buddy_summary.txt",
            mime="text/plain",
            use_container_width=True,
        )
        st.markdown("</div>", unsafe_allow_html=True)

        # CARD 3: KEY TERMS
        st.markdown('<div class="content-card">', unsafe_allow_html=True)
        st.markdown('<div class="card-heading">🔑 Important Key Terms</div>', unsafe_allow_html=True)
        for idx, term in enumerate(pack.key_terms, start=1):
            example_html = (
                f"<div style='margin-top:6px; color:#2563EB; font-size:0.9rem;'><strong>Example:</strong> {term.example}</div>"
                if term.example.strip()
                else ""
            )
            st.markdown(
                f"""
                <div class="soft-box">
                    <div style="font-weight:700; color:#5433E6;">0{idx}. {term.term}</div>
                    <div style="margin-top:4px; color:#334155;">{term.meaning}</div>
                    {example_html}
                </div>
                """,
                unsafe_allow_html=True,
            )
        st.markdown("</div>", unsafe_allow_html=True)

    with right_col:
        # CARD 2: PRACTICE QUIZ
        st.markdown('<div class="content-card">', unsafe_allow_html=True)
        st.markdown('<div class="card-heading">📝 Practice Quiz (5 MCQs)</div>', unsafe_allow_html=True)

        user_answers = {}
        for q_idx, q in enumerate(pack.practice_quiz):
            formatted_options = [f"{letters[i]}) {opt}" for i, opt in enumerate(q.options)]
            choice = st.radio(
                f"Q{q_idx + 1}. {q.question}",
                options=formatted_options,
                index=None,
                key=f"quiz_q_{q_idx}",
                disabled=st.session_state.quiz_submitted,
            )
            if choice is not None:
                user_answers[q_idx] = formatted_options.index(choice)

            if st.session_state.quiz_submitted:
                chosen_idx = user_answers.get(q_idx)
                if chosen_idx == q.correct_index:
                    st.success(f"Correct! {q.explanation}")
                else:
                    correct_label = f"{letters[q.correct_index]}) {q.options[q.correct_index]}"
                    st.error(f"Incorrect. Correct Answer: {correct_label} — {q.explanation}")
            st.divider()

        if not st.session_state.quiz_submitted:
            if st.button("✅ Submit Answers", type="primary", use_container_width=True):
                if len(user_answers) < len(pack.practice_quiz):
                    st.warning("Please answer all 5 questions before submitting.")
                else:
                    st.session_state.quiz_submitted = True
                    st.rerun()
        else:
            score = sum(
                1 for idx, q in enumerate(pack.practice_quiz) if user_answers.get(idx) == q.correct_index
            )
            pct = int((score / len(pack.practice_quiz)) * 100)
            banner_cls = "score-banner-good" if score >= 4 else "score-banner-review"
            st.markdown(
                f'<div class="{banner_cls}">🏆 Final Score: {score} / {len(pack.practice_quiz)} ({pct}%)</div>',
                unsafe_allow_html=True,
            )
            if st.button("🔄 Retake Quiz", use_container_width=True):
                for q_idx in range(len(pack.practice_quiz)):
                    st.session_state.pop(f"quiz_q_{q_idx}", None)
                st.session_state.quiz_submitted = False
                st.rerun()

        st.markdown("</div>", unsafe_allow_html=True)

    # Bottom Export & Reset Controls
    b1, b2 = st.columns(2)
    with b1:
        st.download_button(
            "📦 Download Complete Revision Pack (.txt)",
            data=format_complete_pack_txt(pack, source_name),
            file_name="study_buddy_revision_pack.txt",
            mime="text/plain",
            use_container_width=True,
        )
    with b2:
        if st.button("✨ Generate New Revision Pack", type="primary", use_container_width=True):
            for q_idx in range(5):
                st.session_state.pop(f"quiz_q_{q_idx}", None)
            st.session_state.revision_pack = None
            st.session_state.quiz_submitted = False
            st.rerun()

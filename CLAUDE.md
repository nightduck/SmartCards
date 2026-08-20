## Project
SmartCards is a fork of AnkiDroid (`upstream` remote), repurposed as a language-learning
flashcard app. This is a solo, deliberately AI-driven project — it does not use AnkiDroid's
upstream `AI_POLICY.md` contributor restrictions.

### Roadmap
- **Multi-modal cards**: beyond AnkiDroid's existing two-sided text cards, support audio-only
  prompts (no text cue), image-cued prompts, an example sentence field, and a collapsible
  explainer (meaning / context / synonyms).
- **Capture**: share-sheet integration — share a foreign word from anywhere on the phone to create
  a new card in SmartCards.
- **AI generation**: given a captured/created word, auto-generate its translation and all of the
  above card content (image, example sentence, explainer).
- **Backend**: an eventual service to field these AI generation requests, enabling a free app with
  a paid "AI-enhanced experience" subscription tier.

These are directional, not committed specs.

## Refactoring Scope
- Constrain scope tightly: do not modify unrelated files, themes, or settings 'while you're in there'.

## Verification
- For bug fixes, write the failing regression test FIRST and confirm it fails before applying the fix.

## Git workflow
- Never commit or push directly to `main`. Always work on a branch and open a PR.
- Never merge a PR yourself (`gh pr merge`) — only the repo owner merges, after review.
- Before opening a PR, run the checks in `.github/workflows/README.md#quality-checks`
  (`./gradlew lintAll ktLintCheck lint-rules:test`, `./gradlew jacocoUnitTestReport`).
- Before considering a PR ready for human review, run it through the `pr-full-review` skill
  (`.agents/skills/pr-full-review`) against the current branch.

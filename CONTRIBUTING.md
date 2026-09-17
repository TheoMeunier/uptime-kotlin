# Contributing

Thank you for considering contributing to **Uptime Kotlin**
Contributions of all kinds are welcome and greatly appreciated.

This document provides guidelines to help you contribute effectively.

---

## Code of Conduct

By participating in this project, you agree to abide by the
[Code of Conduct](CODE_OF_CONDUCT.md).

Please be respectful and constructive in all interactions.

---

## How Can I Contribute?

You can contribute in many ways, including:

- Reporting bugs
- Suggesting new features or improvements
- Improving documentation
- Fixing bugs
- Adding new features
- Reviewing pull requests
- Translating the interface, or correcting an existing translation

---

## Reporting Bugs 🐛

Before opening a bug report:

- Check if the issue has already been reported
- Make sure you are using the latest version
- Use the **Bug Report** issue template

Please include as much information as possible:

- Steps to reproduce the issue
- Expected behavior
- Actual behavior
- Logs, screenshots, or error messages if applicable
- Your environment (OS, browser, Docker version, etc.)

---

## Suggesting Features ✨

Feature requests are welcome!

When submitting a feature request, please:

- Clearly describe the problem you are trying to solve
- Explain your proposed solution
- Mention any alternative solutions you have considered

Use the **Feature Request** issue template when available.

---

## Development Setup 🛠️

### Prerequisites

- Node.js (LTS)
- Java 21
- Docker & Docker Compose
- PostgreSQL

### Local Development

1. Fork the repository
2. Clone your fork
   ```bash
   git clone https://github.com/your-username/uptime-kotlin.git
   ```
3. Create a new branch
   ```bash
   git checkout -b my-feature-branch
   ```
4. Follow the instructions in the README to start the project locally

---

## Translations 🌍

The interface lives in `apps/app/src/lang/`. `en.ts` is the reference: every other locale is
typed `typeof en`, so a missing or extra key is a compile error rather than a string that
silently falls back at runtime.

### Correcting an existing translation

`fr.ts`, `de.ts` and `es.ts` were not written by native speakers. Corrections are welcome and
do not need an issue first — open a pull request against the file. Domain vocabulary is where
they are most likely wrong: _probe_, _retry_, _heartbeat_, _degraded_, _uptime_.

### Adding a language

1. Copy `en.ts` to `<code>.ts` (ISO 639-1: `it.ts`, `pt.ts`, `nl.ts`…) and translate the values.
   Keep every key, and keep the `{{placeholders}}` exactly as they appear in `en.ts`.
2. Keep the `_one` / `_other` suffixes on plural keys. If your language has more plural forms
   than English, add the ones it needs — i18next uses the CLDR categories (`_zero`, `_two`,
   `_few`, `_many`).
3. Register it in `i18n.ts`: add the import, the entry in `resources`, the code in
   `SUPPORTED_LANGUAGES`, and the matching Zod locale in `ZOD_LOCALES` (Zod ships its own
   locales; without that entry, form validation messages stay English).
4. Add the language's own name to `layout.language.<code>` in **every** locale file — it is
   always written in that language (`Italiano`, not `Italian`).
5. Run `npm run build` in `apps/app`. TypeScript will list anything missing.

The selector picks it up automatically: it is rendered from `SUPPORTED_LANGUAGES`.

### What is not supported yet

Right-to-left languages. The layout uses physical CSS properties (`ml-auto`, `text-left`,
`right-4`), so Arabic or Hebrew need a pass over the whole style layer first, not just a
language file.

### Where the choice is stored

In `localStorage`, per browser — not on the user account. First visit falls back to the
browser language, then to English. Users change it in **Profile → Preferences**, and on the
login, setup and public status pages, which have no profile to reach.

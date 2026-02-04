# Contributing Guide

Thanks for your interest in contributing to `cola-attendance`.

## How to Contribute

1. **Fork this repository** and clone your fork locally.
2. **Create a branch**: `git checkout -b feature/your-feature` or `fix/your-fix`.
3. **Implement and self-test**:
   - Backend: run `mvn test`
   - Frontend/E2E: verify related flows under `attendance-frontend` and `scripts/`
4. **Commit your changes** with clear messages (you can reference items in `TASKS.md`).
5. **Push to your fork** and open a Pull Request with a short summary of purpose, scope, and test results.

## Commit Convention

Use lowercase prefixes with a colon:

- `feat:` new feature
- `fix:` bug fix
- `docs:` documentation update
- `refactor:` code refactor (no external behavior change)
- `test:` tests only
- `chore:` build/tooling/dependency/config housekeeping

Examples:

- `feat: add duty roster export API`
- `fix: handle null shift in attendance result`
- `docs: update docker quick start`

## Project Conventions

- Backend: Java 21, Spring Boot 3, MyBatis-Plus. Keep package structure and code style consistent.
- Frontend: Vue 3 + Vite + Element Plus. Follow existing folder layout and API wrapper style.
- Secrets: do not commit real passwords, tokens, or keys. Use environment variables/placeholders (see README).

## Discussions

For feature proposals or bug reports, please open an Issue.

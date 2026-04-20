# company module

This file supplements the root `CLAUDE.md`. Read that first — this only covers what differs or needs clarification for this module specifically.

## What this module does

Manages companies, their members, and friend relationships between members. Runs on port 8084 (`etcompany`). Depends on `userKc` and `schema` being up.

## Repository layer — correction to root CLAUDE.md

The root doc describes repositories as using `EntityManager` directly. **That is not true here.** All repositories in this module (`CompanyRepositoryImplementation`, `FriendRepositoryImplementation`) are API clients that delegate every CRUD operation to the `schema` microservice via `ApiSchema`.

There is no direct database access in this module.

## NoJwt pattern

Some repository methods have a `NoJwt` suffix (e.g., `findNoJwt`, `findByNameNoJwt`, `findMemberByUserIdNoJwt`). These use the application's own OAuth2 client registration instead of propagating the current user's JWT.

Use `NoJwt` variants for internal/cascade operations where no user JWT is in scope — for example, validating a member's eligibility inside `addMember()` before the new member has been accepted, or cascading friend deletions when removing a member. Use the standard (JWT-propagating) variants for all user-facing calls.

## Status enums and state machines

**CompanyMemberStatus**: `REQUESTED` → `ACCEPTED` ↔ `BLOCKED`

**CompanyFriendStatus**: `REQUESTED` → `ACCEPTED` ↔ `BLOCKED_BY_SENDER` / `BLOCKED_BY_RECEIVER`

Friend blocking is asymmetric:
- Either participant can block; the status records *who* blocked (`BLOCKED_BY_SENDER` vs `BLOCKED_BY_RECEIVER`).
- Only the user who blocked can unblock. Attempting to unblock a friendship you did not block throws `FORBIDDEN`.

## Member policy

**CompanyMemberPolicy**: `PUBLIC`, `REGISTRATION_REQUIRED`, `AUTHORIZATION_REQUIRED`

`PUBLIC` and `REGISTRATION_REQUIRED` auto-accept new members (status goes straight to `ACCEPTED`). `AUTHORIZATION_REQUIRED` leaves them at `REQUESTED` until a manager explicitly accepts.

## Authorization model

`isValidMember` — user has exactly one `ACCEPTED` member record for the company.

`isCompanyManager` — user is a valid member AND is either the owner, a direct editor, or a member of an editor group (`DbGroup`).

Key invariant: the company owner can never be removed or blocked.

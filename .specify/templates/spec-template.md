# Feature Specification: [FEATURE NAME]

**Feature Branch**: `[###-feature-name]`  
**Created**: [DATE]  
**Status**: Draft  
**Input**: User description: "$ARGUMENTS"

## Delivery Scope *(mandatory)*

<!--
Define product surfaces, not implementation modules. Be explicit so planning cannot silently
expand the feature.

The web application is out of scope by default under the current constitution. Mark it in
scope only when the feature request explicitly requires web implementation changes.
-->

| Surface / Consumer | In Scope? | Expected Outcome or Compatibility Obligation |
|--------------------|-----------|----------------------------------------------|
| Android application | [Yes/No] | [Outcome or reason] |
| iOS application | [Yes/No] | [Outcome or reason] |
| Server-observable behavior | [Yes/No] | [Outcome or reason] |
| Existing web application | [No unless explicit] | [Must remain compatible / explicitly included behavior] |
| Other consumers | [Yes/No/N/A] | [Consumer and expectation] |

### Out of Scope

- [Explicitly excluded behavior or surface]
- [Deferred behavior]
- [Non-goal that might otherwise be assumed]

## User Scenarios and Testing *(mandatory)*

<!--
Prioritize journeys P1, P2, P3, and so on. Each story must be independently demonstrable and
testable. A P1 story should provide a viable first increment rather than merely infrastructure.
-->

### User Story 1 - [Brief Title] (Priority: P1)

[Describe the user journey and value in plain language.]

**Why this priority**: [Why this is the most valuable or necessary slice.]

**Independent Test**: [Specific action and observable outcome proving this story works alone.]

**Acceptance Scenarios**:

1. **Given** [initial state], **When** [action], **Then** [observable outcome].
2. **Given** [initial state], **When** [action], **Then** [observable outcome].

---

### User Story 2 - [Brief Title] (Priority: P2)

[Describe the user journey and value in plain language.]

**Why this priority**: [Reason for priority.]

**Independent Test**: [Specific independent verification.]

**Acceptance Scenarios**:

1. **Given** [initial state], **When** [action], **Then** [observable outcome].

---

### User Story 3 - [Brief Title] (Priority: P3)

[Describe the user journey and value in plain language.]

**Why this priority**: [Reason for priority.]

**Independent Test**: [Specific independent verification.]

**Acceptance Scenarios**:

1. **Given** [initial state], **When** [action], **Then** [observable outcome].

---

[Add or remove user stories as required.]

### Edge Cases and Failure Outcomes

<!-- Replace all placeholders with feature-specific behavior. -->

- What happens when [boundary condition]?
- What happens when required data is missing, stale, duplicated, or unavailable?
- What does the user observe when the operation is rejected or fails?
- How is retry, cancellation, timeout, or offline behavior presented where relevant?
- What happens when an older active client encounters newly added data or behavior?
- What privacy or authorization edge cases apply?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST [specific capability and observable behavior].
- **FR-002**: The system MUST [validation or decision rule].
- **FR-003**: A user MUST be able to [key interaction].
- **FR-004**: The system MUST [persistence or state-transition behavior].
- **FR-005**: The system MUST [failure or recovery behavior].

Use `[NEEDS CLARIFICATION: ...]` only when the ambiguity materially changes scope, user
behavior, security, compatibility, or acceptance.

### Security and Privacy Requirements *(include when applicable)*

<!-- Describe required behavior, not framework configuration. -->

- **SR-001**: The system MUST [who may perform which operation].
- **SR-002**: The system MUST [ownership or privacy rule].
- **SR-003**: The system MUST NOT expose [sensitive information] to [consumer/user].
- **SR-004**: A rejected operation MUST produce [safe user-visible outcome].

### Compatibility Requirements *(include when applicable)*

- **CR-001**: Existing supported clients MUST [continue working / receive fallback behavior].
- **CR-002**: The existing web application MUST remain unchanged and compatible unless it is explicitly in scope.
- **CR-003**: A breaking behavior change MUST define migration and rollout expectations.

### Data Requirements *(include when the feature creates or changes data)*

- **DR-001**: The system MUST preserve [data/invariant].
- **DR-002**: Existing records MUST [remain valid / be migrated / receive a defined default].
- **DR-003**: Data deletion, retention, or restoration MUST behave as follows: [behavior].

### Key Entities *(include if the feature involves data)*

- **[Entity 1]**: [What it represents, important attributes, invariants, and relationships.]
- **[Entity 2]**: [What it represents and how it relates to other entities.]

Do not include database tables, framework classes, DTO names, or implementation-specific
field types in this section.

## Dependencies

- [Existing product capability or external service required]
- [Upstream decision or feature dependency]
- [Operational dependency]

## Assumptions

- [Reasonable product or user assumption]
- [Scope assumption]
- [Compatibility assumption]
- [Data or environment assumption]

## Success Criteria *(mandatory)*

<!--
Success criteria must be measurable, user- or system-observable, and technology-agnostic.
Do not define success as "endpoint exists", "uses Ktor", or "tests pass".
-->

### Measurable Outcomes

- **SC-001**: [Primary journey can be completed under a measurable condition.]
- **SC-002**: [Reliability, correctness, or completion-rate outcome.]
- **SC-003**: [User-facing latency or responsiveness outcome, where relevant.]
- **SC-004**: [Compatibility, support, or business outcome.]

## Requirement Traceability

| User Story | Functional Requirements | Security / Compatibility / Data Requirements | Success Criteria |
|------------|-------------------------|----------------------------------------------|------------------|
| US1 | [FR-...] | [SR-/CR-/DR-...] | [SC-...] |
| US2 | [FR-...] | [SR-/CR-/DR-...] | [SC-...] |
| US3 | [FR-...] | [SR-/CR-/DR-...] | [SC-...] |
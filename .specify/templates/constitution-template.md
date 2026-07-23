<!--
Sync Impact Report
- Version change: [OLD_VERSION] -> [NEW_VERSION]
- Modified principles:
  - [PRINCIPLE_OR_NONE]
- Added sections:
  - [SECTION_OR_NONE]
- Removed sections:
  - [SECTION_OR_NONE]
- Templates requiring updates:
  - [✅/⚠] .specify/templates/plan-template.md
  - [✅/⚠] .specify/templates/spec-template.md
  - [✅/⚠] .specify/templates/tasks-template.md
  - [✅/⚠] .specify/templates/checklist-template.md
  - [✅/⚠] .specify/templates/commands/*.md
- Follow-up TODOs:
  - [TODO_OR_NONE]
-->

# [PROJECT_NAME] Constitution

## Core Principles

### I. [PRINCIPLE_1_NAME]

[PRINCIPLE_1_RULES]

Rationale: [WHY_THIS_PRINCIPLE_EXISTS]

### II. [PRINCIPLE_2_NAME]

[PRINCIPLE_2_RULES]

Rationale: [WHY_THIS_PRINCIPLE_EXISTS]

### III. [PRINCIPLE_3_NAME]

[PRINCIPLE_3_RULES]

Rationale: [WHY_THIS_PRINCIPLE_EXISTS]

### IV. [PRINCIPLE_4_NAME]

[PRINCIPLE_4_RULES]

Rationale: [WHY_THIS_PRINCIPLE_EXISTS]

### V. [PRINCIPLE_5_NAME]

[PRINCIPLE_5_RULES]

Rationale: [WHY_THIS_PRINCIPLE_EXISTS]

## Technical and Architecture Guardrails

[TECHNOLOGY_AND_ARCHITECTURE_RULES]

<!--
Document stable, enforceable rules such as:
- module ownership and allowed dependency directions;
- platform-neutral versus platform-specific code;
- client/server separation;
- API contract and serialization conventions;
- dependency-injection boundaries;
- persistence and migration rules;
- compatibility obligations for active consumers.

Do not use this section as a dependency-version catalogue or feature specification.
-->

## Workflow and Review Standards

[WORKFLOW_AND_REVIEW_RULES]

<!--
Include:
- inspection before implementation;
- required plan and task checks;
- test and verification expectations;
- compatibility and security review;
- handling of generated sources;
- delivery evidence.
-->

## Definition of Done

[DEFINITION_OF_DONE]

<!--
Define observable completion gates. A change should not be complete merely because code
was written. Include applicable compilation, tests, migrations, security, compatibility,
documentation, and verification reporting.
-->

## Governance

This constitution is the authoritative engineering policy for [PROJECT_NAME] and supersedes
conflicting local conventions, plans, task lists, and ad-hoc practices.

### Amendment Process

[AMENDMENT_PROCESS]

<!--
Every amendment should include rationale, impacted sections, compatibility impact,
migration guidance, affected templates, semantic version impact, and an updated
Sync Impact Report.
-->

### Governance Exceptions

[EXCEPTION_PROCESS]

<!--
A temporary exception should identify:
- the exact rule being violated;
- why compliance is currently impractical;
- affected modules/files;
- security, compatibility, and maintenance risks;
- approving reviewer;
- expiration date or removal condition;
- remediation task;
- expected compliant end state.
-->

### Versioning Policy

- **MAJOR**: backward-incompatible governance changes, principle removals, or fundamental
  redefinitions.
- **MINOR**: new principles, new mandatory sections, or material expansion of obligations.
- **PATCH**: clarifications, wording corrections, and non-semantic edits.

### Compliance Reviews

[COMPLIANCE_REVIEW_RULES]

**Version**: [CONSTITUTION_VERSION]  
**Ratified**: [RATIFICATION_DATE]  
**Last Amended**: [LAST_AMENDED_DATE]
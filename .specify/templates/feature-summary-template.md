# Feature Summary and Audit: [FEATURE NAME]

**Generated**: [DATE]  
**Repository state**: [branch and clean/dirty summary]  
**Analysis mode**: [Static only / Static plus verification]  
**Feature scope**: [Modules and product surfaces]  
**Overall confidence**: [High / Medium / Low]

## 1. Executive Summary

[Explain in plain language what the feature does, how complete the traced flow is, which
clients use it, what security level applies, and the most important findings.]

### Key Facts

| Area | Summary |
|------|---------|
| User-visible purpose | [Summary] |
| Primary entry point | [Screen/navigation entry] |
| Server API | [Routes] |
| Authentication | [Mechanism] |
| Authorization | [Policy] |
| Persistence | [Tables/entities] |
| Main risks | [Summary] |
| Verification | [Executed or recommended] |

## 2. Scope and Evidence Map

### Included

| Module | Relevant files/symbols | Role in feature |
|--------|-------------------------|-----------------|
| `core` | [Paths] | [Contracts/domain] |
| `app/shared` | [Paths] | [UI/client flow] |
| `app/androidApp` | [Paths or N/A] | [Platform wiring] |
| `app/iosApp` | [Paths or N/A] | [Platform wiring] |
| `server` | [Paths] | [Routes/business/persistence] |
| `webApp` | [Read-only compatibility evidence or N/A] | [Consumer impact] |

### Excluded

- [Related code deliberately excluded and why]

## 3. User-Visible Behavior

[Describe the normal user journey.]

### States

- **Initial**: [Behavior]
- **Loading**: [Behavior]
- **Content**: [Behavior]
- **Empty**: [Behavior]
- **Refresh**: [Behavior]
- **Load more**: [Behavior]
- **Error**: [Behavior]
- **Offline**: [Behavior or unknown]

## 4. Architecture and End-to-End Flow

### App Flow

```text
[Screen]
-> [ViewModel]
-> [UseCase]
-> [Repository]
-> [Ktor client/data source]
-> [Contract]
```

### Server Flow

```text
[Route]
-> [Authentication]
-> [Authorization/privacy]
-> [UseCase/service]
-> [Repository]
-> [Exposed mapping/transaction]
-> [Tables]
```

### Sequence

1. [Step with path/symbol evidence]
2. [Step with path/symbol evidence]
3. [Step with path/symbol evidence]

## 5. App Components

| Component | Path / symbol | Responsibility | Important behavior |
|-----------|---------------|----------------|--------------------|
| Screen | [Evidence] | [Role] | [Behavior] |
| ViewModel | [Evidence] | [Role] | [Behavior] |
| Use case | [Evidence] | [Role] | [Behavior] |
| Repository | [Evidence] | [Role] | [Behavior] |
| Data source | [Evidence] | [Role] | [Behavior] |

## 6. Server Components

| Component | Path / symbol | Responsibility | Important behavior |
|-----------|---------------|----------------|--------------------|
| Route | [Evidence] | [Role] | [Behavior] |
| Use case/service | [Evidence] | [Role] | [Behavior] |
| Repository | [Evidence] | [Role] | [Behavior] |
| Persistence mapping | [Evidence] | [Role] | [Behavior] |
| Tables/entities | [Evidence] | [Role] | [Behavior] |

## 7. Route Inventory

| Method | Route | Caller | Request | Response | Auth provider | Authorization | Evidence |
|--------|-------|--------|---------|----------|---------------|---------------|----------|
| [GET] | [/path] | [Caller] | [Params/body] | [Contract] | [Provider/public] | [Policy] | [Path:line] |

### Route Error Behavior

| Route | Condition | Status / error contract | Evidence |
|-------|-----------|-------------------------|----------|
| [Route] | [Failure] | [Response] | [Path:line] |

## 8. Authentication and Authorization Matrix

| Operation | Authentication required? | Trusted identity source | Authorization/privacy rule | Enforcement location | Assessment |
|-----------|--------------------------|-------------------------|----------------------------|----------------------|------------|
| View feed | [Yes/No] | [Principal/session] | [Policy] | [Evidence] | [Adequate/Issue/Unknown] |

Clearly separate:

- authentication;
- ownership;
- relationship policy;
- privacy filtering;
- privileged/admin access.

## 9. Contracts and Data Model

### Shared Contracts

| Contract | Path | Used by | Compatibility notes |
|----------|------|---------|---------------------|
| [Type] | [Evidence] | [App/server] | [Defaults/enums/sealed types] |

### Persistence

| Table/entity | Path | Feature role | Important constraints/indexes |
|--------------|------|--------------|-------------------------------|
| [Name] | [Evidence] | [Role] | [Constraints] |

### Mapping

[Explain persistence -> domain/contract mapping and whether sensitive/internal fields remain
isolated.]

## 10. Pagination, Ordering, Filtering and Refresh

- **Pagination model**: [Offset/cursor/none]
- **Page-size validation**: [Behavior/evidence]
- **Ordering**: [Fields and stability]
- **Visibility filters**: [Behavior/evidence]
- **Deduplication**: [Behavior/evidence]
- **Refresh semantics**: [Replace/merge/reset]
- **Load-more semantics**: [Append/cursor handling]
- **Concurrent update behavior**: [Observed or unknown]

## 11. Dependency-Injection Wiring

| Dependency | Interface | Implementation | Koin module | Scope | Evidence |
|------------|-----------|----------------|-------------|-------|----------|
| [Dependency] | [Type] | [Type] | [Module] | [single/factory/etc.] | [Path:line] |

[Note missing, duplicate or cross-container bindings.]

## 12. Error Handling and Observability

- safe client-facing errors;
- internal exception handling;
- sensitive logging risks;
- loading/error state propagation;
- retry and timeout behavior;
- metrics or logs relevant to diagnosing the feature.

## 13. Tests and Verification

### Existing Tests

| Test | Path | Behavior covered | Important gaps |
|------|------|------------------|----------------|
| [Test] | [Evidence] | [Coverage] | [Gap] |

### Commands Executed

```text
[Command and result]
```

### Recommended Commands Not Executed

```bash
[Exact discovered Gradle commands]
```

### Verification Limits

[Environment limitations and remaining unverified runtime behavior.]

## 14. Bug and Risk Findings

### Critical

#### [FEATURE]-BUG-001 — [Title]

- **Classification**: [Confirmed bug / Probable bug / Design or operational risk / Test-coverage gap]
- **Severity**: Critical
- **Confidence**: [High/Medium/Low]
- **Evidence**: [Exact paths, lines and symbols]
- **Trigger**: [Scenario]
- **Actual behavior**: [Behavior]
- **Expected behavior**: [Behavior]
- **Impact**: [Users/data/security]
- **Correction direction**: [Non-implemented recommendation]
- **Regression test**: [Test scenario]

### High

[Findings or "No credible high-severity findings identified."]

### Medium

[Findings or "No credible medium-severity findings identified."]

### Low

[Findings or "No credible low-severity findings identified."]

### Coverage Gaps Without Proven Defect

- [Gap with evidence]

## 15. Unknowns and Assumptions

| Item | Why unresolved | Evidence needed |
|------|----------------|-----------------|
| [Unknown] | [Reason] | [Needed evidence] |

## 16. Recommended Next Actions

1. [Highest-value confirmed correction or verification]
2. [Regression/security/contract test]
3. [Documentation or follow-up analysis]

## Final Assessment

[Summarize reliability, security enforcement, compatibility, important findings and limits.
State explicitly that no-found-bugs does not prove defect-free behavior.]
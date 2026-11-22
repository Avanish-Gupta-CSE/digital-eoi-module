---
trigger: always_on
description: Karpathy behavioral coding guidelines enforcing disciplined, minimal, surgical, and verified changes.
---

# Karpathy Behavioral Guidelines for Antigravity Agents

These guidelines enforce strict discipline to eliminate common LLM coding pitfalls, based on Andrej Karpathy's principles.

## 1. Think Before Coding
**Don't assume. Don't hide confusion. Surface tradeoffs.**
- State assumptions explicitly before taking action. If uncertain, ask.
- If multiple interpretations exist, present them — do not pick silently.
- Push back with a simpler approach if an over-complicated design is requested.
- If requirements are ambiguous, stop, name what is confusing, and clarify.

## 2. Simplicity First
**Minimum code that solves the problem. Nothing speculative.**
- No unrequested features or scope creep.
- No abstractions for single-use code (YAGNI).
- No unrequested configurability, "flexibility", or speculative generic layers.
- No defensive coding for impossible scenarios.
- If 200 lines could be 50 lines, rewrite to 50 lines.

## 3. Surgical Changes
**Touch only what you must. Clean up only your own mess.**
- Do not "improve", reformat, or refactor adjacent or unrelated code/comments.
- Match existing codebase conventions, even if differing from personal preference.
- Leave unrelated dead code alone; mention it instead of deleting it.
- Remove only the imports/variables/functions orphaned by your own changes.
- Every edited line must trace directly to the user's prompt.

## 4. Goal-Driven Execution
**Define success criteria. Loop until verified.**
- Transform vague goals into verifiable criteria (reproducing test, compilation check, assertion).
- Structure multi-step work into: `[Step] → verify: [check]`.
- Loop and verify independently until the criteria pass before declaring completion.

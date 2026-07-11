# MundoZ AntiXray Domain Services

## Visibility Evaluation Service

Responsibility:

Evaluate whether a protected block should be visible.

Input:

- Block Identity
- Exposure Context

Output:

- Visibility Decision

---

## Replacement Selection Service

Responsibility:

Choose a safe replacement representation.

Input:

- Block Identity
- Dimension rules

Output:

- Replacement Block

---

## Protection Policy

Responsibility:

Define which blocks require AntiXray evaluation.

Input:

- Block Identity

Output:

- Protected or not

---

# Domain Dependency Rules

Domain services may depend only on domain concepts.

Allowed:

Visibility Evaluation Service
    |
    v
Visibility Rule
    |
    v
Visibility Decision


Not allowed:

Visibility Evaluation Service
    |
    v
Minecraft BlockState

Replacement Selection Service
    |
    v
LevelChunk

---

# State Modification Rule

Domain services never modify world state.

They only produce decisions.

Examples:

Input:
- Block Identity
- Exposure Context

Output:
- Visibility Decision
- Replacement Block

The application layer decides how these results are applied.

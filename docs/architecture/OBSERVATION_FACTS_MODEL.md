# MundoZ Observation Facts Model

## Purpose

Observation facts represent calculated information
about the relationship between an observer and a target.

Facts are produced by infrastructure analysis
and consumed by domain rules.

---

# Examples

## Distance Fact

Represents spatial distance between observer and target.

---

## Line Of Sight Fact

Represents whether the observer has a valid visual path.

Possible states:

- CLEAR
- BLOCKED

---

## Occlusion Fact

Represents whether terrain blocks observation.

---

# Responsibility

Facts describe reality.

They do not decide representation.

The domain uses facts to produce ObservationDecision.

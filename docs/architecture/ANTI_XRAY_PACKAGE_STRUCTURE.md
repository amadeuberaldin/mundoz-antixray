# MundoZ AntiXray Package Structure

The package structure follows domain-driven design principles.

The main goal is to keep AntiXray business rules independent from
Minecraft implementation details.

├── domain
│   ├── protection
│   ├── visibility
│   └── replacement
│
├── application
│   ├── obfuscation
│   └── reveal
│
├── infrastructure
│   └── minecraft
│       ├── mixin
│       ├── adapter
│       └── context
│
└── bootstrap

---

# Dependency Rules

The dependency direction must always point inward.

Allowed:

infrastructure → application → domain

bootstrap → infrastructure/application

Not allowed:

domain → Minecraft
domain → Fabric API
domain → packets
domain → mixins

---

# Package Responsibilities

## domain

Contains AntiXray business rules.

Examples:

- protected block rules;
- visibility decisions;
- replacement policies;
- exposure rules.

The domain must not know Minecraft exists.

---

## application

Contains use cases.

Examples:

- obfuscate chunk representation;
- reveal exposed blocks.

The application coordinates domain decisions.

---

## infrastructure.minecraft

Contains Minecraft integration.

Examples:

- mixins;
- packet handling;
- adapters;
- Minecraft context.

This layer translates Minecraft objects into application/domain concepts.

---

## bootstrap

Responsible for starting and connecting components.

Examples:

- dependency wiring;
- service registration;
- Fabric initialization.

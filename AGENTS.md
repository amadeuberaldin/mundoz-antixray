## Autonomous Agent Branches

The agent may work autonomously only on a dedicated branch whose name
starts with:

- codex/

Examples:

- codex/replacement-slice
- codex/reveal-migration
- codex/chunk-integration

The agent must never perform autonomous work directly on:

- main
- v2-domain-architecture
- release branches
- production branches

On a dedicated `codex/*` branch, the agent may:

- inspect the repository;
- modify files within the approved mission scope;
- create and update tests;
- run validation commands;
- create multiple small commits;
- push commits to the corresponding remote `codex/*` branch.

Each commit must still follow:

One concept -> one commit -> one responsibility.

Before every commit, the agent must:

1. run `git diff --check`;
2. run relevant focused tests;
3. run `./gradlew test`;
4. run `./gradlew clean build`;
5. confirm that all changed files belong to the approved mission.

The agent must not:

- merge into another branch;
- rebase the target branch;
- force-push;
- delete remote branches;
- modify Git history;
- create releases or tags;
- change the approved mission scope silently.

## Autonomous Mission Completion

When assigned an autonomous mission, the agent should continue working
without requesting approval for every small implementation detail.

It must stop when:

- the approved mission is complete;
- tests or build cannot be fixed within the approved scope;
- documentation and implementation conflict;
- a gameplay or architecture decision is required;
- packet, palette, or runtime behavior would change;
- continuing would require expanding the approved scope.

At completion, the agent must provide:

- the branch name;
- the commits created;
- the exact files changed;
- tests and builds executed;
- known limitations;
- unresolved questions;
- recommended review order.

The agent must push only to its dedicated `codex/*` branch and must not
merge the branch.

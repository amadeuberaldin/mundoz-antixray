# MundoZ AntiXray Protected Blocks

This document defines which blocks are protected by AntiXray
and why they belong to the protection domain.

The list is based on the v1 implementation and refined
according to production experience.

---

# Protected Categories

## Resources

These blocks provide unfair advantages when revealed through xray.

Protected:

- COAL_ORE
- DEEPSLATE_COAL_ORE
- COPPER_ORE
- DEEPSLATE_COPPER_ORE
- IRON_ORE
- DEEPSLATE_IRON_ORE
- GOLD_ORE
- DEEPSLATE_GOLD_ORE
- REDSTONE_ORE
- DEEPSLATE_REDSTONE_ORE
- EMERALD_ORE
- DEEPSLATE_EMERALD_ORE
- LAPIS_ORE
- DEEPSLATE_LAPIS_ORE
- DIAMOND_ORE
- DEEPSLATE_DIAMOND_ORE
- NETHER_GOLD_ORE
- NETHER_QUARTZ_ORE
- ANCIENT_DEBRIS

Reason:

These resources affect progression and economy.

---

# Underground Visibility

## Lava

Protected:

- LAVA

Reason:

Lava exposure can reveal underground spaces and reduce exploration risk.

---

# Excluded Blocks

The following blocks were removed from protection:

## Geode blocks

- AMETHYST_BLOCK
- BUDDING_AMETHYST
- AMETHYST_CLUSTER
- LARGE_AMETHYST_BUD
- MEDIUM_AMETHYST_BUD
- SMALL_AMETHYST_BUD
- CALCITE
- SMOOTH_BASALT

Reason:

They are part of natural exploration and should remain visually correct.

---

## Structure blocks

- TUFF_SLAB

Reason:

Structure-related blocks can also be used in player construction.

Hiding them damages normal gameplay.

---

# Migration Notes

The following blocks existed in v1 but are intentionally removed
from v2 protection:

- AMETHYST_BLOCK
- BUDDING_AMETHYST
- AMETHYST_CLUSTER
- LARGE_AMETHYST_BUD
- MEDIUM_AMETHYST_BUD
- SMALL_AMETHYST_BUD
- CALCITE
- SMOOTH_BASALT
- TUFF_SLAB

This is an intentional behavior change based on production experience.

The v2 architecture preserves lessons learned from v1,
not every v1 implementation detail.

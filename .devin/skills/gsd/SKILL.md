---
name: gsd
description: "Gateway para as instrucoes e skills do GSD Core instaladas no repositorio"
argument-hint: "[help | new-project | discuss-phase | plan-phase | execute-phase | phase | surface | update] [args]"
triggers: ["user", "model"]
---

<objective>
Atue como gateway para as instrucoes e skills do GSD Core (`open-gsd/gsd-core`) que foram importadas para `.github/skills/` e `.github/gsd-core/workflows/`.

Quando o usuario invocar `/gsd`, `/gsd <subcomando>` ou qualquer comando do tipo `/gsd-*`, identifique o subcomando e execute o fluxo correspondente usando as ferramentas nativas do Devin.
</objective>

<routing>
- Nenhum argumento ou `help` -> `.github/skills/gsd-help/SKILL.md` + `.github/gsd-core/workflows/help.md`
- `new-project` -> `.github/skills/gsd-new-project/SKILL.md` + `.github/gsd-core/workflows/new-project.md`
- `discuss-phase` -> `.github/skills/gsd-discuss-phase/SKILL.md` + `.github/gsd-core/workflows/discuss-phase.md`
- `plan-phase` -> `.github/skills/gsd-plan-phase/SKILL.md` + `.github/gsd-core/workflows/plan-phase.md`
- `execute-phase` -> `.github/skills/gsd-execute-phase/SKILL.md` + `.github/gsd-core/workflows/execute-phase.md`
- `phase` -> `.github/skills/gsd-phase/SKILL.md` + workflows `add-phase.md`, `insert-phase.md`, `remove-phase.md`, `edit-phase.md`
- `surface` -> `.github/skills/gsd-surface/SKILL.md`
- `update` -> `.github/skills/gsd-update/SKILL.md` + `.github/gsd-core/workflows/update.md`
</routing>

<process>
1. Leia o SKILL.md do subcomando em `.github/skills/gsd-<sub>/`.
2. Leia o(s) workflow(s) referenciado(s) em `.github/gsd-core/workflows/`.
3. Siga as instrucoes de alto nivel adaptando qualquer comando especifico de outro runtime (Claude Code, Copilot, Node, npm, shell scripts da biblioteca `gsd-core`, etc.) para as ferramentas Devin disponiveis (`read`, `exec`, `grep`, `git_*`, `message_user`, etc.).
4. Nunca execute scripts `.cjs`/Node da biblioteca GSD como se fossem comandos nativos do Devin; interprete-os apenas como descricao do procedimento e reproduza o efeito com ferramentas Devin.
5. Ao final, resuma o resultado e pergunte qual o proximo passo.
</process>

<files>
- Instrucoes gerais: `AGENTS.md`
- Skills: `.github/skills/gsd-*/SKILL.md`
- Workflows: `.github/gsd-core/workflows/*.md`
</files>

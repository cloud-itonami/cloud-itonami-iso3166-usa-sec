# cloud-itonami-iso3166-usa-sec

Open ISO 3166 **agency-level** Blueprint for **USA-SEC**: Securities and Exchange Commission
(parent country: **USA**).

This leaf designs a forkable OSS business for an independent operator
navigating **Securities and Exchange Commission**-specific public-procurement / regulatory compliance,
composing with the country coordinator `cloud-itonami-iso3166-usa`.

## What this is NOT

- **Not Securities and Exchange Commission.** Commercial compliance navigation only.
- **Not legal advice.** Cite official sources; route licensed work to counsel.

## The spec-basis: `src/statute/facts.cljk`

A catalog of **21 verified regulatory anchors** across CFR titles **2, 17 and 48**,
carrying **9 byte-exact quoted spans**, **8 exact occurrence counts** and
**4 checked absences**. Every heading is the byte-exact `label_description`
returned by the eCFR versioner API; every quote is a byte-exact span of live
section text. `tools/verify_citations.cljk` re-fetches all of it and fails if
any of it drifts.

### What the catalog found

Three findings, each recorded as data — a quote, a counted occurrence, or a
checked negative — rather than as prose, so that a reorganisation of the CFR
trips the gate instead of leaving a stale claim looking verified.

| # | Finding | How it is checked |
|---|---|---|
| 1 | **SEC-filer status is an exemption, not an eligibility gate.** Both 48 CFR 52.204-10 (contracts) and 2 CFR 170.105 (assistance) impose executive-compensation reporting *only where* `The public does not have access to information about the compensation ... through periodic reports filed under section 13(a) or 15(d) of the Securities Exchange Act of 1934`. A registrant already filing is **relieved** of the duty. | one quoted span from each rulebook |
| 2 | **In the operative clauses the agency's name is never spelled correctly.** Inside 48 CFR 52.204-10 the string `Securities and Exchange Commission` occurs **0** times and `Security and Exchange Commission` occurs **2**. 2 CFR part 170 is identical: **0** correct, **2** misspelled. Both misspellings sit exactly where the regulation tells the reader to go look. | four occurrence counts, plus two text absences with the misspelling as control |
| 3 | **17 CFR is not the SEC's title, and its one procurement-shaped hit is a false friend.** The title is `Commodity and Securities Exchanges`; chapter I is the CFTC and chapter IV is the Treasury. No label in the title contains `Federal Acquisition`, `procurement` or `contractor`. Exactly one matches `award eligibility` — 17 CFR 240.21F-2 — and it is about whistleblower bounties, where `A company or other entity is not eligible to be a whistleblower.` | a label absence with a 355-hit control, plus the false-friend entry quoting *both* the phrase that looks like a match and the sentence that disarms it |

Finding 2 is the one worth carrying to other leaves: it is the failure mode of
every **name-keyed** compliance scan run over these parts. A tool searching the
federal award rulebooks for this agency by its own name finds nothing in the
clause that decides its user's reporting obligation.

### What an operator can actually sell

Not a disclosure audit. The question is **which side of the exemption a client
is on**, and whether anyone downstream inherits the answer — 2 CFR part 170
re-tests the condition at the subrecipient tier, so an exemption does not
travel down a funding chain. 17 CFR 240.15d-1 sharpens it further: 15(d)
reporting can be suspended, so "is this an SEC reporting company" is not a
lookup against a static list.

## A claim this leaf deliberately does **not** make

Both rulebooks send the reader to `http://www.sec.gov/answers/execomp.htm`.
From this workstation that address returns **HTTP 403**, with and without a
browser User-Agent. That is **not** recorded as a dead link. SEC.gov asks
automated clients to declare themselves, so a 403 is equally consistent with
the page being fine and this client being refused, and distinguishing the two
would mean evading a bot control. The honest state is *could-not-answer*, so
the catalog carries the citation and says so, and asserts nothing about whether
the page exists.

## Where this contradicts `blueprint.edn`

`blueprint.edn` names this leaf an *Independent SEC Financial-Disclosure
Compliance Service*, and this README previously described the product as
`public-company disclosure posture checks when contracting with federal
buyers`. Finding 1 refutes the direction of that sentence: the federal
rulebooks make an SEC filer report **less**, not more, and neither rulebook
conditions eligibility on SEC status at all (eligibility runs on the unique
entity identifier — 2 CFR part 25).

The blueprint's `:itonami.blueprint/name` is **left unchanged**, because fleet
consumers key on it and renaming it is an owner decision, not a side effect of
a citation round. The contradiction is stated here rather than smoothed over.

## Verifying

```bash
# live gate -- re-fetches eCFR. exit 0 verified / 1 drifted / 2 could-not-answer
nbb tools/verify_citations.cljk

# the red side of the same gate: breaks one property at a time in a throwaway
# copy and requires the gate to fail for that stated reason
nbb tools/mutation_check.cljk            # 16 cases (network)
nbb tools/mutation_check.cljk --offline  #  5 cases (no network)

# offline shape invariants -- what the live gate depends on being present
clojure -M:test
clojure -M:lint
```

The gate distinguishes **could-not-answer (exit 2)** from **wrong (exit 1)**.
A control that stops matching, an undeclared endpoint, an empty document, or a
run that checked fewer items than its floors is exit 2 — never a pass.

## Official surface

- https://www.sec.gov/
- eCFR structure API: https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-17.json
- eCFR full-text API: https://www.ecfr.gov/api/versioner/v1/full/2026-08-18/title-17.xml

## Capability layer

Resolves via `kotoba-lang/iso3166` (`USA-SEC`, parent `USA`).

## License

AGPL-3.0-or-later.

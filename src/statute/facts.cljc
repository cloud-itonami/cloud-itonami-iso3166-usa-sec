(ns statute.facts
  "Agency-level compliance catalog for **USA-SEC** (United States Securities
  and Exchange Commission) -- the spec-basis behind this leaf's blueprint
  claim that an independent operator can run an SEC financial-disclosure
  compliance service for parties dealing with federal buyers.

  Scope. This is the SEC-specific layer only. Government-wide U.S. federal
  statutes live in the country coordinator `cloud-itonami-iso3166-usa`'s
  `statute.facts` and are NOT duplicated here; the two catalogs compose, keyed
  `USA-SEC` -> `USA`. Sibling agency leaves (`USA-TREASURY`, `USA-DOT`,
  `USA-FCC`, `USA-DOC`) hold their own chapters. Two government-wide bodies of
  rule ARE carried here anyway -- the FAR (48 CFR chapter 1) and OMB's
  transparency-reporting rule (2 CFR part 170) -- because every finding below
  is a statement about how the SEC relates to them, and a contrast needs both
  sides present to be checked.

  Provenance. Every entry cites the official eCFR (Electronic Code of Federal
  Regulations, GPO/Office of the Federal Register) address for the smallest
  stable unit that was independently confirmed. Nothing here is fabricated:
  each `:statute/verified-label` is the byte-exact `label_description`
  returned by the eCFR versioner structure API on `:statute/verified-at`, and
  each string in `:statute/verified-quotes` is a byte-exact span of the
  section text returned by the eCFR versioner full-text API.
  `tools/verify_citations.cljs` re-fetches both and fails if either drifts.

  Why the citation and the verification URL differ. `:statute/url` is the
  canonical human address a person should open. It is deliberately NOT the URL
  that was machine-verified: fetching www.ecfr.gov from an automated client can
  return HTTP 200 with a `Federal Register :: Request Access` interstitial
  rather than the regulation, so a status-code check against it would report
  success while proving nothing. We verify through the documented machine API
  and record both. The human URLs here were constructed from the same verified
  node paths rather than fetched -- do not `curl` one and treat a 200 as
  confirmation, because it is not.

  THE TRAP THIS CATALOG EXISTS TO PIN DOWN. **In the operative federal text on
  both sides of federal money, the Securities and Exchange Commission's name is
  never spelled correctly -- and the role that text gives the SEC is the
  opposite of the one this leaf's blueprint sells.** This leaf's own README
  describes the product as `public-company disclosure posture checks when
  contracting with federal buyers`, as though being an SEC filer were a
  compliance burden to be audited before a federal buyer will deal with you.
  Three independent findings below refute that, and each is recorded as data
  -- a byte-exact quote, a counted occurrence, or a checked negative -- rather
  than as prose, so that a reorganisation of the CFR cannot leave a stale
  claim sitting here looking verified:

  1. **SEC-filer status is an EXEMPTION from a federal reporting duty, not a
     gate on eligibility.** Both rulebooks impose executive-compensation
     reporting only where `The public does not have access to information about
     the compensation of the executives through periodic reports filed under
     section 13(a) or 15(d) of the Securities Exchange Act of 1934`. That is a
     precondition for the obligation to attach. A registrant already filing
     under 13(a) or 15(d) is relieved of it. The direction runs the other way
     from the blueprint's: SEC reporting companies owe the federal government
     LESS disclosure, not more, and the operator's real question is whether the
     client is inside or outside that exemption.

  2. **Searching either rulebook for the agency's name finds nothing in the
     operative clause.** Within 48 CFR 52.204-10 -- the FAR clause that decides
     the duty -- the string `Securities and Exchange Commission` occurs ZERO
     times, while the misspelling `Security and Exchange Commission` occurs
     twice. 2 CFR part 170 is identical: zero correct, two misspelled. Both
     misspellings sit at the one place the regulation tells the reader to go
     look. This is not a curiosity; it is the failure mode of every
     name-keyed compliance scan run over these parts, and it is counted rather
     than described so that a correction upstream trips the gate instead of
     silently making this paragraph false.

  3. **17 CFR is not the SEC's title, and its one procurement-shaped hit is a
     false friend.** Title 17 is `Commodity and Securities Exchanges`; the SEC
     holds chapter II, while chapter I is the Commodity Futures Trading
     Commission and chapter IV is the Department of the Treasury. Across all
     4,117 nodes of the title, no label contains `Federal Acquisition`,
     `procurement`, or `contractor`. Exactly one label matches `award
     eligibility` -- 17 CFR 240.21F-2 -- and it is about whistleblower
     bounties, where `A company or other entity is not eligible to be a
     whistleblower.` An operator keyword-searching the SEC title for award
     eligibility gets one hit that expressly excludes them.

  What the operator can actually sell. The same question the DOT and Treasury
  leaves arrived at, in SEC clothing: **which side of the exemption is this
  client on**, and does anyone downstream of them inherit the answer. 2 CFR
  part 170 pushes the same test onto first-tier subrecipients, so a filer's
  exemption does not travel to the entities it funds. That distinction, not a
  disclosure audit, is the product.

  A NON-ASSERTION, recorded deliberately. Both rulebooks send the reader to
  `http://www.sec.gov/answers/execomp.htm` to determine whether the public has
  access. From this workstation that address returns HTTP 403, with and
  without a browser User-Agent. **That is NOT recorded here as a dead link.**
  SEC.gov asks automated clients to declare themselves, so a 403 is equally
  consistent with the page being fine and this client being refused, and
  distinguishing the two would mean evading a bot control. The honest state is
  could-not-answer, so the catalog carries the citation and this paragraph and
  makes no claim about the page's existence. Recording a 403 as `dead` would be
  exactly the unverified-claim failure this leaf exists to correct.")

;; ---------------------------------------------------------------------------
;; Verification endpoints.
;;
;; Pinned to a dated snapshot rather than `current` so that a run is
;; reproducible: `current` would silently change the thing being compared
;; against, which is the failure mode where a gate keeps passing because both
;; sides moved together.

(def ecfr-structure-api
  "CFR title -> eCFR versioner *structure* endpoint. Yields the node tree whose
  `label_description` fields the positive half of the gate compares against."
  {2  "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-2.json"
   17 "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-17.json"
   48 "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-48.json"})

(def ecfr-full-text-api
  "CFR title -> eCFR versioner *full-text* endpoint. A quote check appends
  `?part=<part>&section=<section>`; a text-absence or count check appends
  `?part=<part>` alone when no section is named. Declared per title rather than
  built from a prefix so that an entry citing a title nobody declared an
  endpoint for is a could-not-answer instead of a fetch against a URL nobody
  checked."
  {2  "https://www.ecfr.gov/api/versioner/v1/full/2026-08-18/title-2.xml"
   17 "https://www.ecfr.gov/api/versioner/v1/full/2026-08-18/title-17.xml"
   48 "https://www.ecfr.gov/api/versioner/v1/full/2026-08-18/title-48.xml"})

;; ---------------------------------------------------------------------------
;; The catalog.
;;
;; `USA-SEC` is an agency-level key (parent `USA`), matching blueprint.edn's
;; `:itonami.blueprint/iso3166`.
;;
;; `:statute/cfr-node` is the path from the CFR title down to the cited node,
;; as [type identifier] pairs. The live gate walks the eCFR structure tree by
;; this path -- it does not string-match the URL, because hierarchical
;; identifiers nest as substrings of one another (section `240.13a-1` is a
;; prefix of nothing here, but part `24` is a prefix of part `240`, and part
;; `20` of part `200`). Walking explicit [type identifier] steps cannot pass by
;; accident. An empty path denotes the title root itself.
;;
;; A step may be `"*"`, used ONLY where eCFR generates the identifier rather
;; than the CFR citing it -- the `subject_group` nodes inside 17 CFR part 240
;; have identifiers like `ECFR821bdfb7bc8017f`, which are not citable addresses
;; and are not stable enough to record. A wildcard that resolves to more than
;; one node is reported as a failure, not silently taken.
;;
;; `:statute/hat` says which role the entry belongs to. Conflating these is the
;; failure this catalog exists to prevent, so it is a required field:
;;   :securities-regulator -- the SEC writing rules for issuers, its own job
;;   :exemption-hinge      -- a rule the federal reporting exemption turns on,
;;                            whichever rulebook it sits in
;;   :contract-side        -- the FAR: buying from the operator
;;   :assistance-side      -- 2 CFR: awarding money to the operator
;;   :title-cotenant       -- a chapter of 17 CFR that is NOT the SEC, carried
;;                            because `17 CFR` is routinely read as `SEC rules`
;;   :false-friend         -- a node a procurement-shaped keyword search hits,
;;                            whose subject is something else entirely

(def catalog
  "USA-SEC -> ordered vector of verified regulatory anchors."
  {"USA-SEC"
   [
    ;; -----------------------------------------------------------------------
    ;; Who actually lives in title 17.

    {:statute/id            :title-17/root
     :statute/topic         #{:jurisdiction}
     :statute/hat           :title-cotenant
     :statute/title         "17 CFR -- Commodity and Securities Exchanges"
     :statute/cfr-title     17
     :statute/cfr-node      []
     :statute/url           "https://www.ecfr.gov/current/title-17"
     :statute/verified-label "Commodity and Securities Exchanges"
     :statute/verified-via  "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-17.json"
     :statute/verified-at   "2026-08-20"
     :statute/note
     "The title is named for two markets, not for one agency. Everything below
      that treats `17 CFR` as a synonym for `SEC rules` is answering about a
      container that the SEC shares."}

    {:statute/id            :sec/chapter
     :statute/topic         #{:jurisdiction}
     :statute/hat           :securities-regulator
     :statute/title         "17 CFR Chapter II -- Securities and Exchange Commission"
     :statute/cfr-title     17
     :statute/cfr-node      [["chapter" "II"]]
     :statute/url           "https://www.ecfr.gov/current/title-17/chapter-II"
     :statute/verified-label "Securities and Exchange Commission"
     :statute/verified-via  "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-17.json"
     :statute/verified-at   "2026-08-20"
     :statute/note
     "The actual SEC chapter, and the only one of the title's three. Note the
      spelling: `Securities`. The federal award rulebooks that point at this
      agency do not manage it -- see the occurrence counts below."}

    {:statute/id            :cftc/chapter-in-17
     :statute/topic         #{:jurisdiction}
     :statute/hat           :title-cotenant
     :statute/title         "17 CFR Chapter I -- Commodity Futures Trading Commission"
     :statute/cfr-title     17
     :statute/cfr-node      [["chapter" "I"]]
     :statute/url           "https://www.ecfr.gov/current/title-17/chapter-I"
     :statute/verified-label "Commodity Futures Trading Commission"
     :statute/verified-via  "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-17.json"
     :statute/verified-at   "2026-08-20"
     :statute/note
     "A different agency, with its own statute and its own registrants, holding
      the FIRST chapter of the title. An operator who scoped a compliance
      product to `17 CFR` has scoped it across an agency boundary."}

    {:statute/id            :treasury/chapter-in-17
     :statute/topic         #{:jurisdiction}
     :statute/hat           :title-cotenant
     :statute/title         "17 CFR Chapter IV -- Department of the Treasury"
     :statute/cfr-title     17
     :statute/cfr-node      [["chapter" "IV"]]
     :statute/url           "https://www.ecfr.gov/current/title-17/chapter-IV"
     :statute/verified-label "Department of the Treasury"
     :statute/verified-via  "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-17.json"
     :statute/verified-at   "2026-08-20"
     :statute/note
     "The third co-tenant, and the reason this leaf and the USA-TREASURY leaf
      can both be right about the same title. Treasury's government securities
      rules sit here, two chapters away from the SEC's."}

    ;; -----------------------------------------------------------------------
    ;; The SEC doing its own job.

    {:statute/id            :sec/organization
     :statute/topic         #{:agency-organisation}
     :statute/hat           :securities-regulator
     :statute/title         "17 CFR Part 200 -- Organization; Conduct and Ethics; and Information and Requests"
     :statute/cfr-title     17
     :statute/cfr-node      [["chapter" "II"] ["part" "200"]]
     :statute/url           "https://www.ecfr.gov/current/title-17/chapter-II/part-200"
     :statute/verified-label "Organization; Conduct and Ethics; and\nInformation and Requests"
     :statute/verified-via  "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-17.json"
     :statute/verified-at   "2026-08-20"
     :statute/note
     "Recorded with the literal newline eCFR returns inside the label. It is
      kept rather than tidied because the gate compares bytes: a catalog that
      prettifies what the API said would drift from the API on the first run
      and blame the government for it."}

    {:statute/id            :sec/exchange-act-rules
     :statute/topic         #{:disclosure}
     :statute/hat           :securities-regulator
     :statute/title         "17 CFR Part 240 -- General Rules and Regulations, Securities Exchange Act of 1934"
     :statute/cfr-title     17
     :statute/cfr-node      [["chapter" "II"] ["part" "240"]]
     :statute/url           "https://www.ecfr.gov/current/title-17/chapter-II/part-240"
     :statute/verified-label "General Rules and Regulations, Securities Exchange Act of 1934"
     :statute/verified-via  "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-17.json"
     :statute/verified-at   "2026-08-20"
     :statute/note
     "The part containing the rules the federal exemption keys on. The
      exemption names the STATUTE (sections 13(a) and 15(d)); the duty to file
      is implemented HERE, which is why a catalog that stopped at the statute
      could not tell an operator what their client actually files."}

    {:statute/id            :sec/annual-report-13a
     :statute/topic         #{:disclosure :periodic-report}
     :statute/hat           :exemption-hinge
     :statute/title         "17 CFR 240.13a-1 -- Requirements of annual reports"
     :statute/cfr-title     17
     :statute/cfr-node      [["chapter" "II"] ["part" "240"] ["subject_group" "*"] ["section" "240.13a-1"]]
     :statute/url           "https://www.ecfr.gov/current/title-17/chapter-II/part-240/section-240.13a-1"
     :statute/verified-label "Requirements of annual reports."
     :statute/verified-via  "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-17.json"
     :statute/verified-at   "2026-08-20"
     :statute/quote-part    "240"
     :statute/quote-section "240.13a-1"
     :statute/verified-quotes
     ["Every issuer having securities registered pursuant to section 12 of the Act"]
     :statute/note
     "The 13(a) half of the exemption, from the issuer's side: registration
      under section 12 is what pulls an issuer into annual reporting. Label is
      `Requirements` -- plural -- where its 15(d) counterpart is singular. The
      two are recorded byte-exactly rather than normalised, because a catalog
      that tidies them cannot detect the day one of them changes."}

    {:statute/id            :sec/annual-report-15d
     :statute/topic         #{:disclosure :periodic-report}
     :statute/hat           :exemption-hinge
     :statute/title         "17 CFR 240.15d-1 -- Requirement of annual reports"
     :statute/cfr-title     17
     :statute/cfr-node      [["chapter" "II"] ["part" "240"] ["subject_group" "*"] ["section" "240.15d-1"]]
     :statute/url           "https://www.ecfr.gov/current/title-17/chapter-II/part-240/section-240.15d-1"
     :statute/verified-label "Requirement of annual reports."
     :statute/verified-via  "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-17.json"
     :statute/verified-at   "2026-08-20"
     :statute/quote-part    "240"
     :statute/quote-section "240.15d-1"
     :statute/verified-quotes
     ["unless the registrant is exempt from such filing by section 15(d) of the Act or rules thereunder"]
     :statute/note
     "The 15(d) half, and the sharper one for this leaf: 15(d) reporting can
      itself be suspended. So `is the client an SEC reporting company` is not a
      yes/no drawn from a registration list -- an entity can have been one and
      have stopped, which flips the federal exemption back off. The quoted span
      is the carve-out that makes it possible."}

    {:statute/id            :sec/regulation-s-k
     :statute/topic         #{:disclosure}
     :statute/hat           :securities-regulator
     :statute/title         "17 CFR Part 229 -- Regulation S-K"
     :statute/cfr-title     17
     :statute/cfr-node      [["chapter" "II"] ["part" "229"]]
     :statute/url           "https://www.ecfr.gov/current/title-17/chapter-II/part-229"
     :statute/verified-label "Standard Instructions for Filing Forms Under Securities Act of 1933, Securities Exchange Act of 1934 and Energy Policy and Conservation Act of 1975—Regulation S-K"
     :statute/verified-via  "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-17.json"
     :statute/verified-at   "2026-08-20"
     :statute/note
     "Recorded with the em dash eCFR returns, not a hyphen. The part everyone
      calls `Regulation S-K` is titled as an instruction set spanning three
      statutes, which is why a search for the popular name alone finds it only
      by luck."}

    {:statute/id            :sec/executive-compensation
     :statute/topic         #{:disclosure :executive-compensation}
     :statute/hat           :exemption-hinge
     :statute/title         "17 CFR 229.402 -- (Item 402) Executive compensation"
     :statute/cfr-title     17
     :statute/cfr-node      [["chapter" "II"] ["part" "229"] ["subpart" "229.400"] ["section" "229.402"]]
     :statute/url           "https://www.ecfr.gov/current/title-17/chapter-II/part-229/subpart-229.400/section-229.402"
     :statute/verified-label "(Item 402) Executive compensation."
     :statute/verified-via  "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-17.json"
     :statute/verified-at   "2026-08-20"
     :statute/quote-part    "229"
     :statute/quote-section "229.402"
     :statute/verified-quotes
     ["A foreign private issuer will be deemed to comply with this Item if it provides the information required by Items 6.B, 6.E.2, and 6.F of Form 20-F"]
     :statute/note
     "The rule that makes executive compensation public, and therefore the rule
      the federal exemption silently depends on: the FAR asks whether the
      public HAS ACCESS to the information, and this is the Item that puts it
      there. The quoted span is the foreign-private-issuer accommodation, which
      is the case where the answer is least obvious -- such an issuer complies
      via Form 20-F, whose compensation disclosure is thinner. Whether that
      satisfies the FAR's `public does not have access` test is a real
      question this catalog raises and does not answer."}

    {:statute/id            :sec/forms-exchange-act
     :statute/topic         #{:disclosure :forms}
     :statute/hat           :securities-regulator
     :statute/title         "17 CFR Part 249 -- Forms, Securities Exchange Act of 1934"
     :statute/cfr-title     17
     :statute/cfr-node      [["chapter" "II"] ["part" "249"]]
     :statute/url           "https://www.ecfr.gov/current/title-17/chapter-II/part-249"
     :statute/verified-label "Forms, Securities Exchange Act of 1934"
     :statute/verified-via  "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-17.json"
     :statute/verified-at   "2026-08-20"
     :statute/note
     "Where Form 10-K and Form 20-F are prescribed. The FAR's records clause
      names `Securities and Exchange Commission 10-K annual report` -- one of
      the three places in FAR part 52 that spells the agency correctly, and it
      is a records-retention list, not the reporting test."}

    ;; -----------------------------------------------------------------------
    ;; The false friend.

    {:statute/id            :sec/whistleblower-general
     :statute/topic         #{:whistleblower}
     :statute/hat           :false-friend
     :statute/title         "17 CFR 240.21F-1 -- General"
     :statute/cfr-title     17
     :statute/cfr-node      [["chapter" "II"] ["part" "240"] ["subject_group" "*"] ["section" "240.21F-1"]]
     :statute/url           "https://www.ecfr.gov/current/title-17/chapter-II/part-240/section-240.21F-1"
     :statute/verified-label "General."
     :statute/verified-via  "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-17.json"
     :statute/verified-at   "2026-08-20"
     :statute/note
     "Carried only to fix the address of the whistleblower series, so the
      false-friend entry below is anchored to a neighbourhood rather than to a
      lone section that could be renumbered out from under it."}

    {:statute/id            :sec/whistleblower-award-eligibility
     :statute/topic         #{:whistleblower}
     :statute/hat           :false-friend
     :statute/title         "17 CFR 240.21F-2 -- Whistleblower status, award eligibility, confidentiality, and retaliation protections"
     :statute/cfr-title     17
     :statute/cfr-node      [["chapter" "II"] ["part" "240"] ["subject_group" "*"] ["section" "240.21F-2"]]
     :statute/url           "https://www.ecfr.gov/current/title-17/chapter-II/part-240/section-240.21F-2"
     :statute/verified-label "Whistleblower status, award eligibility, confidentiality, and retaliation protections."
     :statute/verified-via  "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-17.json"
     :statute/verified-at   "2026-08-20"
     :statute/quote-part    "240"
     :statute/quote-section "240.21F-2"
     :statute/verified-quotes
     ["A whistleblower must be an individual. A company or other entity is not eligible to be a whistleblower."
      "To be eligible for an award under Section 21F(b) of the Exchange Act"]
     :statute/note
     "THE false friend. This is the ONLY label in all 4,117 nodes of title 17
      matching `award eligibility`, so a keyword search for award eligibility
      inside the SEC's title returns exactly one hit and it is about bounties
      for reporting securities violations. The second quoted span is the phrase
      that would be skimmed as a match; the first is the sentence that makes it
      inapplicable to the operator, who is a company. Two spans rather than one
      on purpose: an entry that quoted only the eligibility sentence would look
      like corroboration of the very error it exists to refute."}

    ;; -----------------------------------------------------------------------
    ;; Contract side: buying from the operator.

    {:statute/id            :far/solicitation-clauses
     :statute/topic         #{:procurement}
     :statute/hat           :contract-side
     :statute/title         "48 CFR Part 52 -- Solicitation Provisions and Contract Clauses"
     :statute/cfr-title     48
     :statute/cfr-node      [["chapter" "1"] ["subchapter" "H"] ["part" "52"]]
     :statute/url           "https://www.ecfr.gov/current/title-48/chapter-1/subchapter-H/part-52"
     :statute/verified-label "Solicitation Provisions and Contract Clauses"
     :statute/verified-via  "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-48.json"
     :statute/verified-at   "2026-08-20"
     :statute/note
     "The document the occurrence counts below are taken over. Recorded as an
      entry, not just as a fetch target, so that the part being counted is
      itself confirmed to exist and be named what this catalog thinks."}

    {:statute/id            :far/exec-comp-reporting
     :statute/topic         #{:procurement :executive-compensation}
     :statute/hat           :contract-side
     :statute/title         "48 CFR 52.204-10 -- Reporting Executive Compensation and First-Tier Subcontract Awards"
     :statute/cfr-title     48
     :statute/cfr-node      [["chapter" "1"] ["subchapter" "H"] ["part" "52"] ["subpart" "52.2"] ["section" "52.204-10"]]
     :statute/url           "https://www.ecfr.gov/current/title-48/chapter-1/subchapter-H/part-52/subpart-52.2/section-52.204-10"
     :statute/verified-label "Reporting Executive Compensation and First-Tier Subcontract Awards."
     :statute/verified-via  "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-48.json"
     :statute/verified-at   "2026-08-20"
     :statute/quote-part    "52"
     :statute/quote-section "52.204-10"
     :statute/verified-quotes
     ["The public does not have access to information about the compensation of the executives through periodic reports filed under section 13(a) or 15(d) of the Securities Exchange Act of 1934"
      "see the U.S. Security and Exchange Commission total compensation filings at"]
     :statute/note
     "Finding 1 and finding 2 in one section. The first span is the exemption
      condition: the duty attaches only where the public CANNOT already see the
      compensation, so an SEC reporting company is relieved of it. The second
      span is the pointer the clause gives for checking that -- quoted with the
      misspelling intact, because that is what the regulation says. Anyone who
      normalises it while copying has produced a document that no longer
      matches the CFR, and a search of the real CFR for their corrected string
      finds nothing."}

    {:statute/id            :far/taxpayer-identification
     :statute/topic         #{:procurement :identifier}
     :statute/hat           :contract-side
     :statute/title         "48 CFR Subpart 4.9 -- Taxpayer Identification Number Information"
     :statute/cfr-title     48
     :statute/cfr-node      [["chapter" "1"] ["subchapter" "A"] ["part" "4"] ["subpart" "4.9"]]
     :statute/url           "https://www.ecfr.gov/current/title-48/chapter-1/subchapter-A/part-4/subpart-4.9"
     :statute/verified-label "Taxpayer Identification Number Information"
     :statute/verified-via  "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-48.json"
     :statute/verified-at   "2026-08-20"
     :statute/note
     "The contrast that keeps finding 1 honest. The FAR does collect an
      identifier and does devote a subpart to it -- it is simply the taxpayer
      identifier, administered by a different agency in a different title. The
      SEC does not issue an identifier that federal buyers collect at all,
      which is the concrete sense in which SEC status is not an eligibility
      credential. Shared with the USA-TREASURY leaf, which reaches the same
      subpart from the tax side."}

    {:statute/id            :far/taxpayer-id-provision
     :statute/topic         #{:procurement :identifier}
     :statute/hat           :contract-side
     :statute/title         "48 CFR 52.204-3 -- Taxpayer identification"
     :statute/cfr-title     48
     :statute/cfr-node      [["chapter" "1"] ["subchapter" "H"] ["part" "52"] ["subpart" "52.2"] ["section" "52.204-3"]]
     :statute/url           "https://www.ecfr.gov/current/title-48/chapter-1/subchapter-H/part-52/subpart-52.2/section-52.204-3"
     :statute/verified-label "Taxpayer identification."
     :statute/verified-via  "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-48.json"
     :statute/verified-at   "2026-08-20"
     :statute/note
     "The provision that actually collects it, sitting two sections from
      52.204-10 in the same subpart. Their adjacency is the whole lesson: the
      identifier the government asks a contractor for, and the disclosure
      status that RELIEVES the contractor of a report, are neighbours in the
      FAR and are constantly conflated by products that sell both as
      `compliance`."}

    ;; -----------------------------------------------------------------------
    ;; Assistance side: awarding money to the operator.

    {:statute/id            :assistance/exec-comp-reporting
     :statute/topic         #{:assistance :executive-compensation}
     :statute/hat           :assistance-side
     :statute/title         "2 CFR Part 170 -- Reporting Subaward and Executive Compensation Information"
     :statute/cfr-title     2
     :statute/cfr-node      [["subtitle" "A"] ["chapter" "I"] ["part" "170"]]
     :statute/url           "https://www.ecfr.gov/current/title-2/subtitle-A/chapter-I/part-170"
     :statute/verified-label "Reporting Subaward and Executive Compensation Information"
     :statute/verified-via  "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-2.json"
     :statute/verified-at   "2026-08-20"
     :statute/note
     "The assistance-side mirror. Unlike the identifier question -- where the
      USA-TREASURY leaf found the contract and assistance sides genuinely
      DIFFER -- the executive-compensation exemption is the same on both sides.
      That symmetry is itself the finding: an operator does not need to know
      which side of the money they are on to answer this one, which makes it
      the rare federal question with a single answer."}

    {:statute/id            :assistance/applicability
     :statute/topic         #{:assistance :executive-compensation}
     :statute/hat           :exemption-hinge
     :statute/title         "2 CFR 170.105 -- Applicability"
     :statute/cfr-title     2
     :statute/cfr-node      [["subtitle" "A"] ["chapter" "I"] ["part" "170"] ["subpart" "A"] ["section" "170.105"]]
     :statute/url           "https://www.ecfr.gov/current/title-2/subtitle-A/chapter-I/part-170/subpart-A/section-170.105"
     :statute/verified-label "Applicability."
     :statute/verified-via  "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-2.json"
     :statute/verified-at   "2026-08-20"
     :statute/quote-part    "170"
     :statute/quote-section "170.105"
     :statute/verified-quotes
     ["The public does not have access to information about the compensation of senior executives of the entity through periodic reports filed under section 13(a) or 15(d) of the Securities Exchange Act of 1934"]
     :statute/note
     "The assistance-side exemption, in the section that decides who the part
      applies to. Note `senior executives` here against the FAR's bare
      `executives`: the two rulebooks impose the same test in almost, but not
      exactly, the same words, which is why this catalog quotes each from its
      own document instead of quoting one and asserting the other matches."}

    {:statute/id            :assistance/award-term
     :statute/topic         #{:assistance}
     :statute/hat           :assistance-side
     :statute/title         "2 CFR 170.220 -- Use of award term"
     :statute/cfr-title     2
     :statute/cfr-node      [["subtitle" "A"] ["chapter" "I"] ["part" "170"] ["subpart" "B"] ["section" "170.220"]]
     :statute/url           "https://www.ecfr.gov/current/title-2/subtitle-A/chapter-I/part-170/subpart-B/section-170.220"
     :statute/verified-label "Use of award term."
     :statute/verified-via  "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-2.json"
     :statute/verified-at   "2026-08-20"
     :statute/quote-part    "170"
     :statute/quote-section "170.220"
     :statute/verified-quotes
     ["A Federal agency must include the award term in Appendix A to this part in each Federal award to a recipient under which the total funding is anticipated to equal or exceed $30,000"]
     :statute/note
     "How the test reaches the operator: not as a regulation they look up, but
      as a term written into their award. This is why the exemption question
      arrives in contract language rather than in a compliance checklist, and
      why the counted occurrences below span the appendix as well as the rule."}

    {:statute/id            :assistance/uei
     :statute/topic         #{:assistance :identifier}
     :statute/hat           :assistance-side
     :statute/title         "2 CFR Part 25 -- Unique Entity Identifier and System for Award Management"
     :statute/cfr-title     2
     :statute/cfr-node      [["subtitle" "A"] ["chapter" "I"] ["part" "25"]]
     :statute/url           "https://www.ecfr.gov/current/title-2/subtitle-A/chapter-I/part-25"
     :statute/verified-label "Unique Entity Identifier and System for Award Management"
     :statute/verified-via  "https://www.ecfr.gov/api/versioner/v1/structure/2026-08-18/title-2.json"
     :statute/verified-at   "2026-08-20"
     :statute/note
     "The identifier that DOES gate federal assistance, carried so that finding
      1 is a comparison rather than an assertion. Eligibility runs on the
      unique entity identifier and SAM.gov registration -- neither of which the
      SEC issues, and neither of which an SEC filing substitutes for. The
      USA-TREASURY leaf establishes the same part from the taxpayer-identifier
      side; both leaves need it, so both carry it."}
    ]})

;; ---------------------------------------------------------------------------
;; Absences.
;;
;; A negative claim fails open: scan the wrong tree, or an empty document, and
;; `nothing matched` is indistinguishable from `confirmed still absent`. Every
;; absence therefore carries a CONTROL that must match in the same tree or the
;; same document. A control that stops matching is reported as could-not-answer
;; (exit 2), never as a pass and never as a drift -- the run simply did not
;; establish anything.

(def absences
  "Negative claims this catalog depends on, each with a control."
  [
   {:absence/id :title-17/carries-no-procurement-vocabulary
    :absence/absent-label {:statute/cfr-title 17
                           :statute/under []
                           :statute/pattern "(?i)federal acquisition|procurement|contractor"}
    :absence/control-label {:statute/pattern "(?i)securities"}
    :absence/see-instead
    {:statute/cfr-title 17
     :statute/cfr-node  [["chapter" "II"] ["part" "240"] ["subject_group" "*"] ["section" "240.21F-2"]]
     :statute/verified-label "Whistleblower status, award eligibility, confidentiality, and retaliation protections."}
    :absence/note
    "Finding 3, as a negative. Across every node of title 17 no label contains
     `Federal Acquisition`, `procurement` or `contractor`; the control confirms
     the scan really read the securities title. The see-instead points at the
     single label that a procurement-shaped search DOES hit, so that the
     absence and its one false positive are checked by the same run. Scoped to
     the whole title rather than to chapter II deliberately: the claim being
     made is about what an operator finds when they search `17 CFR`, which is
     the container they were given, not the chapter they should have used."}

   {:absence/id :far-52/headings-never-name-the-exchange-act
    :absence/absent-label {:statute/cfr-title 48
                           :statute/under [["chapter" "1"] ["subchapter" "H"] ["part" "52"]]
                           :statute/pattern "Exchange Act"}
    :absence/control-label {:statute/pattern "(?i)securit"}
    :absence/note
    "The FAR clause at 52.204-10 turns on the Securities Exchange Act, yet no
     heading anywhere in part 52 says so. The control is instructive rather
     than incidental: eight headings in this part DO match `securit`, and they
     are about classified-information safeguards and supply-chain security --
     `52.204-2 Security Requirements` sits two sections from the clause that
     actually concerns securities law. A reader browsing the table of contents
     for the operator's securities obligations finds the wrong eight and misses
     the one."}

   {:absence/id :far-52-204-10/agency-name-never-spelled-correctly
    :absence/absent-text {:statute/cfr-title 48
                          :statute/part "52"
                          :statute/section "52.204-10"
                          :statute/pattern "Securities and Exchange Commission"}
    :absence/control-text {:statute/pattern "Security and Exchange Commission"}
    :absence/note
    "Finding 2 on the contract side, stated as the negative an operator would
     actually hit. Inside the clause that decides the reporting duty, the
     agency's real name does not occur at all. The control is the misspelling,
     so a run that fetched an empty or wrong document cannot report this as
     confirmed -- and the pairing is the evidence: the name IS there, spelled
     wrong, which is a different fact from the agency being unmentioned."}

   {:absence/id :assistance-170/agency-name-never-spelled-correctly
    :absence/absent-text {:statute/cfr-title 2
                          :statute/part "170"
                          :statute/pattern "Securities and Exchange Commission"}
    :absence/control-text {:statute/pattern "Security and Exchange Commission"}
    :absence/note
    "The same negative on the assistance side, and scoped to the WHOLE part
     rather than one section, because here the claim is stronger: in all of 2
     CFR part 170 -- rule, subparts and the award term in Appendix A -- the
     correct spelling never appears. Two independent OMB and FAR Council texts
     making the same error is what moves this from a typo to a search hazard
     worth encoding."}
   ])

;; ---------------------------------------------------------------------------
;; Occurrence counts.
;;
;; New in this leaf, and the reason: the USA-TREASURY round recorded in its own
;; landing notes that its gate checked quote PRESENCE and not occurrence count,
;; so a section that gained a second copy of a span would still pass. That gap
;; is fatal here, because this catalog's central finding is arithmetic -- `zero
;; correct spellings, two incorrect` is not expressible as present/absent. A
;; presence check cannot distinguish `the FAR names the SEC correctly nowhere
;; in this clause` from `the FAR was fixed and now names it correctly once`.
;;
;; Rules the gate enforces:
;;   * an expectation of 0 MUST carry a control pattern, for the same reason
;;     absences must: an empty document satisfies every claim of the form
;;     `this string occurs zero times`;
;;   * a blank document is could-not-answer, never a pass;
;;   * counting is done on the same flattened text the quote checks use, so a
;;     count recorded here and a quote recorded above are measured by one
;;     instrument. Recording a number produced by a different flattener would
;;     be measuring something else and reporting it as this.

(def occurrence-counts
  "Exact occurrence counts that this catalog's claims rest on."
  [
   {:count/id :far-52-204-10/correct-name
    :count/cfr-title 48 :count/part "52" :count/section "52.204-10"
    :count/pattern "Securities and Exchange Commission"
    :count/expect 0
    :count/control "Security and Exchange Commission"
    :count/note
    "Zero. The clause that decides the duty never spells the agency's name."}

   {:count/id :far-52-204-10/misspelled-name
    :count/cfr-title 48 :count/part "52" :count/section "52.204-10"
    :count/pattern "Security and Exchange Commission"
    :count/expect 2
    :count/note
    "Twice, and both are the operative pointer telling the reader where to go
     check whether the public has access to the compensation information."}

   {:count/id :far-52/correct-name-elsewhere
    :count/cfr-title 48 :count/part "52"
    :count/pattern "Securities and Exchange Commission"
    :count/expect 3
    :count/note
    "The part DOES spell it correctly three times -- in an administrative-
     proceedings example, a records-retention list naming the 10-K, and the
     NRSRO registration reference. None of the three is the reporting test.
     This count is what makes the finding `never where it matters` rather than
     the weaker and false `the FAR does not mention the SEC`. It costs a fetch
     of the entire part; that is the price of the distinction."}

   {:count/id :far-52/misspelled-name-part-wide
    :count/cfr-title 48 :count/part "52"
    :count/pattern "Security and Exchange Commission"
    :count/expect 2
    :count/note
    "Both misspellings in the whole part are the two inside 52.204-10. The
     error is not scattered through the FAR; it is localised to exactly the
     clause an operator needs."}

   {:count/id :assistance-170/correct-name
    :count/cfr-title 2 :count/part "170"
    :count/pattern "Securities and Exchange Commission"
    :count/expect 0
    :count/control "Security and Exchange Commission"
    :count/note
    "Zero across the entire part, including the award term that goes into every
     covered federal award."}

   {:count/id :assistance-170/misspelled-name
    :count/cfr-title 2 :count/part "170"
    :count/pattern "Security and Exchange Commission"
    :count/expect 2
    :count/note
    "Twice, in the recipient and subrecipient branches of the same pointer."}

   {:count/id :assistance-170/exemption-restated
    :count/cfr-title 2 :count/part "170"
    :count/pattern "section 13\\(a\\) or 15\\(d\\) of the Securities Exchange Act of 1934"
    :count/expect 3
    :count/regex? true
    :count/note
    "Three times: once in the applicability rule and twice in Appendix A, for
     recipients and for subrecipients. The count is the evidence for a claim
     the prose makes -- that the exemption does not travel down a funding
     chain, but is re-tested at each tier. A drop to one would mean the tiers
     were consolidated and the advice above would need rewriting."}

   {:count/id :sec-21F-2/company-excluded
    :count/cfr-title 17 :count/part "240" :count/section "240.21F-2"
    :count/pattern "A company or other entity is not eligible to be a whistleblower."
    :count/expect 1
    :count/note
    "The sentence that disarms title 17's only `award eligibility` hit. Counted
     rather than merely quoted so that this catalog notices if the SEC ever
     restates it -- the false-friend finding depends on this exclusion being
     unambiguous and in one place."}
   ])

;; ---------------------------------------------------------------------------
;; Accessors.

(defn entries
  "Every catalog entry, flattened across ISO keys."
  []
  (vec (mapcat val catalog)))

(defn by-hat
  "Entries wearing `hat`."
  [hat]
  (filterv #(= hat (:statute/hat %)) (entries)))

(defn quoted-entries
  "Entries carrying at least one verified quote -- the ones whose operative
  claim, not merely whose address, is checked by the live gate."
  []
  (filterv #(seq (:statute/verified-quotes %)) (entries)))

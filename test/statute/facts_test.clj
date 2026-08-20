(ns statute.facts-test
  "Offline invariants for the USA-SEC compliance catalog.

  These tests deliberately do NOT reach the network -- that is
  `tools/verify_citations.cljs`, which re-fetches the eCFR APIs and is the only
  thing that can tell you whether a citation is still true. What these tests
  pin is the shape the live gate depends on: if the catalog stops carrying the
  fields the gate reads, the gate degrades into checking less and still exits
  0, which is the failure mode where a green light means nothing.

  So the load-bearing tests here are the ones that would let the live gate
  quietly check less:
    * every entry carries the node path, label and title the gate walks;
    * every absence carries a control, and every zero-expectation count carries
      one too, without which an empty document confirms the claim for free;
    * the sections whose text this leaf's advice rests on carry the NUMBER of
      quotes their claims need, not merely one;
    * the three findings in the namespace docstring are each still encoded as
      DATA somewhere, so that deleting the evidence cannot leave the prose
      standing."
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.string :as str]
            [statute.facts :as f]))

(def all (f/entries))

(def hats #{:securities-regulator :exemption-hinge :contract-side
            :assistance-side :title-cotenant :false-friend})

;; ---------------------------------------------------------------------------
;; The catalog exists and is not vacuous.

(deftest catalog-is-not-empty
  (testing "a catalog that shrank to nothing must fail here, not pass silently"
    (is (seq all))
    (is (>= (count all) 20)
        "the live gate's default --min is 20 headings; if the catalog drops
         below that the gate reports could-not-answer, so failing here first
         gives a better message")))

(deftest catalog-key-is-the-blueprint-key
  (is (= #{"USA-SEC"} (set (keys f/catalog)))
      "this leaf carries exactly one ISO key and it must match blueprint.edn"))

(deftest statute-ids-are-unique
  (let [ids (map :statute/id all)]
    (is (= (count ids) (count (distinct ids)))
        "a duplicated :statute/id silently makes one entry unreachable by id")))

;; ---------------------------------------------------------------------------
;; Every field the live gate reads is present.

(deftest every-entry-carries-what-the-gate-walks
  (doseq [e all]
    (testing (str (:statute/id e))
      (is (keyword? (:statute/id e)))
      (is (integer? (:statute/cfr-title e)))
      (is (vector? (:statute/cfr-node e))
          "the gate walks this path; a missing one would be skipped")
      (is (string? (:statute/verified-label e)))
      (is (string? (:statute/verified-via e)))
      (is (string? (:statute/url e)))
      (is (contains? hats (:statute/hat e))
          "an unknown hat means a role this catalog has not thought about")
      (is (string? (:statute/note e))))))

(deftest node-paths-are-type-identifier-pairs
  (doseq [e all, step (:statute/cfr-node e)]
    (testing (str (:statute/id e) " step " (pr-str step))
      (is (vector? step))
      (is (= 2 (count step)))
      (is (every? string? step)))))

(deftest every-cited-title-has-a-structure-endpoint
  (doseq [t (distinct (map :statute/cfr-title all))]
    (is (contains? f/ecfr-structure-api t)
        (str "CFR title " t " is cited but has no declared structure endpoint,
              which the gate reports as could-not-answer"))))

;; ---------------------------------------------------------------------------
;; Quotes: the half that makes the NOTES falsifiable, not just the addresses.

(deftest quoted-entries-carry-a-fetchable-address
  (doseq [e (f/quoted-entries)]
    (testing (str (:statute/id e))
      (is (string? (:statute/quote-part e)))
      (is (string? (:statute/quote-section e)))
      (is (contains? f/ecfr-full-text-api (:statute/cfr-title e))
          "a quote whose title has no full-text endpoint cannot be checked")
      (is (every? #(and (string? %) (seq %)) (:statute/verified-quotes e))))))

(deftest enough-of-the-catalog-is-quoted
  (is (>= (reduce + (map #(count (:statute/verified-quotes %)) (f/quoted-entries))) 8)
      "the gate's --min-quotes floor is 8; below it the run is not evidence"))

(deftest the-false-friend-carries-both-halves
  (let [e (first (filter #(= :sec/whistleblower-award-eligibility (:statute/id %)) all))]
    (is (some? e))
    (is (= 2 (count (:statute/verified-quotes e)))
        "this entry needs BOTH spans: the eligibility phrase that a keyword
         search would take as a match, and the sentence excluding companies
         that makes it inapplicable. Quoting only the first would turn the
         entry into corroboration of the error it exists to refute")
    (is (some #(str/includes? % "not eligible to be a whistleblower")
              (:statute/verified-quotes e)))))

;; ---------------------------------------------------------------------------
;; Absences: a negative with no control is confirmed by an empty document.

(deftest every-absence-has-a-control
  (doseq [a f/absences]
    (testing (str (:absence/id a))
      (is (keyword? (:absence/id a)))
      (is (string? (:absence/note a)))
      (cond
        (:absence/absent-text a)
        (is (get-in a [:absence/control-text :statute/pattern])
            "an empty HTTP body satisfies every claim of the form
             `these words do not appear`")
        (:absence/absent-label a)
        (is (get-in a [:absence/control-label :statute/pattern])
            "a scan of the wrong subtree finds nothing and looks like proof")
        (:absence/absent-part a)
        (is (get-in a [:absence/control-part :statute/part]))
        :else
        (is false "an absence that claims nothing")))))

(deftest text-absences-name-a-fetchable-document
  (doseq [a f/absences :let [t (:absence/absent-text a)] :when t]
    (testing (str (:absence/id a))
      (is (contains? f/ecfr-full-text-api (:statute/cfr-title t)))
      (is (string? (:statute/part t))))))

;; ---------------------------------------------------------------------------
;; Counts: this leaf's addition, and the only way its central claim is sayable.

(deftest count-ids-are-unique
  (let [ids (map :count/id f/occurrence-counts)]
    (is (= (count ids) (count (distinct ids))))))

(deftest every-count-is-well-formed
  (doseq [c f/occurrence-counts]
    (testing (str (:count/id c))
      (is (keyword? (:count/id c)))
      (is (integer? (:count/expect c))
          "a non-numeric expectation is reported by the gate as could-not-answer")
      (is (nat-int? (:count/expect c)))
      (is (string? (:count/pattern c)))
      (is (string? (:count/part c)))
      (is (contains? f/ecfr-full-text-api (:count/cfr-title c)))
      (is (string? (:count/note c))))))

(deftest zero-expectations-carry-a-control
  (doseq [c f/occurrence-counts :when (zero? (:count/expect c))]
    (testing (str (:count/id c))
      (is (string? (:count/control c))
          "`this string occurs zero times` is satisfied by an empty document,
           so a zero-expectation without a control is not a check")
      (is (not= (:count/control c) (:count/pattern c))
          "a control identical to the pattern under test cannot both be absent
           and present; that mistake makes the check unsatisfiable rather than
           vacuous, which is a different bug with the same red"))))

(deftest enough-counts-to-meet-the-floor
  (is (>= (count f/occurrence-counts) 7)
      "the gate's --min-counts floor is 7"))

;; ---------------------------------------------------------------------------
;; The findings in the docstring are encoded as data, not only as prose.

(deftest finding-1-the-exemption-is-recorded-on-both-sides
  (testing "SEC-filer status relieves a federal reporting duty on BOTH sides,
            so both the FAR clause and the 2 CFR rule must carry the quoted
            condition -- a catalog that kept only one could not support the
            claim that the answer is the same either way"
    (let [spans (mapcat :statute/verified-quotes (f/quoted-entries))
          hits (filter #(str/includes? % "section 13(a) or 15(d) of the Securities Exchange Act of 1934") spans)]
      (is (= 2 (count hits))
          "one span from 48 CFR 52.204-10 and one from 2 CFR 170.105"))))

(deftest finding-2-the-misspelling-is-counted-on-both-sides
  (testing "zero correct spellings and two incorrect, in each rulebook"
    (let [zeroes (filter #(and (zero? (:count/expect %))
                               (= "Securities and Exchange Commission" (:count/pattern %)))
                         f/occurrence-counts)]
      (is (= 2 (count zeroes))
          "the FAR clause and 2 CFR part 170 each contribute one zero-count;
           losing either reduces the finding to an anecdote about one agency")
      (is (= #{48 2} (set (map :count/cfr-title zeroes)))))))

(deftest finding-3-title-17-co-tenancy-is-recorded
  (testing "17 CFR is not the SEC's title"
    (let [tenants (f/by-hat :title-cotenant)]
      (is (>= (count tenants) 3)
          "the title root plus the two non-SEC chapters; dropping them would
           leave the README's claim unsupported by any citation")
      (is (some #(= "Commodity Futures Trading Commission" (:statute/verified-label %)) tenants))
      (is (some #(= "Department of the Treasury" (:statute/verified-label %)) tenants)))))

(deftest the-sec-name-is-spelled-correctly-in-this-catalogs-own-prose
  (testing "the quoted CFR spans reproduce the government's misspelling on
            purpose, but this leaf's own notes must not adopt it"
    (doseq [e all]
      (is (not (str/includes? (:statute/note e) "Security and Exchange Commission"))
          (str (:statute/id e) " repeats the misspelling in its own note; the
                misspelling belongs only inside a verified quote")))))

;; ---------------------------------------------------------------------------
;; The README quotes numbers. Numbers in prose drift silently, and a reader has
;; no way to tell a stale count from a current one -- so they are pinned to the
;; data here rather than trusted.

(def readme (slurp "README.md"))

(deftest readme-counts-match-the-catalog
  (testing "every number the README asserts about the catalog is derived from it"
    (is (str/includes? readme (str "**" (count all) " verified regulatory anchors**"))
        "README anchor count drifted from the catalog")
    (is (str/includes? readme
                       (str "**" (reduce + (map #(count (:statute/verified-quotes %))
                                                (f/quoted-entries)))
                            " byte-exact quoted spans**"))
        "README quote-span count drifted")
    (is (str/includes? readme (str "**" (count f/occurrence-counts) " exact occurrence counts**"))
        "README count total drifted")
    (is (str/includes? readme (str "**" (count f/absences) " checked absences**"))
        "README absence count drifted")))

(deftest readme-title-list-matches-the-catalog
  (let [titles (sort (distinct (map :statute/cfr-title all)))]
    (is (= [2 17 48] titles))
    (is (str/includes? readme "titles **2, 17 and 48**")
        "README names a different set of CFR titles than the catalog cites")))

(deftest readme-reports-the-zero-counts-it-claims
  (testing "the headline `0 correct / 2 misspelled` must be what the data says"
    (let [correct (filter #(= "Securities and Exchange Commission" (:count/pattern %))
                          f/occurrence-counts)
          wrong   (filter #(= "Security and Exchange Commission" (:count/pattern %))
                          f/occurrence-counts)
          in-clause (fn [cs t p s]
                      (first (filter #(and (= t (:count/cfr-title %))
                                           (= p (:count/part %))
                                           (= s (:count/section %))) cs)))]
      (is (zero? (:count/expect (in-clause correct 48 "52" "52.204-10"))))
      (is (= 2   (:count/expect (in-clause wrong   48 "52" "52.204-10"))))
      (is (zero? (:count/expect (in-clause correct 2 "170" nil))))
      (is (= 2   (:count/expect (in-clause wrong   2 "170" nil)))))))

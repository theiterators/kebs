# Plan 0 — kebs 2.3.0 release (dependency bumps) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Release kebs 2.3.0 from master with the pending dependency updates, then cut `series/2.x` so master can become 3.0-SNAPSHOT.

**Architecture:** Operational plan — merge Scala Steward PRs, verify, prepare the release commit, tag, publish the drafted GitHub release, branch. No library code changes.

**Tech Stack:** sbt 1.12.13, sbt-typelevel 0.8.7 (`tlBaseVersion`, `tlMimaPreviousVersions`, tag-triggered publishing), `gh` CLI.

**Spec:** `design/2026-11-21-kebs-3-design.md` §5.1 step 0.

## Global Constraints

- 2.x stays on Scala 3.3 LTS (`scala_3 = "3.3.x"`) and Scala 2.13.18; JDK 11 + 17 in CI.
- Repo convention: squash merges titled `<PR title> (#N)`.
- **Every merge, every push to master, the tag push and the release publish need the maintainer's explicit go-ahead in the session** (outward-facing, hard to reverse).
- `ci.yml` is generated (sbt-github-actions); never hand-edit. The local `gh` token lacks the `workflow` scope: merging a PR that touches `.github/workflows/` fails — ask the maintainer to run `! gh auth refresh -h github.com -s workflow`. None of the PRs below touch workflows.
- Release process (from the 2.2.0 release): prep commit on master bumping `tlBaseVersion`, `tlMimaPreviousVersions`, README install-snippet versions; run `sbt +mimaReportBinaryIssues` locally before tagging; push tag `vX.Y.Z` (CI publishes to Maven Central); publish the Release Drafter draft with `gh release edit <draft-tag> --tag vX.Y.Z --title "vX.Y.Z 🥙" --draft=false --latest`.

## Review Focus

- A Steward PR built against a newer Scala 3 (TASTy too new for 3.3) — CI fails with "TASTy signature has wrong version". Expected: not merged into 2.x (this is why #610 scala-java-time 2.11.0 is excluded). Re-check after each merge that master CI stays green.
- Two PRs editing adjacent lines of `build.sbt` — the second merge conflicts. Expected: resolve on the Steward branch (`gh pr checkout N`, rebase on master, push over SSH) or ask Steward to rebase; never push the PR content straight to master.
- MiMa against 2.2.2 reports a binary break caused by a dependency bump (e.g. http4s-stir 0.5 → 0.7 is early-semver-major for that library). Expected: kebs' own API unchanged → no MiMa problems in kebs artifacts; if MiMa does flag kebs classes, stop and report to the maintainer before tagging.
- The tag CI run fails mid-publish. Expected: do not re-tag blindly; report and investigate (Sonatype staging state).
- `series/2.x` branch name already exists on the remote. Expected: check first (`git ls-remote --heads origin series/2.x`), and ask if it exists.

---

### Task 1: Merge the dependency PRs

**PRs to merge (all CI-green or infra-flake only, as of 2026-11-21):** #607 (Scala 3.3.9), #596 (circe 0.14.17), #595 (pekko 1.7.1), #600 (pekko-http 1.4.2), #603 (pureconfig 0.17.11), #613 (http4s-dsl 0.23.39), #605 (http4s-stir 0.7.1), #612 (baklava-core 2.4.0), #602 (scalacheck-derived 0.11.0), #592 (magnolify-scalacheck 0.9.8), #614 (sbt-scalajs 1.23.0), #615 (sbt-jmh 0.4.9).

**Not merged (left open for kebs 3):** #610 scala-java-time 2.11.0 (built with Scala 3.9), #611 / #556 doobie RC14 / RC13 (package relocation, breaking), #604 scala-library 3.10.0, #531 jsoniter (contributor PR, superseded by the kebs 3 jsoniter module — closed in the kebs 3 jsoniter plan with credit).

- [ ] **Step 1: Re-run the failed "Validate Steward Config" check on #612**

Run: `gh run rerun 44217261099 --failed`
Expected: the job passes (the failure was a coursier download error, not a code problem). Check with `gh pr checks 612`.

- [ ] **Step 2: Get the maintainer's go-ahead to merge the 12 PRs listed above**

- [ ] **Step 3: Merge each PR, one at a time, re-checking mergeability between merges**

Run, per PR: `gh pr view N --json mergeable --jq .mergeable` → expect `MERGEABLE`; then
`gh pr merge N --squash --subject "<PR title> (#N)"`.
If a PR becomes `CONFLICTING`, resolve as described in Review Focus, wait for its CI, then merge.

- [ ] **Step 4: Verify master locally on both Scala versions**

Run: `git checkout master && git pull --ff-only && sbt +test`
Expected: all tests pass on 2.13.18 and 3.3.9.

- [ ] **Step 5: Verify master CI**

Run: `gh run list --branch master --limit 3`
Expected: the latest CI run on master is `success`.

### Task 2: Release commit, tag, GitHub release

- [ ] **Step 1: Check binary compatibility against 2.2.2 before bumping**

Run: `sbt +mimaReportBinaryIssues`
Expected: no binary incompatibilities reported for kebs modules.

- [ ] **Step 2: Prepare the release commit**

In `build.sbt`: `ThisBuild / tlBaseVersion := "2.3"` and `ThisBuild / tlMimaPreviousVersions := Set("2.2.2")`.
In `README.md`: install-snippet versions → `2.3.0`.
Commit message: `Update version to 2.3.0, bump MiMa previous to 2.2.2`.

- [ ] **Step 3: Re-run MiMa with the new settings**

Run: `sbt +mimaReportBinaryIssues`
Expected: no issues.

- [ ] **Step 4: Get the maintainer's go-ahead, then push the commit and the tag**

Run: `git push origin master && git tag v2.3.0 && git push origin v2.3.0`

- [ ] **Step 5: Watch the tag's CI run until publishing finishes**

Run: `gh run list --branch v2.3.0 --limit 1` then `gh run watch <id>`
Expected: `success`; artifacts appear under `https://repo1.maven.org/maven2/pl/iterators/kebs-core_3/2.3.0/` (may take up to ~30 min to sync).

- [ ] **Step 6: Publish the drafted release**

Run: `gh release list --limit 3` (find the Release Drafter draft), then
`gh release edit <draft-tag> --tag v2.3.0 --title "v2.3.0 🥙" --draft=false --latest`
Expected: `gh release view v2.3.0` shows a published release with the changelog.

### Task 3: Cut `series/2.x` and tidy the issue tracker

- [ ] **Step 1: Check the branch name is free**

Run: `git ls-remote --heads origin series/2.x`
Expected: no output.

- [ ] **Step 2: Create and push `series/2.x` from the tag (maintainer go-ahead)**

Run: `git branch series/2.x v2.3.0 && git push origin series/2.x`

- [ ] **Step 3: Close #410 (sbt-typelevel migration — already done) with a short comment (maintainer go-ahead)**

Run: `gh issue close 410 --comment "Done — kebs builds with sbt-typelevel (sbt-typelevel-ci-release 0.8.7)."`

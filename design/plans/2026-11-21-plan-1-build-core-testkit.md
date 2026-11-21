# Plan 1 — kebs 3: build switch, core `ValueClassLike` on hearth, testkit + golden tests — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Turn master into kebs 3.0-SNAPSHOT on Scala 2.13.18 + 3.9.0 / JDK 17+, port core's `ValueClassLike` macro to hearth, drop akka-http, migrate doobie to RC14, and pin today's JSON wire format with golden tests before any other macro is rewritten.

**Architecture:** One PR ("kebs 3: build + core"). Build changes first, then the first hearth macro (shared logic in `src/main/scala`, thin `scala-2`/`scala-3` entry points), then a test-only `kebs-testkit` cross-project with shared models, and golden tests that capture current circe / spray-json / play-json output.

**Tech Stack:** sbt 1.12.13, sbt-typelevel 0.8.7, Scala 2.13.18 / 3.9.0, hearth 0.4.5 (+ `hearth-cross-quotes_3` compiler plugin), scalatest 3.2.20, doobie 1.0.0-RC14.

**Spec:** `design/2026-11-21-kebs-3-design.md` (§3 constraints, §4.2.1, §5.1 step 2, §5.2, §5.5 core, §6, §6.1, §7.1).

**Prerequisite:** Plan 0 done (v2.3.0 tagged, `series/2.x` cut). Work on branch `kebs-3/build-core` from master.

## Global Constraints

- Scala versions: `scala_2_13 = "2.13.18"`, `scala_3 = "3.9.0"`. JDK 17 and 21 in CI.
- `tlBaseVersion := "3.0"`; MiMa off for 3.0.0 (`tlMimaPreviousVersions := Set.empty`).
- hearth `0.4.5` exactly (kindlings 0.4.0 depends on it; later plans add kindlings at the same hearth version).
- Every existing test stays green on 2.13 and 3.9 (JVM; JS/Native for modules that have them).
- Golden tests capture **current** behaviour. If current output differs from a literal in this plan, stop and report it — never adjust a literal to make a golden test pass without the maintainer's decision.
- `ci.yml` is generated: change `build.sbt`, run `sbt githubWorkflowGenerate`, commit the result. Pushing/merging changes to `.github/workflows/` needs the `workflow` token scope — ask the maintainer to run `! gh auth refresh -h github.com -s workflow` before opening the PR.
- Do not put planning/design files under `docs/` (mdoc publishes everything there).
- Scalafmt runs on compile; commit formatted code.

## Review Focus

- **Case class with a validating or normalising companion `apply`** (e.g. `object Email { def apply(v: String) = new Email(v.toLowerCase) }`). Expected: `ValueClassLike.apply` goes through it on both Scala versions (Scala 3 used to bypass it). Pinned in Task 3.
- **Generic single-field case class** (`Box[A](a: A)`) and one **defined in another compilation unit** (testkit). Expected: `ValueClassLike[Box[Int], Int]` and `ValueClassLike[testkit.Id, String]` resolve. Pinned in Task 3 (`Box`) and Task 4 (`person flat` resolves `ValueClassLike` for `testkit.model.Id`, compiled in another module; core's own tests cannot depend on testkit without a cycle).
- **Flat-format search over every type** — `flatDecoder[T, A](implicit rep: ValueClassLike[T, A], …)` is tried for `Int`, `List[X]`, 2-field case classes, sealed traits. Expected: no instance and no compile error for those (the existing JSON/HTTP test suites exercise this heavily; they must stay green). Pinned by Task 3's `assertDoesNotCompile` cases plus the full `+test` run.
- **A field the goldens don't cover changes encoding after Scala 3.9** (e.g. `Option` inside a flat value class). Expected: same JSON as 2.3.0. Goldens include `Option` fields, flat value classes, enums, >22 fields and an `InstanceConverter` type (Tasks 4–6).
- **JS/Native builds** of core with the hearth dependency. Expected: `coreJS/test` and `coreNative/test` pass (hearth is published for both). Pinned in Task 3 Step 6.

---

### Task 1: Scala 3.9, JDK 17/21, version 3.0, doobie RC14, scala-java-time 2.11

**Files:**
- Modify: `build.sbt` (`scala_3`, `tlBaseVersion`, `tlMimaPreviousVersions`, `githubWorkflowJavaVersions`, doobie and scala-java-time coordinates, Scala 3 `-Ykind-projector` → `-Xkind-projector` in `commonSettings`)
- Modify: `doobie/src/main/scala/pl/iterators/kebs/doobie/KebsDoobie.scala`, `doobie/src/main/scala/pl/iterators/kebs/doobie/enums/KebsDoobieEnums.scala`, and the doobie test sources (6 `import doobie…` lines in total)
- Modify: `.github/workflows/ci.yml` (regenerated)

- [ ] **Step 1: Apply the build changes**

`val scala_3 = "3.9.0"`; `ThisBuild / tlBaseVersion := "3.0"`; `ThisBuild / tlMimaPreviousVersions := Set.empty`;
`ThisBuild / githubWorkflowJavaVersions := Seq(JavaSpec.temurin("17"), JavaSpec.temurin("21"))`;
`val doobie = "org.typelevel" %% "doobie-core" % "1.0.0-RC14"`, `val doobiePg = "org.typelevel" %% "doobie-postgres" % "1.0.0-RC14"`;
`scalaJavaTime` → `"2.11.0"`.

- [ ] **Step 2: Run the doobie module to see the expected failure**

Run: `sbt "++3.9.0" doobieSupport/Test/compile`
Expected: FAIL — `Not found: doobie` in `KebsDoobie.scala:3` and `KebsDoobieEnums.scala:3`.

- [ ] **Step 3: Move doobie imports to the relocated package**

Every `import doobie.X` / `import doobie._` / `import doobie.implicits._` / `import doobie.postgres._` becomes `import org.typelevel.doobie.X` / `org.typelevel.doobie._` / `org.typelevel.doobie.implicits._` / `org.typelevel.doobie.postgres._` (package verified in the RC14 jar: `org/typelevel/doobie/**`).

- [ ] **Step 4: Run all tests on both Scala versions (JVM)**

Run: `sbt +test`
Expected: PASS on 2.13.18 and 3.9.0. New Scala 3.9 deprecation warnings (`_` wildcard in types, `with` type operator, implicit args without `using`) are acceptable in this task.

- [ ] **Step 5: Regenerate CI and commit**

Run: `sbt githubWorkflowGenerate`
```bash
git add build.sbt doobie .github/workflows/ci.yml
git commit -m "kebs 3: Scala 3.9.0, JDK 17/21, version 3.0, doobie 1.0.0-RC14"
```

### Task 2: Drop akka-http

**Files:**
- Delete: `akka-http/`
- Modify: `build.sbt` (remove `akkaHttpSupport`, `akkaHttpSettings`, `akkaVersion`, `akkaHttpVersion`, `akkaStream`, `akkaStreamTestkit`, `akkaHttp`, `akkaHttpTestkit`, `akkaHttpInExamples`, and `akkaHttpSupport` from the root `aggregate`)
- Rename: `docs/akka-http.md` → `docs/pekko-http.md` (title `Pekko HTTP`, heading `# kebs-pekko-http`, remove the Akka setup line and "Akka" wording)
- Modify: `docs/http4s.md` (link `akka-http.md#path-matchers` → `pekko-http.md#path-matchers`, "Akka/Pekko HTTP matchers" → "Pekko HTTP matchers"; keep "Akka HTTP-style directives" — that describes http4s-stir's style), `docs/core-concepts.md` ("circe, slick, akka-http, etc." → "circe, slick, pekko-http, etc."), `README.md` (remove the `kebs-akka-http` row)
- Modify: `.github/workflows/ci.yml` (regenerated)

- [ ] **Step 1: Remove the module, its build definitions and docs references as listed above**

- [ ] **Step 2: Verify nothing references akka any more**

Run: `grep -rni "akka" --include=*.sbt --include=*.scala --include=*.md . | grep -v "/target/" | grep -vi "akka http-style"`
Expected: no output.

- [ ] **Step 3: Verify the build and docs**

Run: `sbt projects +compile docs/mdoc`
Expected: no `akkaHttpSupport` in the project list; compile and mdoc succeed.

- [ ] **Step 4: Regenerate CI and commit**

Run: `sbt githubWorkflowGenerate`
```bash
git add -A build.sbt akka-http docs README.md .github/workflows/ci.yml
git commit -m "kebs 3: drop kebs-akka-http (use kebs-pekko-http)"
```

### Task 3: hearth wiring + `ValueClassLike` on hearth

**Files:**
- Modify: `build.sbt` — `coreSettings`: add `libraryDependencies += "com.kubuszok" %%% "hearth" % hearthV` (`val hearthV = "0.4.5"`), on Scala 3 `compilerPlugin("com.kubuszok" % "hearth-cross-quotes_3" % hearthV)`, `scalaTest.value % "test"`; `baseSettings` on 2.13: add `"-Xsource:3"` and `"-Wconf:cat=scala3-migration:s"`
- Create: `core/src/main/scala/pl/iterators/kebs/core/macros/ValueClassLike.scala`
- Create: `core/src/main/scala/pl/iterators/kebs/core/macros/internal/ValueClassLikeMacrosImpl.scala`
- Create: `core/src/main/scala-2/pl/iterators/kebs/core/macros/CaseClass1ToValueClassCompat.scala`
- Create: `core/src/main/scala-3/pl/iterators/kebs/core/macros/CaseClass1ToValueClassCompat.scala`
- Delete: `core/src/main/scala-2/pl/iterators/kebs/core/macros/ValueClassReps.scala`, `core/src/main/scala-3/pl/iterators/kebs/core/macros/ValueClassReps.scala`
- Keep: `core/src/main/scala-2/pl/iterators/kebs/core/macros/MacroUtils.scala` (still used by the Scala 2 circe/spray/enumeratum/jsonschema macros until their plans)
- Test: `core/src/test/scala/pl/iterators/kebs/core/ValueClassLikeTests.scala`

**Interfaces:**
- Produces (public, unchanged names): `final class ValueClassLike[VC, F1](val apply: F1 => VC, val unapply: VC => F1)`; `trait CaseClass1ToValueClass { implicit def repFromCaseClass[VC <: Product, F1]: ValueClassLike[VC, F1] }` (Scala 2 signature) / `inline implicit def repFromCaseClass[T <: Product, F1](using m: Mirror.ProductOf[T], teq: m.MirroredElemTypes =:= F1 *: EmptyTuple.type): ValueClassLike[T, F1]` (Scala 3); `object CaseClass1ToValueClass extends CaseClass1ToValueClass`.
- Produces (internal, reused by later plans as the pattern): `private[kebs] trait ValueClassLikeMacrosImpl { this: hearth.MacroCommons => def deriveValueClassLike[T: Type]: Expr_?? }` returning an existential `Expr[ValueClassLike[T, F]]`.

- [ ] **Step 1: Write the failing tests**

```scala
package pl.iterators.kebs.core

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.macros.{CaseClass1ToValueClass, ValueClassLike}

object ValueClassLikeTestDomain {
  final case class Plain(value: String)
  final case class Box[A](a: A)
  final case class Positive(value: Int)
  object Positive { def apply(value: Int): Positive = { require(value > 0, "must be positive"); new Positive(value) } }
  final case class Email(value: String)
  object Email { def apply(value: String): Email = new Email(value.toLowerCase) }
  final case class Two(a: Int, b: Int)
  final class NotCase(val value: Int)
}

class ValueClassLikeTests extends AnyFunSuite with Matchers with CaseClass1ToValueClass {
  import ValueClassLikeTestDomain._

  test("derives apply/unapply for a single-field case class") {
    val rep = implicitly[ValueClassLike[Plain, String]]
    rep.apply("a") shouldBe Plain("a")
    rep.unapply(Plain("b")) shouldBe "b"
  }
  test("infers the field type through an outer implicit") {
    def find[T, A](implicit rep: ValueClassLike[T, A]): ValueClassLike[T, A] = rep
    find[Plain, String].unapply(Plain("c")) shouldBe "c"
  }
  test("generic case class") {
    implicitly[ValueClassLike[Box[Int], Int]].apply(1) shouldBe Box(1)
  }
  test("uses the companion apply (validation)") {
    val rep = implicitly[ValueClassLike[Positive, Int]]
    an[IllegalArgumentException] should be thrownBy rep.apply(-1)
  }
  test("uses the companion apply (normalisation)") {
    implicitly[ValueClassLike[Email, String]].apply("A@B.PL").value shouldBe "a@b.pl"
  }
  test("no instance for a 2-field case class or a non-case class") {
    assertDoesNotCompile("implicitly[ValueClassLike[Two, Int]]")
    assertDoesNotCompile("implicitly[ValueClassLike[NotCase, Int]]")
  }
}
```

- [ ] **Step 2: Run them to see the expected failure**

Run: `sbt "++3.9.0" core/testOnly pl.iterators.kebs.core.ValueClassLikeTests`
Expected: FAIL — "uses the companion apply (validation)" and "(normalisation)" fail on Scala 3 (today's `Mirror.fromProduct` bypasses `apply`). (On 2.13 they already pass.)

- [ ] **Step 3: Wire hearth into the build** (the `build.sbt` changes listed under Files)

- [ ] **Step 4: Implement the shared macro and both entry points**

`ValueClassLikeMacrosImpl.deriveValueClassLike[T]` (see design §7.1 for the shape proven in the spike):
1. `CaseClass.parse[T]`; not a case class → `Environment.reportErrorAndAbort` naming the type.
2. Exactly one parameter across the primary constructor's parameter lists, else abort.
3. Bind the field type without a nested path (`val fieldType: ?? = field.tpe; import fieldType.Underlying as F` — `import field.tpe.Underlying as F` breaks cross-quotes on 2.13).
4. Construct via the companion's `apply` taking exactly one parameter of type `F` (`Type[T].unsortedMethodsNamed("apply")`, then `Method.ApplyValues#apply(Map(name -> arg.as_??))` / `build()`), applying `T`'s type arguments for generic case classes; fall back to `CaseClass.construct` only when no such `apply` is callable at the call site.
5. Read the field with `caseClass.caseFieldValuesAt(t)`.
6. Return `Expr.quote { new ValueClassLike[T, F]((f: F) => …, (t: T) => …) }.as_??`.

Scala 2 entry point: `implicit def repFromCaseClass[VC <: Product, F1]: ValueClassLike[VC, F1] = macro ValueClassRepMacros.materializeValueClassRep[VC, F1]` with `class ValueClassRepMacros(val c: whitebox.Context) extends hearth.MacroCommonsScala2 with ValueClassLikeMacrosImpl` (**whitebox** is required so `F1` is inferred).
Scala 3 entry point: the `inline implicit def` from Interfaces, body `${ ValueClassLikeMacros.materialize[T, F1] }` with `class ValueClassLikeMacros(q: Quotes) extends hearth.MacroCommonsScala3(using q), ValueClassLikeMacrosImpl`. The `Mirror` evidence stays: it drives inference of `F1` and keeps non-matching types from reaching the macro.

- [ ] **Step 5: Run the new tests and the whole build on both versions**

Run: `sbt core/testOnly pl.iterators.kebs.core.ValueClassLikeTests "++3.9.0" core/testOnly pl.iterators.kebs.core.ValueClassLikeTests` then `sbt +test`
Expected: PASS everywhere.

- [ ] **Step 6: Run core on JS and Native**

Run: `sbt +coreJS/test +coreNative/test`
Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add build.sbt core
git commit -m "kebs 3: ValueClassLike on hearth (companion apply on both Scala versions)"
```

### Task 4: `kebs-testkit` + circe golden tests

**Files:**
- Modify: `build.sbt` — new `lazy val testkit = crossProject(JSPlatform, NativePlatform, JVMPlatform).withoutSuffixFor(JVMPlatform).crossType(CrossType.Pure).in(file("testkit")).dependsOn(core).settings(commonSettings *).settings(noPublishSettings *).settings(name := "testkit", moduleName := "kebs-testkit", libraryDependencies += enumeratum.value)` with `crossScalaVersions := supportedScalaVersions`; add `testkit` to the root `aggregate`; `circeSupport` gets `.dependsOn(testkit % "test->compile")`
- Create: `testkit/src/main/scala/pl/iterators/kebs/testkit/model.scala`
- Create: `circe/src/test/scala/pl/iterators/kebs/circe/golden/CirceGoldenTests.scala`
- Create: `circe/src/test/scala-2/pl/iterators/kebs/circe/golden/CirceAdtGoldenTests.scala`

**Interfaces:**
- Produces (`pl.iterators.kebs.testkit.model`, used by Tasks 5–6 and later plans):
  - `final case class Id(value: String)`
  - `final case class Person(id: Id, name: String, age: Int, nickname: Option[String])`
  - `final case class Naming(someField: Int, anotherFieldName: String, field1Name: Int, httpURL: String)`
  - `final case class Event(id: Id, at: java.time.Instant)`
  - `final case class Wide(f1: Int, …, f22: Int, f23: Option[String])` (23 fields `f1`..`f22: Int`, `f23: Option[String]`)
  - `sealed abstract class Color extends enumeratum.EnumEntry; object Color extends enumeratum.Enum[Color] { case object Red; case object DarkBlue; val values = findValues }`
  - `sealed abstract class Level(val value: Int) extends enumeratum.values.IntEnumEntry with pl.iterators.kebs.core.enums.ValueEnumLikeEntry[Int]; object Level extends enumeratum.values.IntEnum[Level] { case object Low extends Level(1); case object High extends Level(2); val values = findValues }`
  - `final case class Paint(color: Color, level: Level)`
  - `sealed trait Shape; final case class Circle(r: Double, label: String) extends Shape; case object Empty extends Shape`

- [ ] **Step 1: Write the golden tests** (expected JSON parsed and compared as `Json` values; decode must round-trip)

Protocols: `object Default extends KebsCirce`, `object Flat extends KebsCirce with CaseClass1ToValueClass`, `pl.iterators.kebs.circe.snakified`, `pl.iterators.kebs.circe.capitalized`; enums via `KebsEnumeratum`/`KebsValueEnumeratum` with `pl.iterators.kebs.circe.enums._`, plus `enums.uppercase` and `enums.lowercase`; `Instant` via `pl.iterators.kebs.instances.KebsInstances`.

| Test name | Protocol | Value | Expected JSON |
|---|---|---|---|
| `person default` | Default | `Person(Id("x"), "Ann", 30, None)` | `{"id":{"value":"x"},"name":"Ann","age":30,"nickname":null}` |
| `person flat` | Flat | `Person(Id("x"), "Ann", 30, Some("A"))` | `{"id":"x","name":"Ann","age":30,"nickname":"A"}` |
| `naming snakified` | snakified | `Naming(1, "a", 2, "u")` | `{"some_field":1,"another_field_name":"a","field1_name":2,"http_url":"u"}` |
| `naming capitalized` | capitalized | `Naming(1, "a", 2, "u")` | `{"SomeField":1,"AnotherFieldName":"a","Field1Name":2,"HttpURL":"u"}` |
| `enums` | Flat + enums | `Paint(Color.DarkBlue, Level.High)` | `{"color":"DarkBlue","level":2}` |
| `enums uppercase` | Flat + enums.uppercase | `Paint(Color.DarkBlue, Level.High)` | `{"color":"DARKBLUE","level":2}` |
| `enums lowercase` | Flat + enums.lowercase | `Paint(Color.DarkBlue, Level.High)` | `{"color":"darkblue","level":2}` |
| `instance converter` | Flat + KebsInstances | `Event(Id("e"), Instant.parse("2026-01-02T03:04:05Z"))` | `{"id":"e","at":"2026-01-02T03:04:05Z"}` |
| `wide (>22 fields)` | Default | `Wide(1, 2, …, 22, None)` | `{"f1":1,…,"f22":22,"f23":null}` |

Also assert case-insensitive decoding of the default enum decoder: decoding `{"color":"darkblue","level":2}` with the default protocol gives `Paint(Color.DarkBlue, Level.High)`.

`CirceAdtGoldenTests` (Scala 2 only — Scala 3 kebs 2 does not derive sealed traits; the circe plan moves it to shared), using the `Default` protocol (with `Flat`, single-field children would be flat):
`Circle(1.0, "c"): Shape` → `{"Circle":{"r":1.0,"label":"c"}}`; `Empty: Shape` → `{"Empty":{}}`.

- [ ] **Step 2: Run them against the current implementation**

Run: `sbt +circeSupport/testOnly "pl.iterators.kebs.circe.golden.*"`
Expected: PASS on both versions. Any mismatch → stop and report it with the actual output (see Global Constraints).

- [ ] **Step 3: Commit**

```bash
git add build.sbt testkit circe/src/test
git commit -m "kebs 3: kebs-testkit module and circe golden tests (2.x wire format)"
```

### Task 5: spray-json golden tests

**Files:**
- Modify: `build.sbt` — `sprayJsonSupport` gets `.dependsOn(testkit.jvm % "test->compile")`
- Create: `spray-json/src/test/scala/pl/iterators/kebs/sprayjson/golden/SprayGoldenTests.scala`

**Interfaces:**
- Consumes: `pl.iterators.kebs.testkit.model._` (Task 4).

- [ ] **Step 1: Write the golden tests** (expected parsed with `JsonParser`, compared as `JsValue`; read must round-trip)

Protocols: `object Default extends DefaultJsonProtocol with KebsSprayJson`, `object Flat extends DefaultJsonProtocol with KebsSprayJson with CaseClass1ToValueClass`, `object FlatNulls extends DefaultJsonProtocol with KebsSprayJson with CaseClass1ToValueClass with NullOptions`, `pl.iterators.kebs.sprayjson.snakified`, `pl.iterators.kebs.sprayjson.capitalized`; enums via `pl.iterators.kebs.sprayjson.enums._` (+ `uppercase`, `lowercase`); `KebsInstances` for `Instant`.

| Test name | Protocol | Value | Expected JSON |
|---|---|---|---|
| `person default` | Default | `Person(Id("x"), "Ann", 30, None)` | `{"id":{"value":"x"},"name":"Ann","age":30}` |
| `person flat` | Flat | `Person(Id("x"), "Ann", 30, Some("A"))` | `{"id":"x","name":"Ann","age":30,"nickname":"A"}` |
| `person NullOptions` | FlatNulls | `Person(Id("x"), "Ann", 30, None)` | `{"id":"x","name":"Ann","age":30,"nickname":null}` |
| `naming snakified` | snakified | `Naming(1, "a", 2, "u")` | `{"some_field":1,"another_field_name":"a","field1_name":2,"http_url":"u"}` |
| `naming capitalized` | capitalized | `Naming(1, "a", 2, "u")` | `{"SomeField":1,"AnotherFieldName":"a","Field1Name":2,"HttpURL":"u"}` |
| `enums` / `enums uppercase` / `enums lowercase` | Flat + enums variants | `Paint(Color.DarkBlue, Level.High)` | `{"color":"DarkBlue","level":2}` / `"DARKBLUE"` / `"darkblue"` |
| `instance converter` | Flat + KebsInstances | `Event(Id("e"), Instant.parse("2026-01-02T03:04:05Z"))` | `{"id":"e","at":"2026-01-02T03:04:05Z"}` |
| `wide (>22 fields)` | Default | `Wide(1, …, 22, None)` | `{"f1":1,…,"f22":22}` |
| `wide NullOptions` | FlatNulls | `Wide(1, …, 22, None)` | `{"f1":1,…,"f22":22,"f23":null}` |

- [ ] **Step 2: Run them against the current implementation**

Run: `sbt +sprayJsonSupport/testOnly "pl.iterators.kebs.sprayjson.golden.*"`
Expected: PASS on both versions; any mismatch → stop and report.

- [ ] **Step 3: Commit**

```bash
git add build.sbt spray-json/src/test
git commit -m "kebs 3: spray-json golden tests (2.x wire format)"
```

### Task 6: play-json golden tests

**Files:**
- Modify: `build.sbt` — `playJsonSupport` gets `.dependsOn(testkit % "test->compile")`
- Create: `play-json/src/test/scala/pl/iterators/kebs/playjson/golden/PlayJsonGoldenTests.scala`

**Interfaces:**
- Consumes: `pl.iterators.kebs.testkit.model._` (Task 4).

- [ ] **Step 1: Write the golden tests** — kebs 2 has no case-class derivation for play-json, so the goldens pin what kebs does provide, with user-written `Json.format` for case classes (this is also the coexistence the play-json plan must preserve):

Protocol: `object Flat extends KebsPlayJson with CaseClass1ToValueClass`, `pl.iterators.kebs.playjson.enums._` (+ `uppercase`, `lowercase`), `KebsInstances`; in the test: `implicit val personFormat: OFormat[Person] = Json.format[Person]`, same for `Paint` and `Event`.

| Test name | Value | Expected JSON |
|---|---|---|
| `flat value class` | `Id("x")` | `"x"` |
| `user Json.format with kebs field formats` | `Person(Id("x"), "Ann", 30, None)` | `{"id":"x","name":"Ann","age":30}` |
| `enums` / `uppercase` / `lowercase` | `Paint(Color.DarkBlue, Level.High)` | `{"color":"DarkBlue","level":2}` / `"DARKBLUE"` / `"darkblue"` |
| `instance converter` | `Event(Id("e"), Instant.parse("2026-01-02T03:04:05Z"))` | `{"id":"e","at":"2026-01-02T03:04:05Z"}` |

- [ ] **Step 2: Run them against the current implementation (JVM and JS)**

Run: `sbt +playJsonSupport/testOnly "pl.iterators.kebs.playjson.golden.*" +playJsonSupportJS/testOnly "pl.iterators.kebs.playjson.golden.*"`
Expected: PASS; any mismatch → stop and report.

- [ ] **Step 3: Commit**

```bash
git add build.sbt play-json/src/test
git commit -m "kebs 3: play-json golden tests (2.x wire format)"
```

### Task 7: Open the PR

- [ ] **Step 1: Full verification**

Run: `sbt +test +coreJS/test +coreNative/test docs/mdoc`
Expected: all green.

- [ ] **Step 2: Push and open the PR (maintainer go-ahead; `workflow` scope needed for `ci.yml`)**

```bash
git push -u origin kebs-3/build-core
gh pr create --base master --title "kebs 3: Scala 3.9, ValueClassLike on hearth, testkit + golden tests" --body-file <(…summary of Tasks 1–6, link to design/2026-11-21-kebs-3-design.md…)
```
Expected: CI green on JDK 17 and 21.

---

## Notes for later plans (not tasks here)

- Enum-string goldens for the non-JSON modules (doobie, slick, baklava, pekko-http, http4s, http4s-stir) and the H2 value round-trips for doobie/slick are written at the start of the naming plan, before its enum-codec refactor (same "capture before change" rule).
- `MacroUtils` (Scala 2) is deleted once circe, spray-json and enumeratum no longer use it; jsonschema then gets its own copy of the two helpers it needs.

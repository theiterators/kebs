import sbt.librarymanagement.ConflictWarning

val scala_2_13             = "2.13.14"
val scala_3                = "3.3.3"
val mainScalaVersion       = scala_3
val supportedScalaVersions = Seq(scala_2_13, scala_3)

ThisBuild / crossScalaVersions := supportedScalaVersions
ThisBuild / scalaVersion       := mainScalaVersion
ThisBuild / conflictWarning    := ConflictWarning.disable
Test / scalafmtOnCompile       := true
ThisBuild / scalafmtOnCompile  := true

lazy val baseSettings = Seq(
  organization         := "pl.iterators",
  organizationName     := "Iterators",
  organizationHomepage := Some(url("https://iterato.rs")),
  homepage             := Some(url("https://github.com/theiterators/kebs")),
  scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature", "-encoding", "utf8") ++ (if (scalaVersion.value.startsWith("3"))
                                                                                             Seq("-Xmax-inlines", "64", "-Yretain-trees")
                                                                                           else Seq.empty)
)

lazy val commonMacroSettings = baseSettings ++ Seq(
  libraryDependencies ++= (if (scalaVersion.value.startsWith("3")) Nil
                           else
                             Seq(
                               "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided",
                               "org.scala-lang" % "scala-reflect"  % scalaVersion.value
                             ))
)

lazy val metaSettings = commonSettings ++ Seq(
  scalacOptions ++= paradiseFlag(scalaVersion.value)
)

lazy val crossBuildSettings = Seq(crossScalaVersions := supportedScalaVersions)

lazy val publishSettings = Seq(
  pomIncludeRepository := const(true),
  licenses             := Seq("MIT License" -> url("http://opensource.org/licenses/MIT")),
  developers := List(
    Developer(id = "luksow", name = "Łukasz Sowa", email = "lsowa@iteratorshq.com", url = url("https://github.com/luksow")),
    Developer(
      id = "pkiersznowski",
      name = "Paweł Kiersznowski",
      email = "pkiersznowski@iteratorshq.com",
      url = url("https://github.com/pk044")
    )
  ),
  scmInfo := Some(
    ScmInfo(browseUrl = url("https://github.com/theiterators/kebs"), connection = "scm:git:https://github.com/theiterators/kebs.git")
  )
) ++ crossBuildSettings

lazy val noPublishSettings =
  Seq(
    publishArtifact := false
  )

def disableScala(v: List[String]) =
  Def.settings(
    libraryDependencies := {
      if (v.contains(scalaBinaryVersion.value)) {
        Nil
      } else {
        libraryDependencies.value
      }
    },
    Seq(Compile, Test).map { x =>
      (x / sources) := {
        if (v.contains(scalaBinaryVersion.value)) {
          Nil
        } else {
          (x / sources).value
        }
      }
    },
    Test / test := {
      if (v.contains(scalaBinaryVersion.value)) {
        ()
      } else {
        (Test / test).value
      }
    },
    publish / skip := (v.contains(scalaBinaryVersion.value))
  )

def optional(dependency: ModuleID) = dependency % "provided"

def paradiseFlag(scalaVersion: String): Seq[String] =
  if (scalaVersion == scala_3)
    Seq.empty
  else
    Seq("-Ymacro-annotations")

val scalaTest       = Def.setting("org.scalatest" %%% "scalatest" % "3.2.17")
val scalaCheck      = Def.setting("org.scalacheck" %%% "scalacheck" % "1.17.0")
val slick           = "com.typesafe.slick"  %% "slick"                % "3.5.1"
val optionalSlick   = optional(slick)
val playJson        = Def.setting("org.playframework"   %%% "play-json"            % "3.0.4")
val slickPg         = "com.github.tminglei" %% "slick-pg"             % "0.22.2"
val doobie          = "org.tpolecat"        %% "doobie-core"          % "1.0.0-RC5"
val doobiePg        = "org.tpolecat"        %% "doobie-postgres"      % "1.0.0-RC5"
val sprayJson       = "io.spray"            %% "spray-json"           % "1.3.6"
val circeV          = "0.14.10"
val circe           = Def.setting("io.circe"            %%% "circe-core"           % circeV)
val circeAuto       = Def.setting("io.circe"            %%% "circe-generic"        % circeV)
val circeAutoExtras = Def.setting("io.circe"            %%% "circe-generic-extras" % "0.14.4")
val circeParser     = Def.setting("io.circe"            %%% "circe-parser"         % circeV)

val jsonschema = "com.github.andyglow" %% "scala-jsonschema" % "0.7.11"

val scalacheck = "org.scalacheck" %% "scalacheck" % "1.18.0"

val scalacheckMagnolify  = "com.spotify"         % "magnolify-scalacheck"  % "0.7.3"
val scalacheckDerived    = "io.github.martinhh" %% "scalacheck-derived"    % "0.4.2"

val enumeratumVersion         = "1.7.4"
val enumeratumPlayJsonVersion = "1.8.1"
val enumeratum                = Def.setting("com.beachape" %%% "enumeratum" % enumeratumVersion)
def enumeratumInExamples = {
  val playJsonSupport = "com.beachape" %% "enumeratum-play-json" % enumeratumPlayJsonVersion
  Seq("com.beachape" %% "enumeratum" % enumeratumVersion, playJsonSupport)
}
val optionalEnumeratum = Def.setting("com.beachape" %%% "enumeratum" % enumeratumVersion % "provided")
val enumeratumInTest = Def.setting("com.beachape" %%% "enumeratum" % enumeratumVersion % "test")

val akkaVersion       = "2.6.20"
val akkaHttpVersion   = "10.2.10"
val akkaStream        = "com.typesafe.akka" %% "akka-stream"         % akkaVersion
val akkaStreamTestkit = "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion
val akkaHttp          = "com.typesafe.akka" %% "akka-http"           % akkaHttpVersion
val akkaHttpTestkit   = "com.typesafe.akka" %% "akka-http-testkit"   % akkaHttpVersion
def akkaHttpInExamples = {
  val akkaHttpSprayJson = "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion
  Seq(
    akkaStream.cross(CrossVersion.for3Use2_13),
    akkaHttp.cross(CrossVersion.for3Use2_13),
    akkaHttpSprayJson.cross(CrossVersion.for3Use2_13)
  )
}

val pekkoVersion       = "1.0.3"
val pekkoHttpVersion   = "1.0.1"
val pekkoHttpJsonV     = "2.0.0"
val pekkoStream        = "org.apache.pekko" %% "pekko-stream"         % pekkoVersion
val pekkoStreamTestkit = "org.apache.pekko" %% "pekko-stream-testkit" % pekkoVersion
val pekkoHttp          = "org.apache.pekko" %% "pekko-http"           % pekkoHttpVersion
val pekkoHttpTestkit   = "org.apache.pekko" %% "pekko-http-testkit"   % pekkoHttpVersion

def pekkoHttpInExamples = {
  val pekkoHttpSprayJson = "org.apache.pekko" %% "pekko-http-spray-json" % pekkoHttpVersion
  Seq(pekkoStream, pekkoHttp, pekkoHttpSprayJson)
}

val http4sVersion = "0.23.27"
val http4s        = "org.http4s" %% "http4s-dsl" % http4sVersion

val http4sStirVersion = "0.3"
val http4sStir        = "pl.iterators" %% "http4s-stir"         % http4sStirVersion
val http4sStirTestkit = "pl.iterators" %% "http4s-stir-testkit" % http4sStirVersion

val pureConfigVersion = "0.17.7"
val pureConfig        = "com.github.pureconfig" %% "pureconfig-core" % pureConfigVersion
val pureConfigGeneric = "com.github.pureconfig" %% "pureconfig-generic" % pureConfigVersion
val pureConfigGenericScala3 = "com.github.pureconfig" %% "pureconfig-generic-scala3" % pureConfigVersion

val scalaJavaTime = Def.setting("io.github.cquiroz" %%% "scala-java-time" % "2.5.0")

lazy val commonSettings = baseSettings ++ Seq(
  scalacOptions ++=
    (if (scalaVersion.value.startsWith("3"))
       Seq("-language:implicitConversions", "-Ykind-projector", "-Xignore-scala2-macros")
     else Seq("-language:implicitConversions", "-language:experimental.macros")),
//  (scalacOptions in Test) ++= Seq("-Ymacro-debug-lite" /*, "-Xlog-implicits"*/ ),
  libraryDependencies += scalaTest.value % "test"
)

lazy val slickSettings = commonSettings ++ Seq(
  libraryDependencies += slick,
  libraryDependencies += (slickPg    % "test"),
  libraryDependencies += (enumeratumInTest.value)
)

lazy val doobieSettings = commonSettings ++ Seq(
  libraryDependencies += doobie,
  libraryDependencies += (doobiePg   % "test"),
  libraryDependencies += (enumeratumInTest.value)
)

lazy val coreSettings = commonMacroSettings ++ Seq(
  libraryDependencies += (scalaCheck.value % "test").cross(CrossVersion.for3Use2_13)
)

lazy val enumSettings = commonMacroSettings ++ Seq(
  libraryDependencies += scalaCheck.value % "test",
  libraryDependencies += scalaTest.value,
  scalacOptions ++= paradiseFlag(scalaVersion.value)
)

lazy val enumeratumSettings = commonMacroSettings ++ Seq(
  libraryDependencies += scalaCheck.value % "test",
  libraryDependencies += scalaTest.value,
  libraryDependencies += optionalEnumeratum.value,
  scalacOptions ++= paradiseFlag(scalaVersion.value)
)

lazy val sprayJsonSettings = commonSettings ++ Seq(
  libraryDependencies += sprayJson.cross(CrossVersion.for3Use2_13),
  libraryDependencies += optionalEnumeratum.value
)

lazy val playJsonSettings = commonSettings ++ Seq(
  libraryDependencies += playJson.value,
  libraryDependencies += (enumeratumInTest.value)
)

lazy val circeSettings = commonSettings ++ Seq(
  libraryDependencies += circe.value,
  libraryDependencies += circeAuto.value,
  libraryDependencies += optionalEnumeratum.value,
  libraryDependencies += (circeParser.value % "test")
) ++ Seq(
  libraryDependencies ++= (if (scalaVersion.value.startsWith("3")) Nil
                           else Seq(circeAutoExtras.value))
)

lazy val akkaHttpSettings = commonSettings ++ Seq(
  libraryDependencies += (akkaHttp).cross(CrossVersion.for3Use2_13),
  libraryDependencies += (akkaStreamTestkit % "test").cross(CrossVersion.for3Use2_13),
  libraryDependencies += (akkaHttpTestkit   % "test").cross(CrossVersion.for3Use2_13),
  libraryDependencies += optionalEnumeratum.value,
  scalacOptions ++= paradiseFlag(scalaVersion.value)
)

lazy val pekkoHttpSettings = commonSettings ++ Seq(
  libraryDependencies += pekkoHttp,
  libraryDependencies += pekkoStream,
  libraryDependencies += pekkoStreamTestkit % "test",
  libraryDependencies += pekkoHttpTestkit   % "test",
  libraryDependencies += enumeratumInTest.value,
  scalacOptions ++= paradiseFlag(scalaVersion.value)
)

lazy val http4sSettings = commonSettings ++ Seq(
  libraryDependencies += http4s,
  scalacOptions ++= paradiseFlag(scalaVersion.value)
)

lazy val http4sStirSettings = commonSettings ++ Seq(
  libraryDependencies += http4s,
  libraryDependencies += http4sStir,
  libraryDependencies += http4sStirTestkit % "test",
  libraryDependencies += enumeratumInTest.value,
  scalacOptions ++= paradiseFlag(scalaVersion.value)
)

lazy val jsonschemaSettings = commonSettings ++ Seq(
  libraryDependencies += jsonschema.cross(CrossVersion.for3Use2_13)
)

lazy val scalacheckSettings = commonSettings ++ Seq(
  libraryDependencies += scalacheck,
  libraryDependencies += (enumeratumInTest.value),
) ++ Seq(
  libraryDependencies ++= (if (scalaVersion.value.startsWith("3")) Seq(scalacheckDerived)
                           else Nil)
) ++ Seq(
  libraryDependencies ++= (if (scalaVersion.value.startsWith("2")) Seq(scalacheckMagnolify.cross(CrossVersion.for3Use2_13)) else Nil)
)

lazy val taggedSettings = commonSettings ++ Seq(
  libraryDependencies += optionalSlick,
  libraryDependencies += optional(circe.value)
)

lazy val opaqueSettings = commonSettings

lazy val examplesSettings = commonSettings ++ Seq(
  libraryDependencies += slickPg,
  libraryDependencies += circeParser.value,
  libraryDependencies ++= enumeratumInExamples,
  libraryDependencies ++= pekkoHttpInExamples,
  scalacOptions ++= paradiseFlag(scalaVersion.value)
)

lazy val taggedMetaSettings = metaSettings ++ Seq(
  libraryDependencies += optional(sprayJson.cross(CrossVersion.for3Use2_13)),
  libraryDependencies += optional(circe.value)
)

lazy val instancesSettings = commonSettings

lazy val pureConfigSettings = commonSettings ++ Seq(
  libraryDependencies += pureConfig,
  libraryDependencies += (if (scalaVersion.value.startsWith("3")) pureConfigGenericScala3 else pureConfigGeneric) % "test",
)

lazy val core = crossProject(JSPlatform, JVMPlatform)
  .withoutSuffixFor(JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("core"))
  .settings(coreSettings *)
  .settings(publishSettings *)
  .settings(
    name        := "core",
    description := "Macros and utils supporting Kebs library",
    moduleName  := "kebs-core"
  )

lazy val slickSupport = project
  .in(file("slick"))
  .dependsOn(core.jvm, enumeratumSupport.jvm, instances.jvm % "test -> test")
  .settings(slickSettings *)
  .settings(publishSettings *)
  .settings(
    name               := "slick",
    description        := "Library to eliminate the boilerplate code that comes with the use of Slick",
    moduleName         := "kebs-slick",
    crossScalaVersions := supportedScalaVersions
  )

lazy val doobieSupport = project
  .in(file("doobie"))
  .dependsOn(instances.jvm, enumeratumSupport.jvm, enumSupport.jvm, opaque.jvm % "test -> test")
  .settings(doobieSettings *)
  .settings(publishSettings *)
  .settings(
    name               := "doobie",
    description        := "Library to eliminate the boilerplate code that comes with the use of Doobie",
    moduleName         := "kebs-doobie",
    crossScalaVersions := supportedScalaVersions
  )

lazy val sprayJsonSupport = project
  .in(file("spray-json"))
  .dependsOn(enumeratumSupport.jvm, instances.jvm % "test -> test")
  .settings(sprayJsonSettings *)
  .settings(publishSettings *)
  .settings(disableScala(List("3")))
  .settings(
    name               := "spray-json",
    description        := "Automatic generation of Spray json formats for case-classes",
    moduleName         := "kebs-spray-json",
    crossScalaVersions := supportedScalaVersions
  )

lazy val playJsonSupport = crossProject(JSPlatform, JVMPlatform)
  .withoutSuffixFor(JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("play-json"))
  .dependsOn(core, enumeratumSupport, enumSupport, instances % "test -> test")
  .settings(playJsonSettings *)
  .settings(publishSettings *)
  .settings(
    name               := "play-json",
    description        := "Automatic generation of Play json formats for case-classes",
    moduleName         := "kebs-play-json",
    crossScalaVersions := supportedScalaVersions
  )

lazy val circeSupport = crossProject(JSPlatform, JVMPlatform)
  .withoutSuffixFor(JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("circe"))
  .dependsOn(core, enumeratumSupport, enumSupport, instances % "test -> test")
  .settings(circeSettings *)
  .settings(crossBuildSettings *)
  .settings(publishSettings *)
  .settings(
    name        := "circe",
    description := "Automatic generation of circe formats for case-classes",
    moduleName  := "kebs-circe"
  )

lazy val akkaHttpSupport = project
  .in(file("akka-http"))
  .dependsOn(core.jvm, enumeratumSupport.jvm, instances.jvm % "test -> test", tagged.jvm % "test -> test", taggedMeta % "test -> test")
  .settings(akkaHttpSettings *)
  .settings(publishSettings *)
  .settings(disableScala(List("3")))
  .settings(
    name               := "akka-http",
    description        := "Automatic generation of akka-http deserializers for 1-element case classes",
    moduleName         := "kebs-akka-http",
    crossScalaVersions := supportedScalaVersions
  )

lazy val pekkoHttpSupport = project
  .in(file("pekko-http"))
  .dependsOn(
    core.jvm,
    enumeratumSupport.jvm,
    enumSupport.jvm,
    instances.jvm  % "test -> test",
    tagged.jvm % "test -> test",
    taggedMeta % "test -> test"
  )
  .settings(pekkoHttpSettings *)
  .settings(publishSettings *)
  .settings(
    name               := "pekko-http",
    description        := "Automatic generation of pekko-http deserializers for 1-element case classes",
    moduleName         := "kebs-pekko-http",
    crossScalaVersions := supportedScalaVersions
  )

lazy val http4sSupport = project
  .in(file("http4s"))
  .dependsOn(core.jvm, instances.jvm, opaque.jvm % "test -> test", tagged.jvm % "test -> test", taggedMeta % "test -> test")
  .settings(http4sSettings *)
  .settings(publishSettings *)
  .settings(
    name               := "http4s",
    description        := "Automatic generation of http4s deserializers for 1-element case classes, opaque and tagged types",
    moduleName         := "kebs-http4s",
    crossScalaVersions := supportedScalaVersions
  )

lazy val http4sStirSupport = project
  .in(file("http4s-stir"))
  .dependsOn(core.jvm, instances.jvm, opaque.jvm % "test -> test", tagged.jvm % "test -> test", taggedMeta % "test -> test")
  .settings(http4sStirSettings *)
  .settings(publishSettings *)
  .settings(
    name               := "http4s-stir",
    description        := "Automatic generation of http4s-stir deserializers for 1-element case classes, opaque and tagged types",
    moduleName         := "kebs-http4s-stir",
    crossScalaVersions := supportedScalaVersions
  )

lazy val jsonschemaSupport = project
  .in(file("jsonschema"))
  .dependsOn(core.jvm)
  .settings(jsonschemaSettings *)
  .settings(publishSettings *)
  .settings(disableScala(List("3")))
  .settings(
    name               := "jsonschema",
    description        := "Automatic generation of JSON Schemas for case classes",
    moduleName         := "kebs-jsonschema",
    crossScalaVersions := supportedScalaVersions
  )

lazy val scalacheckSupport = project
  .in(file("scalacheck"))
  .dependsOn(core.jvm, enumSupport.jvm, opaque.jvm % "test -> test")
  .settings(scalacheckSettings *)
  .settings(publishSettings *)
  .settings(
    name               := "scalacheck",
    description        := "Automatic generation of scalacheck generators for case classes",
    moduleName         := "kebs-scalacheck",
    crossScalaVersions := supportedScalaVersions
  )

lazy val tagged = crossProject(JSPlatform, JVMPlatform)
  .withoutSuffixFor(JVMPlatform)
  .crossType(CrossType.Full)
  .in(file("tagged"))
  .dependsOn(core)
  .settings(taggedSettings *)
  .settings(publishSettings *)
  .settings(disableScala(List("3")))
  .settings(
    name               := "tagged",
    description        := "Representation of tagged types",
    moduleName         := "kebs-tagged",
    crossScalaVersions := supportedScalaVersions
  )

lazy val opaque = crossProject(JSPlatform, JVMPlatform)
  .withoutSuffixFor(JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("opaque"))
  .dependsOn(core)
  .settings(opaqueSettings *)
  .settings(publishSettings *)
  .settings(
    name               := "opaque",
    description        := "Representation of opaque types",
    moduleName         := "kebs-opaque",
    crossScalaVersions := supportedScalaVersions
  )
  .settings(disableScala(List("2.13")))

lazy val taggedMeta = project
  .in(file("tagged-meta"))
  .dependsOn(
    core.jvm,
    tagged.jvm,
    sprayJsonSupport  % "test -> test",
    circeSupport.jvm      % "test -> test",
    jsonschemaSupport % "test -> test",
    scalacheckSupport % "test -> test"
  )
  .settings(taggedMetaSettings *)
  .settings(publishSettings *)
  .settings(disableScala(List("3")))
  .settings(
    name               := "tagged-meta",
    description        := "Representation of tagged types - code generation based on scala-meta",
    moduleName         := "kebs-tagged-meta",
    crossScalaVersions := supportedScalaVersions
  )

lazy val examples = project
  .in(file("examples"))
  .dependsOn(slickSupport, sprayJsonSupport, playJsonSupport.jvm, pekkoHttpSupport, taggedMeta, circeSupport.jvm, instances.jvm)
  .settings(examplesSettings *)
  .settings(noPublishSettings *)
  .settings(disableScala(List("3")))
  .settings(
    name       := "examples",
    moduleName := "kebs-examples"
  )

lazy val instances = crossProject(JSPlatform, JVMPlatform)
  .withoutSuffixFor(JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("instances"))
  .dependsOn(core)
  .settings(instancesSettings *)
  .jsSettings(
    libraryDependencies += scalaJavaTime.value
  )
  .settings(publishSettings *)
  .settings(
    name        := "instances",
    description := "Standard type mappings",
    moduleName  := "kebs-instances"
  )

lazy val enumSupport = crossProject(JSPlatform, JVMPlatform)
  .withoutSuffixFor(JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("enum"))
  .dependsOn(core)
  .settings(enumSettings *)
  .settings(publishSettings *)
  .settings(
    name       := "enum",
    moduleName := "kebs-enum"
  )

lazy val enumeratumSupport = crossProject(JSPlatform, JVMPlatform)
  .withoutSuffixFor(JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("enumeratum"))
  .dependsOn(core)
  .settings(enumeratumSettings *)
  .settings(publishSettings *)
  .settings(
    name       := "enumeratum",
    moduleName := "kebs-enumeratum"
  )

lazy val pureConfigSupport = project
  .in(file("pureconfig"))
  .dependsOn(core.jvm)
  .settings(pureConfigSettings *)
  .settings(publishSettings *)
  .settings(
    name := "pureconfig",
    moduleName := "kebs-pureconfig"
)

lazy val kebs = project
  .in(file("."))
  .aggregate(
    tagged.jvm,
    tagged.js,
    opaque.jvm,
    opaque.js,
    core.jvm,
    core.js,
    slickSupport,
    doobieSupport,
    sprayJsonSupport,
    playJsonSupport.jvm,
    playJsonSupport.js,
    circeSupport.jvm,
    circeSupport.js,
    jsonschemaSupport,
    scalacheckSupport,
    akkaHttpSupport,
    pekkoHttpSupport,
    http4sSupport,
    http4sStirSupport,
    taggedMeta,
    instances.jvm,
    instances.js,
    enumSupport.jvm,
    enumSupport.js,
    enumeratumSupport.jvm,
    enumeratumSupport.js,
    pureConfigSupport
  )
  .settings(baseSettings *)
  .settings(noPublishSettings)
  .settings(
    name        := "kebs",
    description := "Library to eliminate the boilerplate code"
  )

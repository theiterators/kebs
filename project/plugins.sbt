logLevel := Level.Warn

addSbtPlugin("org.scalameta"      % "sbt-scalafmt"           % "2.5.2")
addSbtPlugin("pl.project13.scala" % "sbt-jmh"                % "0.4.7")
addSbtPlugin("com.github.sbt"     % "sbt-ci-release"         % "1.7.0")
addSbtPlugin("org.jmotor.sbt"     % "sbt-dependency-updates" % "1.2.9")

addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject"      % "1.3.2")
addSbtPlugin("org.portable-scala" % "sbt-scala-native-crossproject" % "1.3.2")
addSbtPlugin("org.scala-js"       % "sbt-scalajs"                   % "1.17.0")
addSbtPlugin("org.scala-native"   % "sbt-scala-native"              % "0.5.5")

ThisBuild / organization := "frgomes"
ThisBuild / scmInfo := Some(ScmInfo(url("https://bitbucket.org/mathminds/scalator"), "scm:git@bitbucket.org:mathminds/scalator.git"))

ThisBuild / scalaVersion := "2.12.14"


//----------------------------------------------------------------------------------------------------------------------

//XXX ThisBuild / credentials  += Credentials(Path.userHome / ".ivy2" / ".credentials")
//XXX ThisBuild / publishTo := {
//XXX   val Artifactory = "http://artifactory.mathminds.io/artifactory/"
//XXX   if ((ThisBuild / version).value.toLowerCase.indexOf("snapshot") == -1)
//XXX     Some("mathminds-libs-releases"  at Artifactory + "libs-release-local")
//XXX   else
//XXX     Some("mathminds-libs-snapshots" at Artifactory + "libs-snapshot-local")
//XXX }

//----------------------------------------------------------------------------------------------------------------------

import ProjectSyntax._

val versions = new {
  val acyclic      = "0.1.7"
  val quicklens    = "1.4.11"
  val enumeratum   = "1.5.13"
  val logback      = "1.2.3"
  val utest        = "0.6.5"
  val scalaProps   = "0.5.5"

  val config       = "1.3.3"
  val logging      = "3.9.0"
  val decline      = "0.6.2"
  val json4s       = "3.6.0"
  val eel          = "1.2.4"

  val mssql        = "7.2.1.jre8"
  val adal4j       = "1.6.3"
  val flyway       = "5.2.4"

  val parquet      = "1.10.0"
  
  val azure = new {
    val keyvault  = "1.2.0"
    val datalake = new {
      val store = "2.3.3"
    }
  }
}

//----------------------------------------------------------------------------------------------------------------------

def commonSettings: Seq[Setting[_]] = resolverSettings ++ acyclicSettings ++ doctestSettings

def resolverSettings: Seq[Setting[_]] =
  Seq(
    resolvers ++=
      Seq(
        "Local Maven Repository"  at Path.userHome.asFile.toURI.toURL + ".m2/repository"))

def buildinfoSettings: Seq[Setting[_]] =
  Seq(
    buildInfoPackage := s"build.${organization.value}.${name.value}".replace("-", "."),
    buildInfoKeys    := Seq[BuildInfoKey](organization, version, scalaVersion, sbtVersion))

def acyclicSettings: Seq[Setting[_]] =
  Seq(
    autoCompilerPlugins  := true,
    addCompilerPlugin("com.lihaoyi" %% "acyclic" % versions.acyclic),
    scalacOptions ++= Seq("-feature", "-P:acyclic:force", "-Ypartial-unification"))

def doctestSettings: Seq[Setting[_]] =
  Seq(
    doctestMarkdownEnabled := true,
    doctestTestFramework   := DoctestTestFramework.MicroTest,
    testFrameworks         -= new TestFramework("utest.runner.Framework"),
    testFrameworks         += new TestFramework("test.utest.CustomFramework"))

def forkSettings: Seq[Setting[_]] = forkSettings(false, false)
def forkSettings(forked: Boolean): Seq[Setting[_]] = forkSettings(forked, false)
def forkSettings(forked: Boolean, parallel: Boolean): Seq[Setting[_]] =
  inConfig(Test)(Defaults.testSettings ++
    Seq(
      fork              := forked,
      parallelExecution := parallel))

def disableAssembly: Seq[Setting[_]] =
  Seq(
    assembly / aggregate := false)

//----------------------------------------------------------------------------------------------------------------------

def testDependencies: Seq[Setting[_]] =
  Seq(
    libraryDependencies ++=
      Seq(
        "com.github.scalaprops"      %% "scalaprops"      % versions.scalaProps % testDependencyManagement,
        "com.lihaoyi"                %% "utest"           % versions.utest      % testDependencyManagement))

def configDependencies: Seq[Setting[_]] =
  Seq(
    libraryDependencies ++=
      Seq(
        "com.typesafe"               %  "config"          % versions.config))

def cliDependencies: Seq[Setting[_]] =
  Seq(
    libraryDependencies ++=
      Seq(
        "com.monovore"               %% "decline"         % versions.decline))

def loggingDependencies: Seq[Setting[_]] =
  Seq(
    libraryDependencies ++=
      Seq(
        "com.typesafe.scala-logging" %% "scala-logging"   % versions.logging,
        "ch.qos.logback"             %  "logback-classic" % versions.logback))

def databaseDependencies: Seq[Setting[_]] =
  Seq(
    libraryDependencies ++=
      Seq(
        "com.microsoft.azure"        %  "adal4j"          % versions.adal4j,
        "com.microsoft.sqlserver"    %  "mssql-jdbc"      % versions.mssql,
        "org.flywaydb"               %  "flyway-core"     % versions.flyway))

def azureDependencies: Seq[Setting[_]] =
  Seq(
    libraryDependencies ++=
      Seq(
        "com.microsoft.azure" % "azure-data-lake-store-sdk"     % versions.azure.datalake.store))
        
def exampleDependencies: Seq[Setting[_]] =
  Seq(
    libraryDependencies ++=
      Seq(
        "org.json4s" %% "json4s-native" % versions.json4s,
        "io.eels"    %% "eel-core"      % versions.eel))
        

//----------------------------------------------------------------------------------------------------------------------

lazy val example =
  (project in file("."))
    .aggregate(`example-migrate`)
    .aggregate(`example-core`)
    .aggregate(`example-cli`)
    .aggregate(`example-testbench`)

lazy val `example-migrate` =
  (project in file("example-migrate"))
    .withTestConfiguration
    .settings(commonSettings: _*)
    .settings(cliDependencies: _*)
    .settings(testDependencies: _*)
    .settings(databaseDependencies: _*)
    //plugins
    .disablePlugins(AssemblyPlugin)

lazy val `example-core` =
  (project in file("example-core"))
    .withTestConfiguration
    .settings(commonSettings: _*)
    .settings(loggingDependencies: _*)
    .settings(cliDependencies: _*)
    .settings(databaseDependencies: _*)
    .settings(exampleDependencies: _*)
    //TODO: .settings(azureDependencies: _*)
    .settings(testDependencies: _*)
    .dependsOn(`example-testbench`)
    //plugins
    .disablePlugins(AssemblyPlugin)

lazy val `example-cli` =
  (project in file("example-cli"))
    .withTestConfiguration
    .settings(buildinfoSettings: _*)
    .settings(commonSettings: _*)
    .settings(loggingDependencies: _*)
    .settings(configDependencies: _*)
    .settings(cliDependencies: _*)
    .settings(testDependencies: _*)
    //dependencies
    .dependsOn(`example-core`)
    .dependsOn(`example-migrate`)
    //plugins
    .enablePlugins(BuildInfoPlugin)
    .disablePlugins(AssemblyPlugin)

lazy val `example-testbench` =
  (project in file("example-testbench"))
    .withTestConfiguration
    .settings(commonSettings: _*)
    .settings(loggingDependencies: _*)
    .settings(cliDependencies: _*)
    .settings(testDependencies: _*)
    .settings(databaseDependencies: _*)
    //plugins
    .disablePlugins(AssemblyPlugin)

//----------------------------------------------------------------------------------------------------------------------

//
// see: https://stackoverflow.com/a/32114551/62131
//

lazy val mathFormulaInDoc = taskKey[Unit]("add MathJax script import in doc html to display nice latex formula")

ThisBuild / mathFormulaInDoc := {
  val apiDir = (Compile / doc).value
  val docDir = apiDir    // /"some"/"subfolder"  // in my case, only api/some/folder is parsed
  // will replace this "importTag" by "scriptLine
  val importTag  = "##import MathJax"
  val scriptLine = "<script type=\"text/javascript\" src=\"https://cdn.mathjax.org/mathjax/latest/MathJax.js?config=TeX-AMS-MML_HTMLorMML\"> </script>"
  // find all html file and apply patch
  if(docDir.isDirectory)
    listHtmlFile(docDir).foreach { f =>
      val content = scala.io.Source.fromFile(f).getLines().mkString("\n")
        if(content.contains(importTag)) {
          val writer = new java.io.PrintWriter(f)
          writer.write(content.replace(importTag, scriptLine))
          writer.close()
        }
    }
}

// attach this task to doc task
ThisBuild / mathFormulaInDoc := ((ThisBuild / mathFormulaInDoc) triggeredBy (Compile / doc)).value

// function that find html files recursively
def listHtmlFile(dir: java.io.File): List[java.io.File] = {
  dir.listFiles.toList.flatMap { f =>
    if(f.getName.endsWith(".html")) List(f)
    else if(f.isDirectory)          listHtmlFile(f)
    else                            List[File]()
  }
}

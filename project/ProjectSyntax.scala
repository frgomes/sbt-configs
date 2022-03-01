object ProjectSyntax {
  import sbt._
  import sbt.Keys._
  import Configs._

  val mainDependencyManagement = "compile->compile;test->compile,test;it->compile,test;at->compile,test;ft->compile,test;pt->compile,test;tools->compile,test"
  val testDependencyManagement = "compile;test;it;ft;at;pt;tools"

  implicit class ImplicitProjectSyntax(project: sbt.Project) {
    implicit val withDefaultConfigurations: sbt.Project =
      project
        .configs(Compile, Test, IntegrationTest, AcceptanceTest, FunctionalTest, PerformanceTest, Tools)
        .settings(testSettings: _*)
        .settings(itSettings: _*)
        .settings(atSettings: _*)
        .settings(ftSettings: _*)
        .settings(ptSettings: _*)
        .settings(toolsSettings: _*)

    implicit val withTestConfiguration: sbt.Project =
      project
        .configs(Compile, Test)
        .settings(testSettings: _*)

    implicit val withIntegrationTestConfiguration: sbt.Project =
      project
        .configs(Compile, Test, IntegrationTest)
        .settings(testSettings: _*)
        .settings(itSettings: _*)

    implicit val withAcceptanceTestConfiguration: sbt.Project =
      project
        .configs(Compile, Test, IntegrationTest, AcceptanceTest)
        .settings(testSettings: _*)
        .settings(itSettings: _*)
        .settings(atSettings: _*)

    implicit val withFunctionalTestConfiguration: sbt.Project =
      project
        .configs(Compile, Test, IntegrationTest, AcceptanceTest, FunctionalTest)
        .settings(testSettings: _*)
        .settings(itSettings: _*)
        .settings(atSettings: _*)
        .settings(ftSettings: _*)

    implicit val withPerformanceTestConfiguration: sbt.Project =
      project
        .configs(Compile, Test, IntegrationTest, AcceptanceTest, FunctionalTest, PerformanceTest)
        .settings(testSettings: _*)
        .settings(itSettings: _*)
        .settings(atSettings: _*)
        .settings(ftSettings: _*)
        .settings(ptSettings: _*)
  }

  def additionalTestFrameworks: Seq[Setting[_]] =
    Seq(
      testFrameworks += new TestFramework("scalaprops.ScalapropsFramework"),
      testFrameworks += new TestFramework("utest.runner.Framework",
    ))

  def testSettings: Seq[Setting[_]] =
    inConfig(Test)(Defaults.testSettings ++ additionalTestFrameworks)

  def itSettings: Seq[Setting[_]] =
    inConfig(IntegrationTest)(Defaults.testSettings ++ Defaults.itSettings ++ additionalTestFrameworks ++
      Seq(
        unmanagedSourceDirectories   ++= (Test / sourceDirectories  ).value,
        unmanagedResourceDirectories ++= (Test / resourceDirectories).value,
      ))

  def atSettings: Seq[Setting[_]] =
    inConfig(AcceptanceTest)(Defaults.testSettings ++ additionalTestFrameworks ++
      Seq(
        unmanagedSourceDirectories   ++= (Test / sourceDirectories  ).value,
        unmanagedResourceDirectories ++= (Test / resourceDirectories).value,
      ))

  def ftSettings: Seq[Setting[_]] =
    inConfig(FunctionalTest)(Defaults.testSettings ++ additionalTestFrameworks ++
      Seq(
        unmanagedSourceDirectories   ++= (Test / sourceDirectories  ).value,
        unmanagedResourceDirectories ++= (Test / resourceDirectories).value,
      ))

  def ptSettings: Seq[Setting[_]] =
    inConfig(PerformanceTest)(Defaults.testSettings ++ additionalTestFrameworks ++
      Seq(
        unmanagedSourceDirectories   ++= (Test / sourceDirectories  ).value,
        unmanagedResourceDirectories ++= (Test / resourceDirectories).value,
      ))

  def toolsSettings: Seq[Setting[_]] =
    inConfig(Tools)(Defaults.testSettings ++ Classpaths.configSettings ++ additionalTestFrameworks ++
      Seq(
        unmanagedSourceDirectories   ++= (Test / sourceDirectories  ).value,
        unmanagedResourceDirectories ++= (Test / resourceDirectories).value,
      ))

  def inPlaceTests(c: Configuration): Seq[Setting[_]]  = forkSettings(c, false, false)
  def forkedTests(c: Configuration): Seq[Setting[_]]   = forkSettings(c, true, false)
  def parallelTests(c: Configuration): Seq[Setting[_]] = forkSettings(c, true, true)
  def forkSettings(c: Configuration, forked: Boolean, parallel: Boolean): Seq[Setting[_]] =
    inConfig(c)(
      Seq(
        fork              := forked,
        parallelExecution := parallel))
}

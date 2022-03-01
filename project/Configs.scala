import sbt._

object Configs {
  val FunctionalTest  = config("ft")    extend (Test)
  val AcceptanceTest  = config("at")    extend (Test)
  val PerformanceTest = config("pt")    extend (Test)
  val Tools           = config("tools") extend (Test)
}

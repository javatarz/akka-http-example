import Libs._
import sbt.librarymanagement.ModuleID

object Dependencies {
  private val serverCompile = Seq(Akka.http,
    Akka.stream,
    Akka.`spray-json`,
    Common.`scala-logging`,
    Common.`logger-api`,
    Common.logger
  )

  private val serverTest = Seq(Akka.TestOnly.scalatest,
//    Akka.TestOnly.`stream-test-kit`,
    Akka.TestOnly.`http-test-kit`,
    Akka.TestOnly.`test-kit`
  )

  val server: Seq[ModuleID] = serverCompile ++ serverTest
}

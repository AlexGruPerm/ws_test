import zio._
import zio.test._
import zio.random.Random
import Assertion._

import zio._
import zio.test._
import zio.random.Random
import zio.clock.Clock
import Assertion._

object LayerTests extends DefaultRunnableSpec {

  type Names = Has[Names.Service]
  type Teams = Has[Teams.Service]
  type History = Has[History.Service]
  type History2 = Has[History2.Service]

  val firstNames = Vector( "Ed", "Jane", "Joe", "Linda", "Sue", "Tim", "Tom")

  object Names {
    trait Service {
      def randomName: UIO[String]
    }

    case class NamesImpl(random: Random.Service) extends Names.Service {
      println(s"created namesImpl")
      def randomName =
        random.nextIntBounded(firstNames.size).map(firstNames(_))
    }

    val live: ZLayer[Random, Nothing, Names] =
      ZLayer.fromService(NamesImpl)
  }

  object Teams {
    trait Service {
      def pickTeam(size: Int): UIO[Set[String]]
    }

    case class TeamsImpl(names: Names.Service) extends Service {
      def pickTeam(size: Int) =
        ZIO.collectAll(0.until(size).map { _ => names.randomName}).map(_.toSet ) // yeah I know team could have < size!
    }

    val live: ZLayer[Names, Nothing, Teams] =
      ZLayer.fromService(TeamsImpl)

  }

  object History {

    trait Service {
      def wonLastYear(team: Set[String]): Boolean
    }

    case class HistoryImpl(lastYearsWinners: Set[String]) extends Service {
      def wonLastYear(team: Set[String]) = lastYearsWinners == team
    }

    val live: ZLayer[Teams, Nothing, History] = ZLayer.fromServiceM { teams =>
      teams.pickTeam(5).map(nt => HistoryImpl(nt))
    }

  }

  object History2 {

    trait Service {
      def wonLastYear(team: Set[String]): Boolean
    }

    case class History2Impl(lastYearsWinners: Set[String], lastYear: Long) extends Service {
      def wonLastYear(team: Set[String]) = lastYearsWinners == team
    }

    val live: ZLayer[Clock with Teams, Nothing, History2] = ZLayer.fromEffect {
      for {
        someTime <- ZIO.accessM[Clock](_.get.nanoTime)
        team <- teams.pickTeam(5)
      } yield History2Impl(team, someTime)
    }

  }


  def namesTest = testM("names test") {
    for {
      name <- names.randomName
    }  yield {
      assert(firstNames.contains(name))(equalTo(true))
    }
  }

  def justTeamsTest = testM("small team test") {
    for {
      team <- teams.pickTeam(1)
    }  yield {
      assert(team.size)(equalTo(1))
    }
  }

  def inMyTeam = testM("combines names and teams") {
    for {
      name <- names.randomName
      team <- teams.pickTeam(5)
      _ = if (team.contains(name)) println("one of mine")
      else println("not mine")
    } yield assertCompletes
  }


  def wonLastYear = testM("won last year") {
    for {
      team <- teams.pickTeam(5)
      _ <- history.wonLastYear(team)
    } yield assertCompletes
  }

  def wonLastYear2 = testM("won last year") {
    for {
      team <- teams.pickTeam(5)
      _ <- history2.wonLastYear(team)
    } yield assertCompletes
  }


  val individually = suite("individually")(
    suite("needs Names")(
      namesTest
    ).provideCustomLayer(Names.live),
    suite("needs just Team")(
      justTeamsTest
    ).provideCustomLayer(Names.live >>> Teams.live),
    suite("needs Names and Teams")(
      inMyTeam
    ).provideCustomLayer(Names.live ++ (Names.live >>> Teams.live)),
    suite("needs History and Teams")(
      wonLastYear
    ).provideCustomLayerShared((Names.live >>> Teams.live) ++ (Names.live >>> Teams.live >>> History.live)),
    suite("needs History2 and Teams")(
      wonLastYear2
    ).provideCustomLayerShared((Names.live >>> Teams.live) ++ (((Names.live >>> Teams.live) ++ Clock.any) >>> History2.live))
  )

  val altogether = suite("all together")(
    suite("needs Names")(
      namesTest
    ),
    suite("needs just Team")(
      justTeamsTest
    ),
    suite("needs Names and Teams")(
      inMyTeam
    ),
    suite("needs History and Teams")(
      wonLastYear
    ),
  ).provideCustomLayerShared(Names.live ++ (Names.live >>> Teams.live) ++ (Names.live >>> Teams.live >>> History.live))

  override def spec = (
    individually
    )
}

import LayerTests._

package object names {
  def randomName = ZIO.accessM[Names](_.get.randomName)
}

package object teams {
  def pickTeam(nPicks: Int) = ZIO.accessM[Teams](_.get.pickTeam(nPicks))
}

package object history {
  def wonLastYear(team: Set[String]) = ZIO.access[History](_.get.wonLastYear(team))
}

package object history2 {
  def wonLastYear(team: Set[String]) = ZIO.access[History2](_.get.wonLastYear(team))
}
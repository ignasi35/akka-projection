/*
 * Copyright (C) 2020 Lightbend Inc. <https://www.lightbend.com>
 */

package akka.projection.slick

import scala.language.existentials

import akka.projection.TestTags
import akka.projection.slick.SlickOffsetStoreSpec.SlickSpecConfig
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import org.scalatest.Tag
import org.testcontainers.containers.JdbcDatabaseContainer
import org.testcontainers.containers.MSSQLServerContainer
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.containers.OracleContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.startupcheck.IsRunningStartupCheckStrategy

object SlickContainerOffsetStoreSpec {

  abstract class ContainerJdbcSpecConfig extends SlickSpecConfig {

    val tag: Tag = TestTags.ContainerDb
    def container: JdbcDatabaseContainer[_]

    override def config = {
      baseConfig.withFallback(ConfigFactory.parseString(s"""
        akka.projection.slick = {
           db = {
             url = "${container.getJdbcUrl}"
             driver = ${container.getDriverClassName}
             user = ${container.getUsername}
             password = ${container.getPassword}
             connectionPool = disabled
             keepAliveConnection = true
           }
        }
        """))

    }

    protected def initContainer(container: JdbcDatabaseContainer[_]): JdbcDatabaseContainer[_] = {
      container.withStartupCheckStrategy(new IsRunningStartupCheckStrategy)
      container.withStartupAttempts(5)
      container.start()
      container
    }

    override def stopContainer(): Unit =
      container.stop()
  }

  class PostgresSpecConfig extends ContainerJdbcSpecConfig {

    val name = "Postgres Database"
    val container = initContainer(new PostgreSQLContainer)

    override def config: Config =
      super.config.withFallback(ConfigFactory.parseString("""
        akka.projection.slick = {
           profile = "slick.jdbc.PostgresProfile$"
        }
        """))
  }

  class MySQLSpecConfig extends ContainerJdbcSpecConfig {

    val name = "MySQL Database"
    val container = initContainer(new MySQLContainer)

    override def config: Config =
      super.config.withFallback(ConfigFactory.parseString("""
        akka.projection.slick = {
           profile = "slick.jdbc.MySQLProfile$"
        }
        """))
  }
  class MSSQLServerSpecConfig extends ContainerJdbcSpecConfig {

    val name = "MS SQL Server Database"
    override val tag = TestTags.FlakyDb

    val container = initContainer(new MSSQLServerContainer)

    override def config: Config =
      super.config.withFallback(ConfigFactory.parseString("""
        akka.projection.slick = {
           profile = "slick.jdbc.SQLServerProfile$"
        }
        """))
  }
  class OracleSpecConfig extends ContainerJdbcSpecConfig {

    val name = "Oracle Database"
    val container = initContainer(new OracleContainer("oracleinanutshell/oracle-xe-11g"))

    override def config: Config =
      super.config.withFallback(ConfigFactory.parseString("""
        akka.projection.slick = {
           profile = "slick.jdbc.OracleProfile$"
        }
        """))
  }
}

class PostgresSlickOffsetStoreSpec extends SlickOffsetStoreSpec(new SlickContainerOffsetStoreSpec.PostgresSpecConfig)
class MySQLSlickOffsetStoreSpec extends SlickOffsetStoreSpec(new SlickContainerOffsetStoreSpec.MySQLSpecConfig)
class MSSQLServerSlickOffsetStoreSpec
    extends SlickOffsetStoreSpec(new SlickContainerOffsetStoreSpec.MSSQLServerSpecConfig)
class OracleSlickOffsetStoreSpec extends SlickOffsetStoreSpec(new SlickContainerOffsetStoreSpec.OracleSpecConfig)

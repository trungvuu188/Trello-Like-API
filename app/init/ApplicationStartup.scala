package init

import play.api.{Logger, Logging}
import play.api.inject.ApplicationLifecycle

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ApplicationStartup @Inject()(
                                    lifecycle: ApplicationLifecycle,
                                    databaseInitializer: DatabaseInitializer
                                  )(implicit ec: ExecutionContext) {

  // This will run when the application starts
  initialize()

  private def initialize(): Unit = {
    Logger("application").info("Application startup initialization...")

    databaseInitializer.initializeDatabase().recover {
      case ex =>
        Logger("application").error("Failed to initialize database", ex)
        // Don't fail the entire application startup
        ()
    }
  }

}
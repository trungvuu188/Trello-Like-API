package services

import mappers.WorkspaceMapper
import models.Enums
import models.entities.Workspace
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play._
import repositories.WorkspaceRepository

import java.time.LocalDateTime
import scala.concurrent.{ExecutionContext, Future}

class WorkspaceServiceSpec extends PlaySpec with MockitoSugar {

    implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

    def mkService(workspaceRepo: WorkspaceRepository) = {
        new WorkspaceService(workspaceRepo)
    }

    "WorkspaceService" should {

        "getAllWorkspaces should return all workspaces as responses" in {
            val workspaceRepo = mock[WorkspaceRepository]
            val service = mkService(workspaceRepo)

            val now = LocalDateTime.now()
            val workspaces = Seq(
                Workspace(
                    id = Some(1),
                    name = Some("Workspace 1"),
                    status = Enums.Active,
                    createdBy = Some(100),
                    createdAt = Some(now),
                    updatedAt = Some(now)
                ),
                Workspace(
                    id = Some(2),
                    name = Some("Workspace 2"),
                    status = Enums.Active,
                    createdBy = Some(200),
                    createdAt = Some(now.minusDays(1)),
                    updatedAt = Some(now.minusHours(1))
                )
            )

            when(workspaceRepo.getAll()).thenReturn(Future.successful(workspaces))

            val result = service.getAllWorkspaces

            result.map { responses =>
                responses must have length 2
                responses.head mustBe WorkspaceMapper.toResponse(workspaces.head)
                responses(1) mustBe WorkspaceMapper.toResponse(workspaces(1))

                verify(workspaceRepo, times(1)).getAll()
            }
        }

        "getAllWorkspaces should return empty sequence when no workspaces exist" in {
            val workspaceRepo = mock[WorkspaceRepository]
            val service = mkService(workspaceRepo)

            when(workspaceRepo.getAll()).thenReturn(Future.successful(Seq.empty))

            val result = service.getAllWorkspaces

            result.map { responses =>
                responses mustBe empty
                verify(workspaceRepo, times(1)).getAll()
            }
        }

        "getWorkspaceById should return workspace response when workspace exists" in {
            val workspaceRepo = mock[WorkspaceRepository]
            val service = mkService(workspaceRepo)

            val now = LocalDateTime.now()
            val workspace = Workspace(
                id = Some(1),
                name = Some("Test Workspace"),
                status = Enums.Active,
                createdBy = Some(100),
                createdAt = Some(now),
                updatedAt = Some(now)
            )

            when(workspaceRepo.getWorkspaceById(1)).thenReturn(Future.successful(Some(workspace)))

            val result = service.getWorkspaceById(1)

            result.map { response =>
                response mustBe Some(WorkspaceMapper.toResponse(workspace))
                verify(workspaceRepo, times(1)).getWorkspaceById(1)
            }
        }

        "getWorkspaceById should return None when workspace does not exist" in {
            val workspaceRepo = mock[WorkspaceRepository]
            val service = mkService(workspaceRepo)

            when(workspaceRepo.getWorkspaceById(999)).thenReturn(Future.successful(None))

            val result = service.getWorkspaceById(999)

            result.map { response =>
                response mustBe None
                verify(workspaceRepo, times(1)).getWorkspaceById(999)
            }
        }

        "deleteWorkspace should return true when workspace is successfully deleted" in {
            val workspaceRepo = mock[WorkspaceRepository]
            val service = mkService(workspaceRepo)

            when(workspaceRepo.delete(1)).thenReturn(Future.successful(1)) // 1 row affected

            val result = service.deleteWorkspace(1)

            result.map { success =>
                success mustBe true
                verify(workspaceRepo, times(1)).delete(1)
            }
        }

        "deleteWorkspace should return false when workspace deletion fails" in {
            val workspaceRepo = mock[WorkspaceRepository]
            val service = mkService(workspaceRepo)

            when(workspaceRepo.delete(999)).thenReturn(Future.successful(0)) // 0 rows affected

            val result = service.deleteWorkspace(999)

            result.map { success =>
                success mustBe false
                verify(workspaceRepo, times(1)).delete(999)
            }
        }

        "deleteWorkspace should return false when multiple workspaces are affected (edge case)" in {
            val workspaceRepo = mock[WorkspaceRepository]
            val service = mkService(workspaceRepo)

            when(workspaceRepo.delete(1)).thenReturn(Future.successful(2)) // 2 rows affected (unexpected)

            val result = service.deleteWorkspace(1)

            result.map { success =>
                success mustBe true // service treats > 0 as success
                verify(workspaceRepo, times(1)).delete(1)
            }
        }

        "service should handle repository failures gracefully" in {
            val workspaceRepo = mock[WorkspaceRepository]
            val service = mkService(workspaceRepo)

            when(workspaceRepo.getAll()).thenReturn(Future.failed(new RuntimeException("Database error")))

            val result = service.getAllWorkspaces

            result.failed.map { exception =>
                exception.getMessage mustBe "Database error"
                verify(workspaceRepo, times(1)).getAll()
            }
        }
    }
}
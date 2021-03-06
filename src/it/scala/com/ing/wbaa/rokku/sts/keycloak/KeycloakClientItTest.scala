package com.ing.wbaa.rokku.sts.keycloak

import akka.Done
import akka.actor.ActorSystem
import com.ing.wbaa.rokku.sts.config.KeycloakSettings
import com.ing.wbaa.rokku.sts.data.UserName
import com.ing.wbaa.rokku.sts.helper.OAuth2TokenRequest
import org.scalatest.diagrams.Diagrams
import org.scalatest.wordspec.AsyncWordSpec

import scala.concurrent.ExecutionContextExecutor

class KeycloakClientItTest extends AsyncWordSpec with Diagrams with OAuth2TokenRequest with KeycloakClient {

  override implicit val testSystem: ActorSystem = ActorSystem.create("test-system")
  override implicit val exContext: ExecutionContextExecutor = testSystem.dispatcher

  override val keycloakSettings: KeycloakSettings = new KeycloakSettings(testSystem.settings.config) {
    override val realmPublicKeyId: String = "FJ86GcF3jTbNLOco4NvZkUCIUmfYCqoqtOQeMfbhNlE"
    override val issuerForList: Set[String] = Set("sts-rokku")
  }

  "Keycloak client" should {
    val username = "test"
    var createdUserId = KeycloakUserId("")

    "add a user" in {
      insertUserToKeycloak(UserName(username)).map(addedUserId => {
        createdUserId = addedUserId
        assert(addedUserId.id.nonEmpty)
      })
    }

    "thrown error when adding existing user" in {
      recoverToSucceededIf[javax.ws.rs.WebApplicationException](insertUserToKeycloak(UserName(username)))
    }

    "delete the created user" in {
      deleteUserFromKeycloak(createdUserId).map(d => assert(d == Done))
    }
  }
}

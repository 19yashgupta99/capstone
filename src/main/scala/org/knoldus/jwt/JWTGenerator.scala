package org.knoldus.jwt

import akka.http.scaladsl.server.directives.Credentials
import org.knoldus.model.{UserCredentials, UserJsonProtocol}
import pdi.jwt.{JwtAlgorithm, JwtClaim, JwtSprayJson}
import spray.json._

import java.util.concurrent.TimeUnit
import scala.util.Try

object JWTGenerator extends UserJsonProtocol{

  val algorithm = JwtAlgorithm.HS256
  val secretKey = "YashGupta'sSecretKey"

  def createToken(role: String, expirationPeriodInMinutes: Int): String = {
    val claims = JwtClaim(
      content = s"""{"role": "${role}"}""".parseJson.asJsObject.toString,
      expiration = Some(System.currentTimeMillis() / 1000 + TimeUnit.MINUTES.toSeconds(expirationPeriodInMinutes)),
      issuedAt = Some(System.currentTimeMillis() / 1000),
      issuer = Some("knoldus.com")
    )
    JwtSprayJson.encode(claims, secretKey, algorithm) // JWT string
  }

  def tokenDecoder(token : String): Try[JwtClaim] = JwtSprayJson.decode(token, secretKey, Seq(algorithm))

  def isTokenValidOrNot(token: String): Boolean = {
    JwtSprayJson.isValid(token, secretKey, Seq(algorithm))
  }

}

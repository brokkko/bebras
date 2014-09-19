package ru.ipo.sso

import java.security.spec.{PKCS8EncodedKeySpec, X509EncodedKeySpec}
import java.security.{KeyFactory, PrivateKey, PublicKey}
import CryptUtils._

import play.api.Play

case class SSOConfiguration(
                             host: String,
                             id: String,
                            publicKey: PublicKey,
                             privateKey: PrivateKey
                             )

object SSOConfiguration {
  lazy val get: SSOConfiguration = {
    val config = Play.current.configuration.getConfig("ipo-sso") getOrElse {
      throw new Exception("No ipo-sso configuration found")
    }

    def getString(field: String): String =
      config.getString(field) getOrElse {
        throw new Exception(s"No ipo-sso.$field configuration found")
      }

    val host = getString("host")
    val id = getString("id")
    val publicKeyAsString = getString("public")
    val privateKeyAsString = getString("private")

    val keyFactory = KeyFactory.getInstance("RSA")

    def x509ToPublicKey(encodedKey: Array[Byte]): PublicKey = {
      val keySpec = new X509EncodedKeySpec(encodedKey)
      keyFactory.generatePublic(keySpec)
    }

    def pkcs8ToPrivateKey(encodedKey: Array[Byte]): PrivateKey = {
      val keySpec = new PKCS8EncodedKeySpec(encodedKey)
      val f = KeyFactory.getInstance("RSA")
      keyFactory.generatePrivate(keySpec)
    }

    SSOConfiguration(
      host,
      id,
      x509ToPublicKey(base64decode(publicKeyAsString)),
      pkcs8ToPrivateKey(base64decode(privateKeyAsString))
    )
  }
}
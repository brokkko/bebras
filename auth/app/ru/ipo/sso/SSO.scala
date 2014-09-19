package ru.ipo.sso

import play.api.libs.ws.WS
import play.api.mvc.{Results, Call, SimpleResult}

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

object SSO {

  sealed abstract class LoginResult

  case class UserNotFound() extends LoginResult

  case class UserFound(email: String, remoteUid: String, loginToken: String) extends LoginResult

  case class LoginError(message: String) extends LoginResult

  def login(login: String, password: String, redirect: Option[String] = None): Future[LoginResult] = {
    val config: SSOConfiguration = SSOConfiguration.get

    val form: Map[String, Seq[String]] = Map[String, Seq[String]](
      "login" -> Seq(login),
      "password" -> Seq(password),
      "service" -> Seq(config.id),
      "signature" -> Seq(CryptUtils.sign(login, password, config.id, "", redirect getOrElse ""))
    ) ++
      (redirect map { r => Map("redirect" -> Seq(r))} getOrElse {
        Map()
      })

    val willResponse = WS.url(config.host + "/do_login").post(form)

    willResponse map { response =>
      val status = (response.json \ "status").asOpt[String]

      status match {
        case Some("user not found") => UserNotFound()
        case Some("user found") => UserFound(
          (response.json \ "email").as[String],
          (response.json \ "uid").as[String],
          (response.json \ "token").as[String]
        )
        case _ => LoginError(response.body)
      }
    }
  }

  def autoLoginLink(token: String): String = SSOConfiguration.get.host + "/auto_login/" + token

  def autoLogin(token: String): SimpleResult =
    Results.Redirect(Call("GET", autoLoginLink(token)))
}

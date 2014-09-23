package ru.ipo.sso

import play.api.Logger
import play.api.libs.ws.WS
import play.api.mvc.{Results, Call, SimpleResult}

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

object SSO {

  // Login --------------------------------------------------------------------------------------------

  sealed abstract class LoginResult

  case class UserNotFound() extends LoginResult

  case class UserFound(email: String, remoteUid: String, loginToken: String) extends LoginResult

  case class LoginError(message: String) extends LoginResult {
    Logger.debug("Login error: " + message)
  }

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
      if (response.status != 200)
        LoginError(response.body)
      else {
        val status = (response.json \ "status").asOpt[String]

        status match {
          case Some("user not found") => UserNotFound()
          case Some("user found") => UserFound(
            (response.json \ "email").as[String],
            (response.json \ "uid").as[String],
            (response.json \ "token").as[String]
          )
          case Some(message) => LoginError(message)
        }
      }
    }
  }

  def autoLoginLink(token: String): String = SSOConfiguration.get.host + "/auto_login/" + token

  def autoLogin(token: String): SimpleResult =
    Results.Redirect(Call("GET", autoLoginLink(token)))

  // Register --------------------------------------------------------------------------------------------

  sealed abstract class RegistrationResult

  case class RegistrationError(message: String) extends RegistrationResult {
    Logger.debug("Registration error: " + message)
  }

  case class RegistrationSuccessful() extends RegistrationResult

  case class RegistrationDuplication() extends RegistrationResult

  def register(login: String, password: String, email: String, active: Boolean): Future[RegistrationResult] = {
    val config: SSOConfiguration = SSOConfiguration.get
    val service = config.id

    val state: String = (if (active) 1 else 0).toString

    val form: Map[String, Seq[String]] = Map[String, Seq[String]](
      "login" -> Seq(login),
      "password" -> Seq(password),
      "email" -> Seq(email),
      "state" -> Seq(state),
      "service" -> Seq(service),
      "signature" -> Seq(CryptUtils.sign(
         login, password, email, state, service
      ))
    )

    val willResponse = WS.url(config.host + "/create_user").post(form)

    willResponse map { response =>
      if (response.status != 200)
        RegistrationError(response.body)
      else {
        val status = (response.json \ "status").asOpt[String]

        status match {
          case Some("user created") => RegistrationSuccessful()
          case Some("user already exists") => RegistrationDuplication()
          case Some(message) => RegistrationError(message)
        }
      }
    }
  }

  // Modify --------------------------------------------------------------------------------------------

  sealed abstract class ModificationResult

  case class ModificationError(message: String) extends ModificationResult {
    Logger.debug("Modification error: " + message)
  }

  case class ModificationSuccessful() extends ModificationResult

  case class ModificationUserNotFound() extends ModificationResult

  def modify(login: Option[String], password: Option[String], email: String, active: Option[Boolean]): Future[ModificationResult] = {
    val config: SSOConfiguration = SSOConfiguration.get
    val service = config.id

    val stateForForm = active map {if (_) "1" else "0"}

    var form: Map[String, Seq[String]] = Map(
      "email" -> Seq(email),
      "service" -> Seq(service),
      "signature" -> Seq(CryptUtils.sign(
        login getOrElse "",
        password getOrElse "",
        email,
        stateForForm getOrElse "",
        service
      ))
    )

    if (login.isDefined)
      form = form ++ Map("login" -> Seq(login.get))
    if (password.isDefined)
      form = form ++ Map("password" -> Seq(password.get))
    if (active.isDefined)
      form = form ++ Map("state" -> Seq(stateForForm.get))

    val willResponse = WS.url(config.host + "/mod_user").post(form)

    willResponse map { response =>
      if (response.status != 200)
        ModificationError(response.body)
      else {

        val status = (response.json \ "status").asOpt[String]

        status match {
          case Some("user modified") => ModificationSuccessful()
          case Some("user not found") => ModificationUserNotFound()
          case Some(message) => ModificationError(message)
        }
      }
    }
  }
}

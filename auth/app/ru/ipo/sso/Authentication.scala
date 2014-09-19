package ru.ipo.sso

import play.api.libs.ws.WS
import play.api.mvc._

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._
import CryptUtils._

class AuthenticatedRequest[A, U](val user: Option[U], request: Request[A]) extends WrappedRequest[A](request)

class UserAction[U](userConstraint: Option[U] => Option[SimpleResult])(implicit authSettings: AuthenticationSettings[U])
  extends ActionBuilder[({type λ[A] = AuthenticatedRequest[A, U]})#λ] {

  sealed abstract class SessionMatchResult

  case class SessionsAreSame(localSession: String) extends SessionMatchResult

  case class SessionsAreNull() extends SessionMatchResult

  case class NeedLogout() extends SessionMatchResult

  case class NeedLoginFor(globalSession: String) extends SessionMatchResult

  def matchSessions(header: RequestHeader, localSessionCookieName: String): SessionMatchResult = {
    val mbGlobalSession = header.cookies.get("KIO_SSO_SESSION_ID") map {
      _.value
    }
    val mbLocalSession = header.cookies.get(localSessionCookieName) map {
      _.value
    }

    if (mbGlobalSession == mbLocalSession)
      mbLocalSession map SessionsAreSame getOrElse SessionsAreNull()
    else
      mbGlobalSession map NeedLoginFor getOrElse NeedLogout()
  }

  def getUserBySession(globalSessionId: String): Future[Option[U]] = {
    val ssoHost = SSOConfiguration.get.host
    val serviceId = SSOConfiguration.get.id

    val willResponse = WS.url(s"$ssoHost/get_user").post(Map(
      "service" -> Seq(serviceId),
      "session" -> Seq(globalSessionId),
      "signature" -> Seq(sign(serviceId, globalSessionId))
    ))

    willResponse flatMap {
      response =>
        val email = for (
          status <- (response.json \ "status").asOpt[String] if status == "user found";
          email <- (response.json \ "email").asOpt[String]
        ) yield email

        email map {
          authSettings.email2User
        } getOrElse {
          Future.successful(None)
        }
    }
  }

  def invokeBlock[A](request: Request[A], block: (AuthenticatedRequest[A, U]) => Future[SimpleResult]) = {

    def noUser = userConstraint(None) map {
      Future.successful
    } getOrElse
      block(new AuthenticatedRequest(None, request))

    def hasUser(user: U) = userConstraint(Some(user)) map {
      Future.successful
    } getOrElse
      block(new AuthenticatedRequest(Some(user), request))

    matchSessions(request, authSettings.localSessionCookieName) match {
      case SessionsAreNull() => noUser
      case SessionsAreSame(localSession) => authSettings.request2User(request) flatMap {
        _ map hasUser getOrElse noUser
      }
      case NeedLogout() => noUser map {
        result => authSettings.doLogout(result.discardingCookies(DiscardingCookie(authSettings.localSessionCookieName)))
      }
      case NeedLoginFor(globalSession) => getUserBySession(globalSession) flatMap {
        _ map { u => hasUser(u) map authSettings.doLogin(u)} getOrElse noUser
      } map {
        _.withCookies(Cookie(authSettings.localSessionCookieName, globalSession))
      }
    }
  }

}

object UserAction {
  def getBuilder[U](implicit authSettings: AuthenticationSettings[U]) = new UserActionBuilder

  class UserActionBuilder[U](implicit authSettings: AuthenticationSettings[U]) {

    def constrain(constraint: Option[U] => Option[SimpleResult]): UserAction[U] = new UserAction(constraint)

    def apply(): UserAction[U] = new UserAction((_: Option[U]) => None)

    def apply(constraint: U => Boolean): UserAction[U] =
      new UserAction(
        (mayBeUser: Option[U]) => {
          def failed = Some(Results.Redirect(authSettings.needLoginRedirect))

          mayBeUser map {
            user =>
              if (constraint(user))
                None
              else
                failed
          } getOrElse failed
        }
      )

    def apply(needAuthentication: Boolean) =
      new UserAction(
        (_: Option[U]) map { user => None}
          getOrElse Some(Results.Redirect(authSettings.needLoginRedirect))
      )
  }

}

/*
object Authenticator extends Controller {

  case class LoginPassword(
                            login: String,
                            password: String
                            )

  var loginForm = Form(mapping(
    "login" -> nonEmptyText,
    "password" -> nonEmptyText
  )(LoginPassword.apply)(LoginPassword.unapply)),,

  def login = Action {
    Ok(login_form.render(loginForm))
  }

  def doLogin() = Action.async { implicit request =>
    loginForm.bindFromRequest().fold(
      formWithErrors => Future.successful(Ok(login_form.render(formWithErrors))),
      loginPassword => DBUser.byLoginAndPassword(loginPassword.login, loginPassword.password) flatMap {
        case None =>
          Future.successful(Ok(login_form.render(loginForm.withGlobalError("Неверный логин или пароль"))))
        //TODO get from messages
        case Some(user) =>
          Login.successfulLoginForSite(user, routes.Users.showUser(user.email).absoluteURL())
      }
    )
  }

  def logout() = Action.async { implicit request => //TODO protect from CSRF
    val token = LogoutToken(routes.Authenticator.login().absoluteURL())
    val saveToken = DBToken.save(token)

    saveToken map {
      if (_)
        Redirect(routes.Login.autoLogout(token.id))
      else
        InternalServerError("failed to create logout token")
    }
  }
}*/

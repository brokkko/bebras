package ru.ipo.sso

import play.api.mvc.{Result, RequestHeader, Call}

import scala.concurrent.Future

trait AuthenticationSettings[U] {
  val needLoginRedirect: Call
  val localSessionCookieName: String

  /**
   * Get user based on request cookies
   * @param headers headers with cookies that contain information about user logged in
   * @return a logged in user
   */
  def request2User(headers: RequestHeader): Future[Option[U]]

  /**
   * Modify result to set cookies that indicate a logged in user
   * @param user a logged in user
   * @param result a result to which cookies should be added
   * @return new result with cookies
   */
  def doLogin(user: U)(result: Result): Result

  /**
   * Modify result to set cookies that indicate a logged in user
   * @param result a result to which cookies should be added
   * @return new result with cookies
   */
  def doLogout(result: Result): Result

  /**
   * Find user by email
   * @param email
   * @return
   */
  def email2User(email: String): Future[Option[U]]
}

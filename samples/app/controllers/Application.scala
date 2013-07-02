package controllers

import play.api._
import play.api.mvc._
import securesocial.core._

object Application extends Controller with SecureSocial {
  
  def index = SecuredAction(ajaxCall = true) { implicit request =>
    Ok("Authorized.")
  }
  
}
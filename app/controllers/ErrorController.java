package controllers;

import static play.mvc.Results.ok;

import play.mvc.Result;
import views.html.error;

public class ErrorController {

  public ErrorController() {

  }

  public Result renderForm(String errorMessage){
    return ok(error.render(errorMessage));
  }

}
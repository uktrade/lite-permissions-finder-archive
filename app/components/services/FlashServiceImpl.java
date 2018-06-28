package components.services;

import static play.mvc.Controller.flash;

public class FlashServiceImpl implements FlashService {

  @Override
  public void flashInvalidSession() {
    flash("error", "Sorry, your session is no longer valid.");
    flash("detail", "Please start again.");
  }

}

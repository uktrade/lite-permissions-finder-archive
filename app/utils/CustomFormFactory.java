package utils;

import com.google.inject.Inject;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class CustomFormFactory {

  private final FormFactory formFactory;

  @Inject
  public CustomFormFactory(FormFactory formFactory) {
    this.formFactory = formFactory;
  }

  public <T> Form<T> form(Class<T> clazz) {
    return formFactory.form(clazz);
  }

  public <T> Form<T> bindFromRequest(Class<T> clazz) {
    Form<T> form = formFactory.form(clazz).bindFromRequest();
    if (form.hasErrors()) {
      return form;
    } else {
      return validate(form);
    }
  }

  private static <T> Form<T> validate(Form<T> form) {
    for (Field field : form.getBackedType().getFields()) {
      Constraints.MinLength minLength = field.getAnnotation(Constraints.MinLength.class);
      if (minLength != null) {
        form = validateMinLength(form, field.getName(), minLength.value());
      }
    }
    return form;
  }

  private static <T> Form<T> validateMinLength(Form<T> form, String field, long minLength) {
    String value = form.rawData().get(field);
    if (value == null || value.trim().length() < minLength) {
      return form.withError(field, "Minimum length is " + minLength);
    } else {
      Map<String, String> rawData = new HashMap<>(form.rawData());
      rawData.put(field, value.trim());
      return form.bind(rawData);
    }
  }

}

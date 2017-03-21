import com.google.inject.Inject;
import com.google.inject.Singleton;
import filters.common.ExcludingCsrfFilter;
import play.http.HttpFilters;
import play.mvc.EssentialFilter;

@Singleton
public class Filters implements HttpFilters {

  private final EssentialFilter[] filters;

  @Inject
  public Filters(ExcludingCsrfFilter excludingCsrfFilter) {
    filters = new EssentialFilter[]{ excludingCsrfFilter.asJava() };
  }

  @Override
  public EssentialFilter[] filters() {
    return filters;
  }

}

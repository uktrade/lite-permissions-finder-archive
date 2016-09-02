import com.google.inject.Inject;
import com.google.inject.Singleton;
import filters.common.RequestLogFilter;
import play.filters.csrf.CSRFFilter;
import play.http.HttpFilters;
import play.mvc.EssentialFilter;

@Singleton
public class Filters implements HttpFilters {

  private final EssentialFilter[] filters;

  @Inject
  public Filters(RequestLogFilter requestLogFilter, CSRFFilter csrfFilter) {
    filters = new EssentialFilter[] {requestLogFilter, csrfFilter.asJava()};
  }

  @Override
  public EssentialFilter[] filters() {
    return filters;
  }

}

import com.google.inject.Inject;
import com.google.inject.Singleton;
import filters.common.RequestLogFilter;
import play.http.HttpFilters;
import play.mvc.EssentialFilter;

@Singleton
public class Filters implements HttpFilters {

  private final RequestLogFilter requestLogFilter;

  @Inject
  public Filters(RequestLogFilter requestLogFilter) {
    this.requestLogFilter = requestLogFilter;
  }

  @Override
  public EssentialFilter[] filters() {
    return new EssentialFilter[] {requestLogFilter};
  }

}

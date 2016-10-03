import com.google.inject.Inject
import play.api.mvc.{EssentialAction, EssentialFilter}
import play.filters.csrf.CSRFFilter

/**
  * Filter which allows #NOCSRF markup in routes file, to mark certain routes as excluded from CSRF checks. By default, 
  * CSRF protection is either applied to all URLs or a whitelist - there is no out of the box blacklist support. 
  *
  * Taken from http://dominikdorn.com/2014/07/playframework-2-3-global-csrf-protection-disable-csrf-selectively/ 
  *
  * @param filter Onward filter 
  */
class ExcludingCsrfFilter @Inject()(filter: CSRFFilter) extends EssentialFilter {

  override def apply(nextFilter: EssentialAction) = new EssentialAction {

    import play.api.mvc._

    override def apply(rh: RequestHeader) = {
      val chainedFilter = filter.apply(nextFilter)
      if (rh.tags.getOrElse("ROUTE_COMMENTS", "").contains("NOCSRF")) {
        nextFilter(rh)
      } else {
        chainedFilter(rh)
      }
    }
  }
}
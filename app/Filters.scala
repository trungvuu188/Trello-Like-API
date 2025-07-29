import filters.{ApiAccessFilter, CorsFilter, RateLimitFilter, SecurityHeadersFilter}
import play.api.http.HttpFilters
import play.filters.gzip.GzipFilter

import javax.inject.{Inject, Singleton}

@Singleton
class Filters @Inject()(
    corsFilter: CorsFilter,
    securityHeadersFilter: SecurityHeadersFilter,
    rateLimitFilter: RateLimitFilter,
    apiAccessFilter: ApiAccessFilter,
    gzipFilter: GzipFilter
) extends HttpFilters {

    override val filters = Seq(
        corsFilter,           // Handle CORS first
        securityHeadersFilter, // Add security headers
        rateLimitFilter,      // Rate limiting
        apiAccessFilter,      // Authentication & Authorization
        gzipFilter           // Compress responses (built-in Play filter)
    )
}
package co.caio.tablier;

import org.immutables.value.Value;
import org.immutables.value.Value.Style.ImplementationVisibility;

@Value.Style(visibility = ImplementationVisibility.PACKAGE, overshadowImplementation = true)
@Value.Immutable
public interface SearchResultsInfo {

  int paginationStart();

  int paginationEnd();

  int numMatching();

  @Value.Default
  default String previousPageHref() {
    return "";
  }

  @Value.Default
  default String nextPageHref() {
    return "";
  }

  class Builder extends ImmutableSearchResultsInfo.Builder {}
}

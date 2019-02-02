package co.caio.tablier.model;

import java.util.List;
import org.immutables.value.Value;
import org.immutables.value.Value.Style.ImplementationVisibility;

@Value.Style(visibility = ImplementationVisibility.PACKAGE, overshadowImplementation = true)
@Value.Immutable
public interface SearchResultsInfo {

  int paginationStart();

  int paginationEnd();

  long numMatching();

  @Value.Default
  default String previousPageHref() {
    return "";
  }

  @Value.Default
  default String nextPageHref() {
    return "";
  }

  List<RecipeInfo> recipes();

  class Builder extends ImmutableSearchResultsInfo.Builder {}
}

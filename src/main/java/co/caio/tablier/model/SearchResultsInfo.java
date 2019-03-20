package co.caio.tablier.model;

import java.util.List;
import org.immutables.value.Value;

@ImmutableStyle
@Value.Immutable
public interface SearchResultsInfo {

  int paginationStart();

  int paginationEnd();

  long numMatching();

  int numRecipes();

  @Value.Default
  default int numAppliedFilters() {
    return 0;
  }

  @Value.Default
  default String previousPageHref() {
    return "";
  }

  @Value.Default
  default String nextPageHref() {
    return "";
  }

  List<RecipeInfo> recipes();

  SidebarInfo sidebar();

  class Builder extends ImmutableSearchResultsInfo.Builder {}
}

package co.caio.tablier.model;

import java.util.List;
import org.immutables.value.Value;
import org.immutables.value.Value.Style.ImplementationVisibility;

@Value.Style(visibility = ImplementationVisibility.PACKAGE, overshadowImplementation = true)
@Value.Immutable
public interface SidebarInfo {

  List<SortInfo> sortOptions();

  List<FilterInfo> filters();

  @Value.Default
  default boolean showCounts() {
    return true;
  }

  class Builder extends ImmutableSidebarInfo.Builder {
    public Builder addSortOption(String name, String value, boolean isSelected) {
      addSortOptions(SortInfo.of(name, value, isSelected));
      return this;
    }
  }

  @Value.Style(visibility = ImplementationVisibility.PACKAGE, overshadowImplementation = true)
  @Value.Immutable
  interface SortInfo {

    String name();

    String value();

    @Value.Default
    default boolean isSelected() {
      return false;
    }

    class Builder extends ImmutableSortInfo.Builder {}

    static SortInfo of(String name, String value, boolean isSelected) {
      return new Builder().name(name).value(value).isSelected(isSelected).build();
    }
  }
}

package co.caio.tablier.model;

import java.util.List;
import org.immutables.value.Value;

@ImmutableStyle
@Value.Immutable
public interface FilterInfo {

  String name();

  List<FilterOption> options();

  @Value.Default
  default boolean showCounts() {
    return false;
  }

  @Value.Default
  default boolean isRemovable() {
    return true;
  }

  class Builder extends ImmutableFilterInfo.Builder {

    public Builder addOption(String name, String href, boolean isActive) {
      return addOption(name, href, 0, isActive);
    }

    public Builder addOption(String name, String href, int count) {
      addOptions(FilterOption.of(name, href, count, false));
      return this;
    }

    public Builder addOption(String name, String href, int count, boolean isActive) {
      addOptions(FilterOption.of(name, href, count, isActive));
      return this;
    }
  }

  @ImmutableStyle
  @Value.Immutable
  interface FilterOption {
    String name();

    String href();

    @Value.Default
    default int count() {
      return 0;
    }

    @Value.Default
    default boolean isActive() {
      return false;
    }

    class Builder extends ImmutableFilterOption.Builder {}

    static FilterOption of(String name, String href, int count, boolean isActive) {
      return new FilterOption.Builder()
          .name(name)
          .href(href)
          .count(count)
          .isActive(isActive)
          .build();
    }
  }
}

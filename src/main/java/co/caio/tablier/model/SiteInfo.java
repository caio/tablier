package co.caio.tablier.model;

import org.immutables.value.Value;

@ImmutableStyle
@Value.Immutable
public interface SiteInfo {

  String title();

  @Value.Default
  default String description() {
    return "";
  }

  @Value.Default
  default boolean isUnstable() {
    return false;
  }

  @Value.Default
  default String searchAction() {
    return "/search";
  }

  @Value.Default
  default String searchValue() {
    return "";
  }

  @Value.Default
  default boolean searchIsAutoFocus() {
    return true;
  }

  class Builder extends ImmutableSiteInfo.Builder {}
}

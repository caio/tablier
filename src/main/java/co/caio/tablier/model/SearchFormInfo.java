package co.caio.tablier.model;

import org.immutables.value.Value;

@ImmutableStyle
@Value.Immutable
public interface SearchFormInfo {

  @Value.Default
  default String value() {
    return "";
  }

  @Value.Default
  default boolean isAutoFocus() {
    return true;
  }

  class Builder extends ImmutableSearchFormInfo.Builder {}
}

package co.caio.tablier.model;

import org.immutables.value.Value;
import org.immutables.value.Value.Style.ImplementationVisibility;

@Value.Style(visibility = ImplementationVisibility.PACKAGE, overshadowImplementation = true)
@Value.Immutable
public interface SiteInfo {

  @Value.Default
  default boolean isUnstable() {
    return false;
  }

  class Builder extends ImmutableSiteInfo.Builder {}
}

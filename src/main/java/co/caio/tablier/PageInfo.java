package co.caio.tablier;

import org.immutables.value.Value;
import org.immutables.value.Value.Style.ImplementationVisibility;

@Value.Style(visibility = ImplementationVisibility.PACKAGE, overshadowImplementation = true)
@Value.Immutable
public interface PageInfo {
  String title();

  @Value.Default
  default boolean showUnstableWarning() {
    return false;
  }

  class Builder extends ImmutablePageInfo.Builder {}
}

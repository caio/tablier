package co.caio.tablier.model;

import java.util.List;
import org.immutables.value.Value;
import org.immutables.value.Value.Style.ImplementationVisibility;

@Value.Style(visibility = ImplementationVisibility.PACKAGE, overshadowImplementation = true)
@Value.Immutable
public interface SidebarInfo {

  List<FilterInfo> filters();

  @Value.Default
  default boolean showCounts() {
    return true;
  }

  class Builder extends ImmutableSidebarInfo.Builder {}
}

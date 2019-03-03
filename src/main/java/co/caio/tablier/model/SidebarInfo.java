package co.caio.tablier.model;

import java.util.List;
import org.immutables.value.Value;

@ImmutableStyle
@Value.Immutable
public interface SidebarInfo {

  List<FilterInfo> filters();

  class Builder extends ImmutableSidebarInfo.Builder {}
}

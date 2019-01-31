package co.caio.tablier;

import java.util.List;
import org.immutables.value.Value;
import org.immutables.value.Value.Style.ImplementationVisibility;


@Value.Style(
    visibility = ImplementationVisibility.PACKAGE,
    overshadowImplementation = true
)
@Value.Immutable
public interface SiteInfo {

  @Value.Default
  default String title() { return "gula.recipes"; }

  List<NavigationItem> navigationItems();

  class Builder extends ImmutableSiteInfo.Builder {
    public Builder addNavigationItem(String href, String text, boolean isActive) {
      addNavigationItems(new NavigationItem(href, text, isActive));
      return this;
    }
  }

  class NavigationItem {
    private final String href;
    private final String text;
    private final boolean isActive;

    NavigationItem(String href, String text, boolean isActive) {
      this.href = href;
      this.text = text;
      this.isActive = isActive;
    }

    public String href() {
      return href;
    }

    public String text() {
      return text;
    }

    public boolean isActive() {
      return isActive;
    }
  }
}

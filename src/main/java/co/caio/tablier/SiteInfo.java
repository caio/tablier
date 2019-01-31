package co.caio.tablier;

import java.util.List;

public class SiteInfo {

  public String title() {
    return "gula.recipes";
  }

  public List<NavigationItem> navigationItems() {
    return List.of(
        new NavigationItem("/", "gula.recipes", true),
        new NavigationItem("/about", "About", false));
  }

  public class NavigationItem {
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

package co.caio.tablier;

import com.fizzed.rocker.runtime.ArrayOfByteArraysOutput;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import views.Index;

public class Main {

  private static final Path outputDir = Path.of("src/");

  private static final Map<String, PageInfo> pageVariations;
  private static final Map<String, SearchFormInfo> searchFormVariations;

  static {
    var pages = new HashMap<String, PageInfo>();
    var defaultPage = new PageInfo.Builder().title("Index").build();
    pages.put("", defaultPage);
    pages.put(
        "_unstable", new PageInfo.Builder().from(defaultPage).showUnstableWarning(true).build());

    pageVariations = Collections.unmodifiableMap(pages);

    var searchforms = new HashMap<String, SearchFormInfo>();
    var defaultSearchForm = new SearchFormInfo.Builder().build();
    searchforms.put("", defaultSearchForm);
    searchforms.put("_disabled", new SearchFormInfo.Builder().isDisabled(true).build());
    searchforms.put("_nofocus", new SearchFormInfo.Builder().isAutoFocus(false).build());
    searchforms.put(
        "_disablednofocus",
        new SearchFormInfo.Builder().isDisabled(true).isAutoFocus(false).build());

    searchFormVariations = Collections.unmodifiableMap(searchforms);
  }

  public static void main(String[] args) {

    var siteInfo =
        new SiteInfo.Builder()
            .title("gula.recipes")
            .addNavigationItem("/", "gula.recipes", true)
            .addNavigationItem("/about", "About", false)
            .build();

    pageVariations.forEach(
        (pagePrefix, page) -> {
          searchFormVariations.forEach(
              (searchFormPrefix, searchForm) -> {
                var name = String.format("index%s%s.html", pagePrefix, searchFormPrefix);

                try (var os = new FileOutputStream(outputDir.resolve(name).toFile())) {
                  var result =
                      Index.template(siteInfo, page, searchForm)
                          .render(ArrayOfByteArraysOutput.FACTORY);

                  for (byte[] array : result.getArrays()) {
                    os.write(array);
                  }
                } catch (IOException e) {
                  e.printStackTrace();
                  System.exit(1);
                }
              });
        });
  }
}

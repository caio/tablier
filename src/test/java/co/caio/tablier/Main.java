package co.caio.tablier;

import com.fizzed.rocker.runtime.ArrayOfByteArraysOutput;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import views.Index;
import views.Search;
import views.ZeroResults;

public class Main {

  private static final Path outputDir = Path.of("src/");

  private static final Map<String, PageInfo> pageVariations;
  private static final Map<String, SearchFormInfo> searchFormVariations;
  private static final Map<String, SearchResultsInfo> searchResultsVariations;

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

    var srs = new HashMap<String, SearchResultsInfo>();

    var srSinglePage =
        new SearchResultsInfo.Builder()
            .paginationStart(1)
            .paginationEnd(19)
            .numMatching(19)
            .build();
    srs.put("", srSinglePage);

    var srHasNext =
        new SearchResultsInfo.Builder()
            .paginationStart(1)
            .paginationEnd(20)
            .numMatching(21)
            .nextPageHref("/next")
            .build();
    srs.put("_next", srHasNext);

    var srHasPrevious =
        new SearchResultsInfo.Builder()
            .paginationStart(21)
            .paginationEnd(21)
            .numMatching(21)
            .previousPageHref("/previous")
            .build();
    srs.put("_prev", srHasPrevious);

    var srHasBoth =
        new SearchResultsInfo.Builder()
            .paginationStart(41)
            .paginationEnd(60)
            .numMatching(99)
            .nextPageHref("/next")
            .previousPageHref("/previous")
            .build();
    srs.put("_both", srHasBoth);

    searchResultsVariations = Collections.unmodifiableMap(srs);
  }

  private static void writeResult(String filename, ArrayOfByteArraysOutput result) {
    try (var os = new FileOutputStream(outputDir.resolve(filename).toFile())) {
      for (byte[] array : result.getArrays()) {
        os.write(array);
      }
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  private static void renderIndex(
      String filename, SiteInfo siteInfo, PageInfo pageInfo, SearchFormInfo searchFormInfo) {

    writeResult(
        filename,
        Index.template(siteInfo, pageInfo, searchFormInfo).render(ArrayOfByteArraysOutput.FACTORY));
  }

  private static void renderSearch(
      String filename,
      SiteInfo siteInfo,
      PageInfo pageInfo,
      SearchFormInfo searchFormInfo,
      SearchResultsInfo srInfo) {
    try (var os = new FileOutputStream(outputDir.resolve(filename).toFile())) {
      var result =
          Search.template(siteInfo, pageInfo, searchFormInfo, srInfo)
              .render(ArrayOfByteArraysOutput.FACTORY);

      for (byte[] array : result.getArrays()) {
        os.write(array);
      }
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  public static void main(String[] args) {

    var siteInfo =
        new SiteInfo.Builder()
            .title("gula.recipes")
            .addNavigationItem("/", "gula.recipes", true)
            .addNavigationItem("/about", "About", false)
            .build();

    pageVariations.forEach(
        (pagePrefix, page) ->
            searchFormVariations.forEach(
                (searchFormPrefix, searchForm) -> {
                  var indexName = String.format("index%s%s.html", pagePrefix, searchFormPrefix);
                  var zeroName = String.format("zero_results%s%s.html", pagePrefix, searchFormPrefix);

                  writeResult(
                      indexName,
                      Index.template(siteInfo, page, searchForm)
                          .render(ArrayOfByteArraysOutput.FACTORY));

                  writeResult(
                      zeroName,
                      ZeroResults.template(siteInfo, page, searchForm)
                          .render(ArrayOfByteArraysOutput.FACTORY));

                  searchResultsVariations.forEach(
                      (srPrefix, sr) -> {
                        var searchName =
                            String.format(
                                "search%s%s%s.html", pagePrefix, searchFormPrefix, srPrefix);

                        writeResult(
                            searchName,
                            Search.template(siteInfo, page, searchForm, sr)
                                .render(ArrayOfByteArraysOutput.FACTORY));
                      });
                }));
  }
}

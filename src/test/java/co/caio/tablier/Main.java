package co.caio.tablier;

import com.fizzed.rocker.runtime.StringBuilderOutput;
import views.Index;

public class Main {
  public static void main(String[] args) {
    var view =
        Index.template(new SiteInfo(), new PageInfo(), new SearchInfo())
            .render(StringBuilderOutput.FACTORY);
    System.out.println(view.toString());
  }
}

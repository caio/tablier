package co.caio.tablier;

import com.fizzed.rocker.runtime.StringBuilderOutput;
import views.Hello;

public class Main {
  public static void main(String[] args) {
    var view = Hello.template("world").render(StringBuilderOutput.FACTORY);
    System.out.println(view.toString());
  }
}

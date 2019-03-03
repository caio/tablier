package co.caio.tablier.model;

import org.immutables.value.Value;

@ImmutableStyle
@Value.Immutable
public interface ErrorInfo {

  String title();

  String subtitle();

  class Builder extends ImmutableErrorInfo.Builder {}
}

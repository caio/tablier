package co.caio.tablier.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.immutables.value.Value;
import org.immutables.value.Value.Style.ImplementationVisibility;

@Target({ElementType.PACKAGE, ElementType.TYPE})
@Retention(RetentionPolicy.CLASS) // Make it class retention for incremental compilation
@Value.Style(
    visibility = ImplementationVisibility.PACKAGE,
    overshadowImplementation = true,
    defaults = @Value.Immutable(copy = false))
@interface ImmutableStyle {}

package com.google.code.geobeagle.xmlimport;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.google.inject.BindingAnnotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

public class XmlimportAnnotations {
    private XmlimportAnnotations() {
    }
    @BindingAnnotation @Target( { FIELD, PARAMETER, METHOD })
    @Retention(RUNTIME) public static @interface GpxAnnotation { }

    @BindingAnnotation @Target( { FIELD, PARAMETER, METHOD })
    @Retention(RUNTIME) public static @interface SDCard { }

    @BindingAnnotation @Target( { FIELD, PARAMETER, METHOD })
    @Retention(RUNTIME) public static @interface ImportDirectory { }

    @BindingAnnotation @Target( { FIELD, PARAMETER, METHOD })
    @Retention(RUNTIME) public static @interface DetailsDirectory { }

    @BindingAnnotation @Target( { FIELD, PARAMETER, METHOD })
    @Retention(RUNTIME) public static @interface VersionPath { }

    @BindingAnnotation @Target( { FIELD, PARAMETER, METHOD })
    @Retention(RUNTIME) public static @interface OldDetailsDirectory { }

    @BindingAnnotation @Target( { FIELD, PARAMETER, METHOD })
    @Retention(RUNTIME) public static @interface LoadDetails { }

    @BindingAnnotation @Target( { FIELD, PARAMETER, METHOD })
    @Retention(RUNTIME) public static @interface WriteDetails { }
}

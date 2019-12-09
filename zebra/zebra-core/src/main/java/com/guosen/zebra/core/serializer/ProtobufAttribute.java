package com.guosen.zebra.core.serializer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.guosen.zebra.core.serializer.utils.JStringUtils;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ProtobufAttribute {

  boolean required() default false;

  String protobufSetter() default JStringUtils.EMPTY;

  String protobufGetter() default JStringUtils.EMPTY;

  String pojoGetter() default JStringUtils.EMPTY;

  String pojoSetter() default JStringUtils.EMPTY;

}

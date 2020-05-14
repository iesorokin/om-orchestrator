package ru.iesorokin.orchestrator.sleuth

import java.lang.annotation.Inherited

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
annotation class ExtractProcessDataFromDelegate

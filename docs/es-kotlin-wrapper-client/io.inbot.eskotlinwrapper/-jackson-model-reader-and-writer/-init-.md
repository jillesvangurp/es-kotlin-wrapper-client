[es-kotlin-wrapper-client](../../index.md) / [io.inbot.eskotlinwrapper](../index.md) / [JacksonModelReaderAndWriter](index.md) / [&lt;init&gt;](./-init-.md)

# &lt;init&gt;

`JacksonModelReaderAndWriter(javaClazz: `[`Class`](https://docs.oracle.com/javase/8/docs/api/java/lang/Class.html)`<`[`T`](index.md#T)`>, objectMapper: ObjectMapper)`

Alternate constructor so we can instantiate from Java as well

`JacksonModelReaderAndWriter(clazz: `[`KClass`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.reflect/-k-class/index.html)`<`[`T`](index.md#T)`>, objectMapper: ObjectMapper)`

Simple implementation of [ModelReaderAndWriter](../-model-reader-and-writer/index.md) that uses a jackson object mapper.

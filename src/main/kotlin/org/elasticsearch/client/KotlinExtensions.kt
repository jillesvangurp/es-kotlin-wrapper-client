package org.elasticsearch.client

import com.fasterxml.jackson.databind.ObjectMapper
import io.inbot.eskotlinwrapper.IndexDAO
import io.inbot.eskotlinwrapper.JacksonModelReaderAndWriter
import io.inbot.eskotlinwrapper.ModelReaderAndWriter
import io.inbot.eskotlinwrapper.SuspendingActionListener.Companion.suspending
import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.impl.client.BasicCredentialsProvider
import org.elasticsearch.action.search.ClearScrollRequest
import org.elasticsearch.action.search.ClearScrollResponse
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.action.search.SearchScrollRequest
import org.elasticsearch.client.indices.CreateIndexRequest
import org.elasticsearch.client.indices.CreateIndexResponse
import org.elasticsearch.common.unit.TimeValue

/**
 * Fake constructor like factory that gives you sane defaults that will allow you to quickly connect to elastic cloud.
 */
@Suppress("FunctionName")
fun RestHighLevelClient(
    host: String = "localhost",
    port: Int = 9200,
    https: Boolean = false,
    user: String? = null,
    password: String? = null
): RestHighLevelClient {
    var restClientBuilder = RestClient.builder(HttpHost(host, port, if (https) "https" else "http"))
    if (!user.isNullOrBlank()) {
        restClientBuilder = restClientBuilder.setHttpClientConfigCallback {
            val basicCredentialsProvider = BasicCredentialsProvider()
            basicCredentialsProvider.setCredentials(
                AuthScope.ANY,
                UsernamePasswordCredentials(user, password)
            )
            it.setDefaultCredentialsProvider(basicCredentialsProvider)
        }
    }
    return RestHighLevelClient(restClientBuilder)
}

fun <T : Any> RestHighLevelClient.crudDao(
    index: String,
    modelReaderAndWriter: ModelReaderAndWriter<T>,
    type: String = "_doc",
    readAlias: String = index,
    writeAlias: String = index,
    refreshAllowed: Boolean = false,
    defaultRequestOptions: RequestOptions = RequestOptions.DEFAULT
): IndexDAO<T> {
    return IndexDAO(
        indexName = index,
        client = this,
        modelReaderAndWriter = modelReaderAndWriter,
        refreshAllowed = refreshAllowed,
        type = type,
        indexReadAlias = readAlias,
        indexWriteAlias = writeAlias,
        defaultRequestOptions = defaultRequestOptions

    )
}

inline fun <reified T : Any> RestHighLevelClient.crudDao(
    index: String,
    objectMapper: ObjectMapper = ObjectMapper().findAndRegisterModules(),
    refreshAllowed: Boolean = false
): IndexDAO<T> {
    return IndexDAO(
        index,
        this,
        JacksonModelReaderAndWriter(T::class, objectMapper),
        refreshAllowed = refreshAllowed
    )
}

fun RestHighLevelClient.search(
    requestOptions: RequestOptions = RequestOptions.DEFAULT,
    block: SearchRequest.() -> Unit
): SearchResponse {
    val searchRequest = SearchRequest()
    block.invoke(searchRequest)
    return this.search(searchRequest, requestOptions)
}

suspend fun RestHighLevelClient.searchAsync(
    requestOptions: RequestOptions = RequestOptions.DEFAULT,
    block: SearchRequest.() -> Unit
): SearchResponse {
    val searchRequest = SearchRequest()
    block.invoke(searchRequest)
    return suspending {
        this.searchAsync(searchRequest, requestOptions, it)
    }
}

fun RestHighLevelClient.scroll(
    scrollId: String,
    ttl: Long,
    requestOptions: RequestOptions = RequestOptions.DEFAULT
): SearchResponse {
    return this.scroll(
        SearchScrollRequest(scrollId).scroll(
            TimeValue.timeValueMinutes(
                ttl
            )
        ), requestOptions
    )
}

suspend fun RestHighLevelClient.scrollAsync(
    scrollId: String,
    ttl: Long,
    requestOptions: RequestOptions = RequestOptions.DEFAULT
): SearchResponse {
    return suspending {
        this.scrollAsync(
            SearchScrollRequest(scrollId).scroll(
                TimeValue.timeValueMinutes(
                    ttl
                )
            ), requestOptions, it
        )
    }
}

fun RestHighLevelClient.clearScroll(
    vararg scrollIds: String,
    requestOptions: RequestOptions = RequestOptions.DEFAULT
): ClearScrollResponse {
    val clearScrollRequest = ClearScrollRequest()
    scrollIds.forEach { clearScrollRequest.addScrollId(it) }
    return this.clearScroll(clearScrollRequest, requestOptions)
}

suspend fun RestHighLevelClient.clearScrollAsync(
    vararg scrollIds: String,
    requestOptions: RequestOptions = RequestOptions.DEFAULT
): ClearScrollResponse {
    // FIXME figure out a way to use this to create some kind of suspending Sequence<SearchResponse>, this seems to be hard currently
    return suspending {
        val clearScrollRequest = ClearScrollRequest()
        scrollIds.forEach { clearScrollRequest.addScrollId(it) }
        this.clearScrollAsync(clearScrollRequest, requestOptions, it)
    }
}

suspend fun IndicesClient.createIndexAsync(
    index: String,
    requestOptions: RequestOptions = RequestOptions.DEFAULT,
    block: CreateIndexRequest.() -> Unit
): CreateIndexResponse {
    val request = CreateIndexRequest(index)
    block.invoke(request)
    return suspending {
        this.createAsync(request, requestOptions, it)
    }
}
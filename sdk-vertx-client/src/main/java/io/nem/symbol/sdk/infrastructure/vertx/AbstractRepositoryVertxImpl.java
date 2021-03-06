/*
 * Copyright 2020 NEM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nem.symbol.sdk.infrastructure.vertx;

import io.nem.symbol.core.crypto.PublicKey;
import io.nem.symbol.core.utils.ConvertUtils;
import io.nem.symbol.core.utils.MapperUtils;
import io.nem.symbol.sdk.api.OrderBy;
import io.nem.symbol.sdk.api.Page;
import io.nem.symbol.sdk.api.RepositoryCallException;
import io.nem.symbol.sdk.model.account.Address;
import io.nem.symbol.sdk.model.transaction.JsonHelper;
import io.nem.symbol.sdk.openapi.vertx.invoker.ApiClient;
import io.nem.symbol.sdk.openapi.vertx.invoker.ApiException;
import io.nem.symbol.sdk.openapi.vertx.model.Order;
import io.nem.symbol.sdk.openapi.vertx.model.Pagination;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.reactivex.core.impl.AsyncResultSingle;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * Created by fernando on 30/07/19.
 *
 * @author Fernando Boucquez
 */
public abstract class AbstractRepositoryVertxImpl {


    private final JsonHelper jsonHelper;

    public AbstractRepositoryVertxImpl(ApiClient apiClient) {
        this.jsonHelper = new JsonHelperJackson2(apiClient.getObjectMapper());
    }

    public <T> Observable<T> call(Consumer<Handler<AsyncResult<T>>> callback) {
        IllegalArgumentException originalException = new IllegalArgumentException("Original call");
        Function<? super Throwable, ? extends ObservableSource<? extends T>> resumeFunction = this
            .onError(originalException);
        return new AsyncResultSingle<T>(callback::accept).toObservable()
            .onErrorResumeNext(resumeFunction);
    }

    public RepositoryCallException exceptionHandling(Throwable e,
        IllegalArgumentException originalException) {
        if (e instanceof RepositoryCallException) {
            return (RepositoryCallException) e;
        }
        return new RepositoryCallException(
            extractMessageFromException(e),
            extractStatusCodeFromException(e), e instanceof ApiException ? originalException : e);
    }

    private String extractMessageFromException(Throwable e) {
        List<String> messages = new ArrayList<>();
        messages.add(ExceptionUtils.getMessage(e));
        if (e instanceof ApiException) {
            messages.add("" + ((ApiException) e).getCode());
            String responseBody = ((ApiException) e).getResponseBody();
            if (responseBody != null) {
                try {
                    // Extracting message from the response body.
                    Object json = jsonHelper.parse(responseBody);
                    messages.add(jsonHelper.getString(json, "code"));
                    messages.add(jsonHelper.getString(json, "message"));
                } catch (IllegalArgumentException ignore) {
                    messages.add(StringUtils.truncate(responseBody, 100));
                }
            }
        }
        return messages.stream().filter(StringUtils::isNotBlank).collect(Collectors.joining(" - "));
    }

    private int extractStatusCodeFromException(Throwable e) {
        return (e instanceof ApiException) ? ((ApiException) e).getCode() : 0;
    }

    public <T> Function<Throwable, Observable<T>> onError(
        IllegalArgumentException originalException) {
        return (Throwable e) -> Observable.error(exceptionHandling(e, originalException));
    }

    public <T> Observable<T> exceptionHandling(Observable<T> observable) {

        IllegalArgumentException originalException = new IllegalArgumentException("Original call");
        Function<? super Throwable, ? extends ObservableSource<? extends T>> resumeFunction = this
            .onError(originalException);
        return observable.onErrorResumeNext(resumeFunction);
    }

    protected Order toDto(OrderBy order) {
        return order == null ? null : Order.fromValue(order.getValue());
    }

    protected String toDto(PublicKey publicKey) {
        return publicKey == null ? null : publicKey.toHex();
    }

    protected String toDto(Address address) {
        return address == null ? null : address.plain();
    }

    protected String toDto(BigInteger number) {
        if (number == null) {
            return null;
        }
        ConvertUtils.validateNotNegative(number);
        return MapperUtils.fromBigIntegerToHex(number);
    }

    protected <T> Page<T> toPage(Pagination pagination, List<T> data) {
        return new Page<>(data, pagination.getPageNumber(), pagination.getPageSize(),
            pagination.getTotalEntries(), pagination.getTotalPages());
    }


    public JsonHelper getJsonHelper() {
        return jsonHelper;
    }
}

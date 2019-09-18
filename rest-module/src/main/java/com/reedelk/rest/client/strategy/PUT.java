package com.reedelk.rest.client.strategy;

import com.reedelk.rest.client.BodyProvider;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPut;

public class PUT extends BaseStrategyWithBody {

    @Override
    protected HttpEntityEnclosingRequestBase request(BodyProvider bodyProvider) {
        return new HttpPut();
    }
}

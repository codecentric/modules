package com.reedelk.rest.apacheclient.strategy;

import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpRequestBase;

public class HEAD extends BaseStrategy {

    @Override
    protected HttpRequestBase request() {
        return new HttpHead();
    }
}

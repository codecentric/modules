package com.reedelk.rest.configuration;

import com.reedelk.rest.server.HttpRequestHandler;
import com.reedelk.rest.server.HttpServerRoutes;
import com.reedelk.runtime.api.annotation.Default;

@Default("GET")
public enum RestMethod {

    GET {
        @Override
        public void addRoute(HttpServerRoutes routes, String path, HttpRequestHandler handler) {
            routes.get(path, handler);
        }
    },

    POST {
        @Override
        public void addRoute(HttpServerRoutes routes, String path, HttpRequestHandler handler) {
            routes.post(path, handler);
        }
    },

    PUT {
        @Override
        public void addRoute(HttpServerRoutes routes, String path, HttpRequestHandler handler) {
            routes.put(path, handler);
        }
    },

    DELETE {
        @Override
        public void addRoute(HttpServerRoutes routes, String path, HttpRequestHandler handler) {
            routes.delete(path, handler);
        }
    },

    HEAD {
        @Override
        public void addRoute(HttpServerRoutes routes, String path, HttpRequestHandler handler) {
            routes.head(path, handler);
        }
    },

    OPTIONS {
        @Override
        public void addRoute(HttpServerRoutes routes, String path, HttpRequestHandler handler) {
            routes.head(path, handler);
        }
    };

    public abstract void addRoute(HttpServerRoutes routes, String path, HttpRequestHandler handler);


}

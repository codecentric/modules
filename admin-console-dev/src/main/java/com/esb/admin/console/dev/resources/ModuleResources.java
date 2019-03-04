package com.esb.admin.console.dev.resources;

import com.esb.internal.api.module.v1.ModuleService;
import org.takes.Request;
import org.takes.Response;
import org.takes.facets.fork.FkMethods;
import org.takes.facets.fork.FkRegex;
import org.takes.facets.fork.Fork;
import org.takes.facets.fork.TkFork;
import org.takes.misc.Opt;

import java.io.IOException;

import static com.esb.admin.console.dev.HttpMethod.*;

public class ModuleResources implements Fork {

    private static final String BASE_PATH = "/module";

    private final FkRegex fkRegex;

    public ModuleResources(ModuleService service) {
        fkRegex = new FkRegex(BASE_PATH, new TkFork(
                new FkMethods(PUT.name(), new ModulePUTResource(service)),
                new FkMethods(POST.name(), new ModulePOSTResource(service)),
                new FkMethods(DELETE.name(), new ModuleDELETEResource(service)),
                new FkMethods(GET.name(), new ModuleGETResource(service))));
    }

    @Override
    public Opt<Response> route(Request request) throws IOException {
        return fkRegex.route(request);
    }


}

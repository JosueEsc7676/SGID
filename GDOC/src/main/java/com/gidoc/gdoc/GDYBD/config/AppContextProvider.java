package com.gidoc.gdoc.GDYBD.config;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class AppContextProvider {

    private static ApplicationContext context;

    public AppContextProvider(ApplicationContext applicationContext) {
        context = applicationContext;
    }

    public static ApplicationContext getApplicationContext() {
        return context;
    }
}

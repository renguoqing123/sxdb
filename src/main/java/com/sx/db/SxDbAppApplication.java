package com.sx.db;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * Hello world!
 *
 */
@SpringBootApplication
public class SxDbAppApplication {
    public static void main( String[] args ){
    	new SpringApplicationBuilder(SxDbAppApplication.class).web(WebApplicationType.SERVLET).run(args);
    }
}

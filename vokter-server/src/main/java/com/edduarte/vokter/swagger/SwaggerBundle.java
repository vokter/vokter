package com.edduarte.vokter.swagger;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.listing.ApiListingResource;

import java.util.Map;

/**
 * A {@link ConfiguredBundle} that provides hassle-free configuration of Swagger
 * and Swagger UI on top of Dropwizard.
 *
 * @author Eduardo Duarte
 * @author Federico Recio
 * @author Flemming Frandsen
 * @author Tristan Burch
 */
@FunctionalInterface
public interface SwaggerBundle<T extends Configuration> extends ConfiguredBundle<T> {

    @Override
    default void initialize(Bootstrap<?> bootstrap) {
        bootstrap.addBundle(new ViewBundle<Configuration>() {
            @Override
            public Map<String, Map<String, String>> getViewConfiguration(Configuration configuration) {
                return ImmutableMap.of();
            }
        });
    }


    @Override
    default void run(T var, Environment environment) throws Exception {
        SwaggerBundleConfig config =
                getSwaggerBundleConfiguration(var);
        if (config == null) {
            throw new IllegalStateException("You need to provide " +
                    "an instance of SwaggerBundleConfiguration");
        }

        ConfigurationHelper configurationHelper = new ConfigurationHelper(var, config);
        new AssetsBundle(
                Constants.SWAGGER_RESOURCES_PATH,
                configurationHelper.getSwaggerUriPath(),
                null,
                Constants.SWAGGER_ASSETS_NAME).run(environment);

        environment.jersey()
                .register(new SwaggerResource(configurationHelper.getUrlPattern()));
        environment.getObjectMapper()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);

        setUpSwagger(config, configurationHelper.getUrlPattern());
        environment.jersey().register(new ApiListingResource());
    }


    SwaggerBundleConfig getSwaggerBundleConfiguration(T configuration);


    default void setUpSwagger(SwaggerBundleConfig swaggerBundleConfig, String urlPattern) {
        BeanConfig config = new BeanConfig();

        if (swaggerBundleConfig.getTitle() != null) {
            config.setTitle(swaggerBundleConfig.getTitle());
        }

        if (swaggerBundleConfig.getVersion() != null) {
            config.setVersion(swaggerBundleConfig.getVersion());
        }

        if (swaggerBundleConfig.getDescription() != null) {
            config.setDescription(swaggerBundleConfig.getDescription());
        }

        if (swaggerBundleConfig.getContact() != null) {
            config.setContact(swaggerBundleConfig.getContact());
        }

        if (swaggerBundleConfig.getLicense() != null) {
            config.setLicense(swaggerBundleConfig.getLicense());
        }

        if (swaggerBundleConfig.getLicenseUrl() != null) {
            config.setLicenseUrl(swaggerBundleConfig.getLicenseUrl());
        }

        if (swaggerBundleConfig.getTermsOfServiceUrl() != null) {
            config.setTermsOfServiceUrl(swaggerBundleConfig.getTermsOfServiceUrl());
        }

        config.setBasePath(urlPattern);

        if (swaggerBundleConfig.getResourcePackage() != null) {
            config.setResourcePackage(swaggerBundleConfig.getResourcePackage());
        } else {
            throw new IllegalStateException("Resource package needs to be " +
                    "specified for Swagger to correctly detect annotated " +
                    "resources");
        }

        config.setScan(true);
    }
}

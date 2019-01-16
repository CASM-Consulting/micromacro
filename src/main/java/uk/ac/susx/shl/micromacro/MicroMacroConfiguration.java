package uk.ac.susx.shl.micromacro;

import io.dropwizard.Configuration;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.bundles.assets.AssetsBundleConfiguration;
import io.dropwizard.bundles.assets.AssetsConfiguration;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.db.DataSourceFactory;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.util.Map;

public class MicroMacroConfiguration extends Configuration implements AssetsBundleConfiguration {

    
    @NotNull
    @JsonProperty
    String workspaceMapPath;

    @NotNull
    @JsonProperty
    String obMapPath;

    @NotNull
    @JsonProperty
    String obCacheTable;


    @NotNull
    @JsonProperty
    String sessionsPath;

    @NotNull
    @JsonProperty
    String geoJsonPath;

    @NotNull
    @JsonProperty
    int pubNerPort;

    @NotNull
    @JsonProperty
    int placeNerPort;

    @Valid
    @NotNull
    @JsonProperty
    private final AssetsConfiguration assets = AssetsConfiguration.builder().build();

    @Override
    public AssetsConfiguration getAssetsConfiguration() {
        return assets;
    }


//    @Valid
//    @NotNull
//    private JerseyClientConfiguration jerseyClient = new JerseyClientConfiguration();

//    @JsonProperty("jerseyClient")
//    public JerseyClientConfiguration getJerseyClientConfiguration() {
//        return jerseyClient;
//    }
//
//    @JsonProperty("jerseyClient")
//    public void setJerseyClientConfiguration(JerseyClientConfiguration jerseyClient) {
//        this.jerseyClient = jerseyClient;
//    }





    @Valid
    @NotNull
    private DataSourceFactory database = new DataSourceFactory();

    @JsonProperty("database")
    public void setDataSourceFactory(DataSourceFactory factory) {
        this.database = factory;
    }

    @JsonProperty("database")
    public DataSourceFactory getDataSourceFactory() {
        return database;
    }
}

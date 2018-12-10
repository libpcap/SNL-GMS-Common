package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.service;

import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.repository.StationReferenceRepositoryInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.service.configuration.Configuration;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.service.configuration.Endpoints;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.service.handlers.ExceptionHandlers;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.service.handlers.RouteHandlers;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.service.responsetransformers.JsonTransformer;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.service.handlers.HttpErrorHandlers;
import org.apache.commons.lang3.Validate;
import spark.Spark;

/**
 * Service implementation of the stationreference-repository.
 *
 * Provides a minimal wrapper around a singleton {@link Spark} service.
 */
public class StationReferenceCoiService {

  private StationReferenceCoiService() { }

  /**
   * Starts the service with the provided {@link Configuration} to determine service properties.
   * Routes repository calls to the provided {@link StationReferenceRepositoryInterface}.
   *
   * @param configuration the configuration of the service
   * @param stationRefRepo the repository for retrieving station reference information
   */
  public static void startService(Configuration configuration,
      StationReferenceRepositoryInterface stationRefRepo) {
    Validate.notNull(configuration,
        "Cannot create StationReferenceCoiService with null configuration");
    Validate.notNull(stationRefRepo,
        "Cannot create StationReferenceCoiService with null station ref repository");

    configureHttpServer(configuration);
    configureRoutesAndFilters(configuration, stationRefRepo);
    Spark.awaitInitialization();

  }

  /**
   * Stops the REST service.
   */
  public static void stopService() {
    Spark.stop();
  }

  /**
   * Configures the HTTP server before the it is started. NOTE: This method must be called before
   * routes and filters are declared, because Spark Java automatically starts the HTTP server when
   * routes and filters are declared.
   *
   * @param configuration the configuration to be used
   */
  private static void configureHttpServer(Configuration configuration) {
    // Set the listening port.
    Spark.port(configuration.port);

    // Set the min/max number of threads, and the idle timeout.
    Spark.threadPool(configuration.maxThreads,
        configuration.minThreads,
        configuration.idleTimeOutMillis);
  }

  /**
   * Registers all routes and filters. Sets exceptions for endpoints to specific pre implemented
   * responses.
   *
   * @param configuration the configuration to be used
   * @param stationRefRepo the repository for retrieving station reference information
   */
  private static void configureRoutesAndFilters(Configuration configuration,
      StationReferenceRepositoryInterface stationRefRepo) {
    // Exception handler.
    Spark.exception(Exception.class, ExceptionHandlers::ExceptionHandler);
    // HTTP custom error handlers.
    Spark.notFound(HttpErrorHandlers::Http404);
    Spark.internalServerError(HttpErrorHandlers::Http500);
    // setup 'after' filters
    Spark.before((request, response) -> response.type("application/json"));
    Spark.after((request, response) -> response.header(
        "access-control-allow-origin", "*"));
    //Get routes
    Spark.get(configuration.getBaseUrl() + Endpoints.NETWORKS,
        (request, response) -> RouteHandlers.retrieveNetworks(request, response, stationRefRepo),
        new JsonTransformer());
    Spark.get(configuration.getBaseUrl() + Endpoints.NETWORKS + "/id/:id",
        (request, response) -> RouteHandlers.retrieveNetworksById(request, response, stationRefRepo),
        new JsonTransformer());
    Spark.get(configuration.getBaseUrl() + Endpoints.NETWORKS + "/name/:name",
        (request, response) -> RouteHandlers.retrieveNetworksByName(request, response, stationRefRepo),
        new JsonTransformer());
    Spark.get(configuration.getBaseUrl() + Endpoints.STATIONS,
        (request, response) -> RouteHandlers.retrieveStations(request, response, stationRefRepo),
        new JsonTransformer());
    Spark.get(configuration.getBaseUrl() + Endpoints.STATIONS + "/id/:id",
        (request, response) -> RouteHandlers.retrieveStationsById(request, response, stationRefRepo),
        new JsonTransformer());
    Spark.get(configuration.getBaseUrl() + Endpoints.STATIONS + "/name/:name",
        (request, response) -> RouteHandlers.retrieveStationsByName(request, response, stationRefRepo),
        new JsonTransformer());
    Spark.get(configuration.getBaseUrl() + Endpoints.SITES,
        (request, response) -> RouteHandlers.retrieveSites(request, response, stationRefRepo),
        new JsonTransformer());
    Spark.get(configuration.getBaseUrl() + Endpoints.SITES + "/id/:id",
        (request, response) -> RouteHandlers.retrieveSitesById(request, response, stationRefRepo),
        new JsonTransformer());
    Spark.get(configuration.getBaseUrl() + Endpoints.SITES + "/name/:name",
        (request, response) -> RouteHandlers.retrieveSitesByName(request, response, stationRefRepo),
        new JsonTransformer());
    Spark.get(configuration.getBaseUrl() + Endpoints.DIGITIZERS,
        (request, response) -> RouteHandlers.retrieveDigitizers(request, response, stationRefRepo),
        new JsonTransformer());
    Spark.get(configuration.getBaseUrl() + Endpoints.DIGITIZERS + "/id/:id",
        (request, response) -> RouteHandlers.retrieveDigitizersById(request, response, stationRefRepo),
        new JsonTransformer());
    Spark.get(configuration.getBaseUrl() + Endpoints.DIGITIZERS + "/name/:name",
        (request, response) -> RouteHandlers.retrieveDigitizersByName(request, response, stationRefRepo),
        new JsonTransformer());
    Spark.get(configuration.getBaseUrl() + Endpoints.CHANNELS,
        (request, response) -> RouteHandlers.retrieveChannels(request, response, stationRefRepo),
        new JsonTransformer());
    Spark.get(configuration.getBaseUrl() + Endpoints.CHANNELS + "/id/:id",
        (request, response) -> RouteHandlers.retrieveChannelsById(request, response, stationRefRepo),
        new JsonTransformer());
    Spark.get(configuration.getBaseUrl() + Endpoints.CHANNELS + "/name/:name",
        (request, response) -> RouteHandlers.retrieveChannelsByName(request, response, stationRefRepo),
        new JsonTransformer());
    Spark.get(configuration.getBaseUrl() + Endpoints.CALIBRATIONS,
        (request, response) -> RouteHandlers.retrieveCalibrations(request, response, stationRefRepo),
        new JsonTransformer());
    Spark.get(configuration.getBaseUrl() + Endpoints.SENSORS,
        (request, response) -> RouteHandlers.retrieveSensors(request, response, stationRefRepo),
        new JsonTransformer());
    Spark.get(configuration.getBaseUrl() + Endpoints.RESPONSES,
        (request, response) -> RouteHandlers.retrieveResponses(request, response, stationRefRepo),
        new JsonTransformer());
    Spark.get(configuration.getBaseUrl() + Endpoints.NETWORK_MEMBERSHIPS,
        (request, response) -> RouteHandlers.retrieveNetworkMemberships(request, response, stationRefRepo),
        new JsonTransformer());
    Spark.get(configuration.getBaseUrl() + Endpoints.STATION_MEMBERSHIPS,
        (request, response) -> RouteHandlers.retrieveStationMemberships(request, response, stationRefRepo),
        new JsonTransformer());
    Spark.get(configuration.getBaseUrl() + Endpoints.SITE_MEMBERSHIPS,
        (request, response) -> RouteHandlers.retrieveSiteMemberships(request, response, stationRefRepo),
        new JsonTransformer());
    Spark.get(configuration.getBaseUrl() + Endpoints.DIGITIZER_MEMBERSHIPS,
        (request, response) -> RouteHandlers.retrieveDigitizerMemberships(request, response, stationRefRepo),
        new JsonTransformer());

    Spark.get(configuration.getBaseUrl() + "alive", RouteHandlers::alive);
  }
}

package gms.core.signalenhancement.waveformfiltering;

import gms.core.signalenhancement.waveformfiltering.control.FilterControl;
import gms.core.signalenhancement.waveformfiltering.http.ContentType;
import gms.core.signalenhancement.waveformfiltering.http.ErrorHandler;
import gms.core.signalenhancement.waveformfiltering.http.FilterControlRouteHandler;
import gms.core.signalenhancement.waveformfiltering.http.StandardResponse;
import gms.core.signalenhancement.waveformfiltering.http.configuration.HttpServiceConfiguration;
import gms.core.signalenhancement.waveformfiltering.http.configuration.HttpServiceConfigurationLoader;
import gms.core.signalenhancement.waveformfiltering.osdgateway.client.HttpClientConfigurationLoader;
import gms.core.signalenhancement.waveformfiltering.osdgateway.client.OsdGatewayClient;
import gms.core.signalenhancement.waveformfiltering.plugin.FilterControlPluginRegistry;
import gms.core.signalenhancement.waveformfiltering.plugin.FilterPlugin;
import java.net.URL;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.ExceptionHandler;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

/**
 * HTTP Controller application for {@link FilterControl}
 */
public class Application {

  private static Logger logger = LoggerFactory.getLogger(Application.class);

  /**
   * Path suffix for streaming invocation of {@link FilterControl}. {@link HttpServiceConfiguration}
   * provides the prefix.
   */
  private static final String streamingPath = "/streaming";

  /**
   * Path suffix for claim check invocation of {@link FilterControl}.  {@link
   * HttpServiceConfiguration} provides the prefix.
   */
  private static final String claimCheckPath = "/claimCheck";

  public static void main(String[] args) {
    Runtime.getRuntime().addShutdownHook(new Thread(Application::stop));
    Application.start();
  }

  private Application() {
  }

  /**
   * Starts the Spark HTTP server and registers routes
   */
  private static void start() {
    logger.info("Initializing waveform-filtering-control service...");
    final String propertiesFile = "gms/core/signalenhancement/waveformfiltering/http/application.properties";
    HttpServiceConfiguration config = HttpServiceConfigurationLoader
        .load(getUrlToResourceFile(propertiesFile));

    logger.info("Initializing embedded server...");
    configureServer(config);
    logger.info("Embedded server initialized.");

    logger.info("Initializing waveform-filtering-control routes...");
    configureRoutes(config);
    logger.info("waveform-filtering-control routes initialized.");

    Spark.awaitInitialization();
    logger.info("Service initialized, ready to accept requests");
  }

  /**
   * Stops the Spark HTTP server
   */
  private static void stop() {
    Spark.stop();
  }

  /**
   * Configures the Spark HTTP server (ports, thread pool, exception handler)
   */
  private static void configureServer(HttpServiceConfiguration config) {
    Spark.port(config.getPort());
    Spark.threadPool(config.getMaxThreads(), config.getMinThreads(), config.getIdleTimeOutMillis());

    // Exception handlers.  Replicate calls to exception to avoid unchecked assignment warnings
    Spark.exception(Exception.class, exception(ErrorHandler::handle500));
    Spark.exception(IllegalArgumentException.class, exception(ErrorHandler::handle500));
    Spark.exception(NullPointerException.class, exception(ErrorHandler::handle500));

    // Error handlers
    Spark.notFound(route(r -> ErrorHandler.handle404(r.url())));
    Spark.internalServerError(route(r -> ErrorHandler.handle500("")));
  }

  /**
   * Obtains an {@link ExceptionHandler} using exceptionToString to extract a String from an {@link
   * Exception} and the exceptionHandler to create a {@link StandardResponse} from the String.
   *
   * @param exceptionHandler function creating a StandardResponse from a String, not null
   * @return ExceptionHandler, not null
   */
  private static <U extends Exception> ExceptionHandler<U> exception(
      Function<String, StandardResponse> exceptionHandler) {

    // Fills in a Response from a StandardResponse
    final BiConsumer<StandardResponse, Response> fillResponseFromStdRes = (stdRes, res) -> {
      res.status(stdRes.getHttpStatus());
      res.type(stdRes.getContentType().toString());
      res.body(stdRes.getResponseBody().toString());
    };

    // Creates a StandardResponse from an Exception
    final Function<Exception, StandardResponse> exceptionToStdRes = e ->
        exceptionHandler.apply(Application.messageOrEmpty(e));

    // Create the ExceptionHandler
    return (e, req, res) -> fillResponseFromStdRes.accept(exceptionToStdRes.apply(e), res);
  }

  /**
   * Obtains the {@link Exception#getMessage()} for exception or the empty string if it does
   * not have a message
   *
   * @return String, possibly empty, not null
   */
  private static String messageOrEmpty(Exception exception) {
    return Optional.ofNullable(exception.getMessage()).orElse("");
  }

  /**
   * Configures the waveform-filter-control HTTP routes
   */
  private static void configureRoutes(HttpServiceConfiguration config) {
    // Delegate calls to this route handler
    FilterControlRouteHandler filterControlRouteHandler = getFilterControlRouteHandler();

    // Configure the streaming route handler
    Function<Request, StandardResponse> streamingClosure = r -> filterControlRouteHandler
        .streaming(parseContentType(r), r.bodyAsBytes(), parseAcceptType(r));
    Spark.post(config.getBaseUrl() + streamingPath, route(streamingClosure));

    // Configure the claim check route handler
    Function<Request, StandardResponse> claimCheckClosure = r -> filterControlRouteHandler
        .claimCheck(parseContentType(r), r.bodyAsBytes(), parseAcceptType(r));
    Spark.post(config.getBaseUrl() + claimCheckPath, route(claimCheckClosure));
  }

  /**
   * Obtain a {@link Route} from the routeOp function mapping a {@link Request} to a {@link
   * StandardResponse}.
   *
   * @param routeOp function mapping a {@link Request} to a {@link StandardResponse}, not null
   * @return Route, not null
   */
  private static Route route(Function<Request, StandardResponse> routeOp) {

    // Fills in a Response from a StandardResponse
    final BiFunction<Response, StandardResponse, Object> responseHandler = (res, stdRes) -> {
      res.status(stdRes.getHttpStatus());
      res.type(stdRes.getContentType().toString());
      return stdRes.getResponseBody();
    };

    // Create a Route from a function mapping a Request to a StandardResponse
    final Function<Function<Request, StandardResponse>, Route> routeClosure =
        f -> (req, res) -> responseHandler.apply(res, f.apply(req));

    return routeClosure.apply(routeOp);
  }

  /**
   * Obtains the content type of the {@link Request}'s body
   *
   * @param request Request, not null
   * @return {@link ContentType}, not null
   */
  private static ContentType parseContentType(Request request) {
    return ContentType.parse(request.headers("Content-Type"));
  }

  /**
   * Obtains the clients expected body type
   *
   * @param request Request, not null
   * @return {@link ContentType}, not null
   */
  private static ContentType parseAcceptType(Request request) {
    return ContentType.parse(request.headers("Accept"));
  }

  /**
   * Obtains a fully constructed instance of {@link FilterControlRouteHandler}
   *
   * @return FilterControlRouteHandler, not null
   */
  private static FilterControlRouteHandler getFilterControlRouteHandler() {
    return FilterControlRouteHandler.create(getFilterControl());
  }

  /**
   * Obtains a fully constructed instance of {@link FilterControl}
   *
   * @return FilterControl, not null
   */
  private static FilterControl getFilterControl() {
    FilterControl filterControl = FilterControl
        .create(getFilterControlPluginRegistry(), getOsdGatewayClient());
    filterControl.initialize();

    return filterControl;
  }

  /**
   * Obtains a {@link FilterControlPluginRegistry} populated with available {@link
   * gms.core.signalenhancement.waveformfiltering.plugin.FilterPlugin}s
   *
   * @return FilterControlPluginRegistry, not null
   */
  private static FilterControlPluginRegistry getFilterControlPluginRegistry() {
    FilterControlPluginRegistry registry = new FilterControlPluginRegistry();

    // TODO: use module system for plugin discovery?
    logger.info("Registering {} plugins",
        ServiceLoader.load(FilterPlugin.class).stream().count());
    ServiceLoader.load(FilterPlugin.class).stream()
        .map(Provider::get)
        .forEach(registry::register);
    return registry;
  }

  /**
   * Obtains a fully constructed instance of {@link OsdGatewayClient}
   *
   * @return OsdGatewayClient, not null
   */
  private static OsdGatewayClient getOsdGatewayClient() {
    // Get URL to the .properties file
    final String path = "gms/core/signalenhancement/waveformfiltering/osdgateway/client/osdGateway.properties";
    final URL propFileUrl = getUrlToResourceFile(path);

    // Construct the OsdGatewayClient with OSD Gatway service access configuration
    return OsdGatewayClient
        .create(HttpClientConfigurationLoader.load("gateway_", propFileUrl));
  }

  /**
   * Obtains a {@link URL} to the file at the provided path within this Jar's resources directory
   *
   * @param path String file path relative to this Jar's root, not null
   * @return URL to the resources file at the provided path
   */
  private static URL getUrlToResourceFile(String path) {
    final URL propFileUrl = Thread.currentThread().getContextClassLoader().getResource(path);
    if (null == propFileUrl) {
      final String message = "filter-control application can't find file in resources: " + path;
      logger.error(message);
      throw new MissingResourceException(message, Application.class.getName(), path);
    }
    return propFileUrl;
  }
}

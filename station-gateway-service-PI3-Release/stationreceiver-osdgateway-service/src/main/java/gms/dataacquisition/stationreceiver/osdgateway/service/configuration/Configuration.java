package gms.dataacquisition.stationreceiver.osdgateway.service.configuration;

import java.util.Optional;
import java.util.function.BiPredicate;

/**
 * Configuration class for the service. Set ports values, minThreads, maxThreads, idleTimeOutMillis
 * and adds constructor to help with testing
 */
public class Configuration {

  static final int DEFAULT_PORT = 8080;
  static final int DEFAULT_MIN_THREADS = 2;
  static final int DEFAULT_MAX_THREADS = 10;
  static final int DEFAULT_IDLE_TIMEOUT_MILLIS = 30000;
  static final String DEFAULT_PERSISTENCE_URL = "jdbc:postgresql://localhost:5432/xmp_metadata";

  public final Optional<String> persistenceUrl;
  public final int port;
  public final int minThreads;
  public final int maxThreads;
  public final int idleTimeOutMillis;

  private Configuration(String persistenceUrl, int port, int minThreads,
      int maxThreads, int idleTimeOutMillis) {

    this.persistenceUrl = Optional.ofNullable(persistenceUrl);
    this.port = port;
    this.minThreads = minThreads;
    this.maxThreads = maxThreads;
    this.idleTimeOutMillis = idleTimeOutMillis;
  }

  /**
   * Obtains an instance of {@link Builder}
   *
   * @return a Builder, not null
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Constructs instances of {@link Configuration} following the builder pattern
   */
  public static class Builder {

    private String persistenceUrl;
    private int port;
    private int minThreads;
    private int maxThreads;
    private int idleTimeOutMillis;

    private Builder() {
      this.persistenceUrl = DEFAULT_PERSISTENCE_URL;
      this.port = DEFAULT_PORT;
      this.minThreads = DEFAULT_MIN_THREADS;
      this.maxThreads = DEFAULT_MAX_THREADS;
      this.idleTimeOutMillis = DEFAULT_IDLE_TIMEOUT_MILLIS;
    }

    /**
     * Construct the {@link Configuration}
     *
     * @return Configuration built from this {@link Builder}, not null
     * @throws IllegalArgumentException if minThreads, maxThreads, idleTimeOutMillis, or port are
     * negative; if minThreads is greater than maxThreads; if port is beyond the valid range
     */
    public Configuration build() {

      // Passes if a is strictly less than b
      final BiPredicate<Integer, Integer> lessThan = (a, b) -> Integer.compare(a, b) < 0;

      validateParameter(lessThan, port, 0,
          "Configuration must use a port between 0 and 65535");
      validateParameter(lessThan, 65536, port,
          "Configuration must use a port between 0 and 65535");
      validateParameter(lessThan, minThreads, 1,
          "Configuration minThreads must be >= 1");
      validateParameter(lessThan, maxThreads, 1,
          "Configuration maxThreads must be >= 1");
      validateParameter(lessThan, idleTimeOutMillis, 1,
          "Configuration idleTimeOutMillis must be >= 1");
      validateParameter(lessThan, maxThreads, minThreads,
          "Configuration cannot have maxThreads less than minThreads");

      return new Configuration(persistenceUrl, port,
          minThreads, maxThreads, idleTimeOutMillis);
    }

    private static <A, B> void validateParameter(BiPredicate<A, B> test, A a, B b, String message) {
      if (test.test(a, b)) {
        throw new IllegalArgumentException(message);
      }
    }

    /**
     * Sets the URL used to access the persistence implementation.  Null is allowed.
     *
     * @param persistenceUrl URL to the persistence implementation
     * @return this Builder
     */
    public Builder setPersistenceUrl(String persistenceUrl) {
      this.persistenceUrl = persistenceUrl;
      return this;
    }

    /**
     * Set the service's port
     *
     * @param port service's port
     * @return this {@link Builder}
     */
    public Builder setPort(int port) {
      this.port = port;
      return this;
    }

    /**
     * Set the service's minimum number of threads
     *
     * @param minThreads service's minimum number of threads
     * @return this {@link Builder}
     */
    public Builder setMinThreads(int minThreads) {
      this.minThreads = minThreads;
      return this;
    }

    /**
     * Set the service's maximum number of threads
     *
     * @param maxThreads service's maximum number of threads
     * @return this {@link Builder}
     */
    public Builder setMaxThreads(int maxThreads) {
      this.maxThreads = maxThreads;
      return this;
    }

    /**
     * Set the service's idle timeout in milliseconds
     *
     * @param idleTimeOutMillis service's idle timeout in milliseconds
     * @return this {@link Builder}
     */
    public Builder setIdleTimeOutMillis(int idleTimeOutMillis) {
      this.idleTimeOutMillis = idleTimeOutMillis;
      return this;
    }
  }
}


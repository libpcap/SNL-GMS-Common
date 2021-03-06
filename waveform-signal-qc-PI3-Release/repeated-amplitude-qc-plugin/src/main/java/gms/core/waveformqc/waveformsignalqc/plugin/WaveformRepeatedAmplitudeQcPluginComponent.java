package gms.core.waveformqc.waveformsignalqc.plugin;

import gms.core.waveformqc.waveformqccontrol.objects.PluginVersion;
import gms.core.waveformqc.waveformqccontrol.objects.WaveformQcChannelSohStatus;
import gms.core.waveformqc.waveformqccontrol.plugin.PluginConfiguration;
import gms.core.waveformqc.waveformqccontrol.plugin.WaveformQcPlugin;
import gms.core.waveformqc.waveformsignalqc.algorithm.WaveformRepeatedAmplitudeInterpreter;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A component wrapping a {@link WaveformRepeatedAmplitudeQcPlugin} by implementing the {@link
 * WaveformQcPlugin} interface. Provides a name and version number but defers logic to the
 * WaveformGapQcPlugin.
 */
public class WaveformRepeatedAmplitudeQcPluginComponent implements WaveformQcPlugin {

  private static final Logger logger = LoggerFactory
      .getLogger(WaveformRepeatedAmplitudeQcPluginComponent.class);

  private static final String PLUGIN_NAME = "waveformRepeatedAmplitudeQcPlugin";

  private static final PluginVersion pluginVersion = PluginVersion.from(1, 0, 0);

  private WaveformRepeatedAmplitudeQcPlugin plugin = null;

  /**
   * Obtains this plugin component's name
   *
   * @return String, not null
   */
  @Override
  public String getName() {
    return PLUGIN_NAME;
  }

  /**
   * Obtains this plugin component's verion number
   *
   * @return {@link PluginVersion}, not null
   */
  @Override
  public PluginVersion getVersion() {
    return pluginVersion;
  }

  /**
   * Initialize using the provided {@link PluginConfiguration}.  Creates the wrapped {@link
   * WaveformRepeatedAmplitudeQcPlugin}.  Must only be called once and must be called before the
   * first call to {@link WaveformRepeatedAmplitudeQcPluginComponent#generateQcMasks(Collection,
   * Collection, Collection, UUID)}
   *
   * @param pluginConfiguration generic PluginConfiguration, not null
   * @throws NullPointerException if pluginConfiguration is null
   * @throws IllegalStateException if this operation is called more than once
   */
  @Override
  public void initialize(PluginConfiguration pluginConfiguration) {
    Objects.requireNonNull(pluginConfiguration,
        "WaveformRepeatedAmplitudeQcPluginComponent cannot be initialized with null PluginConfiguration");

    if (this.plugin != null) {
      throw new IllegalStateException(
          "WaveformRepeatedAmplitudeQcPluginComponent cannot be initialized twice");
    }

    this.plugin = WaveformRepeatedAmplitudeQcPlugin.create(
        WaveformRepeatedAmplitudeQcPluginConfiguration.from(pluginConfiguration),
        new WaveformRepeatedAmplitudeInterpreter());
  }

  /**
   * Uses the wrapped {@link WaveformRepeatedAmplitudeQcPlugin} to create gap {@link QcMask}s. Must
   * only be called after {@link WaveformRepeatedAmplitudeQcPluginComponent#initialize(PluginConfiguration)}
   *
   * @param channelSegments {@link ChannelSegment}s to check for repeated adjacent amplitudes, not
   * null
   * @param waveformQcChannelSohStatuses {@link WaveformQcChannelSohStatus}, not null
   * @param existingQcMasks Previously created {@link QcMask}s that can affect processing, not null
   * @param creationInfoId {@link UUID} to a {@link gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo}
   * to associate with new QcMasks, not null
   * @return Stream of new or updated QcMasks, not null
   * @throws NullPointerException if channelSegments, waveformQcChannelSohStatuses, existingQcMasks,
   * or creationInfoId are null
   * @throws IllegalStateException if this operation is called prior to {@link
   * WaveformRepeatedAmplitudeQcPluginComponent#initialize(PluginConfiguration)}
   */
  @Override
  public Stream<QcMask> generateQcMasks(Collection<ChannelSegment> channelSegments,
      Collection<WaveformQcChannelSohStatus> waveformQcChannelSohStatuses,
      Collection<QcMask> existingQcMasks, UUID creationInfoId) {

    Objects.requireNonNull(channelSegments,
        "WaveformRepeatedAmplitudeQcPluginComponent cannot generateQcMasks with null channelSegments");
    Objects.requireNonNull(waveformQcChannelSohStatuses,
        "WaveformRepeatedAmplitudeQcPluginComponent cannot generateQcMasks with null waveformQcChannelSohStatuses");
    Objects.requireNonNull(existingQcMasks,
        "WaveformRepeatedAmplitudeQcPluginComponent cannot generateQcMasks with null existingQcMasks");
    Objects.requireNonNull(creationInfoId,
        "WaveformRepeatedAmplitudeQcPluginComponent cannot generateQcMasks with null creationInfoId");

    if (this.plugin == null) {
      throw new IllegalStateException(
          "WaveformRepeatedAmplitudeQcPluginComponent cannot be used before it is initialized");
    }

    return plugin.createQcMasks(channelSegments, existingQcMasks, creationInfoId);
  }
}

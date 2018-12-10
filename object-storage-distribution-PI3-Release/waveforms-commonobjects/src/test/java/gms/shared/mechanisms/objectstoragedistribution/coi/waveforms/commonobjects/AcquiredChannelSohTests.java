package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import java.lang.reflect.Constructor;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.TestFixtures;
import org.junit.Test;

/**
 * Tests {@link AcquiredChannelSoh} creation and usage semantics Created by trsault on 8/25/17.
 */
public class AcquiredChannelSohTests {

  private final AcquiredChannelSoh.AcquiredChannelSohType calib = AcquiredChannelSoh.AcquiredChannelSohType.CALIBRATION_UNDERWAY;
  private final Instant epoch = Instant.EPOCH;
  private final Instant later = epoch.plusSeconds(30);
  private final UUID processingChannelId = TestFixtures.PROCESSING_CHANNEL_1_ID;

  @Test
  public void equalsAndHashcodeTest() {
    TestUtilities.checkClassEqualsAndHashcode(AcquiredChannelSohAnalog.class);
    TestUtilities.checkClassEqualsAndHashcode(AcquiredChannelSohBoolean.class);
  }

  @Test
  public void testAcquiredChannelSohAnalogCreateNullParameters() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
        AcquiredChannelSohAnalog.class, "create",
        processingChannelId, calib, epoch, later, 0.0,
        CreationInfo.DEFAULT);
  }

  @Test
  public void testAcquiredChannelSohAnalogFromNullParameters() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
        AcquiredChannelSohAnalog.class, "from",
        TestFixtures.CHANNEL_SEGMENT_ID, processingChannelId,
        calib, epoch, later, 0.0,
        CreationInfo.DEFAULT);
  }

  @Test
  public void testAcquiredChannelSohBooleanCreateNullParameters() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
        AcquiredChannelSohBoolean.class, "create",
        processingChannelId, calib, epoch, later,
        false, CreationInfo.DEFAULT);
  }

  @Test
  public void testAcquiredChannelSohBooleanFromNullParameters() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
        AcquiredChannelSohBoolean.class, "from",
        TestFixtures.SOH_ANALOG_ID, processingChannelId,
        calib, epoch, later,
        false, CreationInfo.DEFAULT);
  }
}

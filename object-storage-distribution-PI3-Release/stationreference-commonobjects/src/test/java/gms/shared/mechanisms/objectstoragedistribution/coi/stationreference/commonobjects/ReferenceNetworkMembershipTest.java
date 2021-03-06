package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import java.time.Instant;
import java.util.UUID;
import org.junit.Test;

public class ReferenceNetworkMembershipTest {
  private final UUID id = UUID.fromString("1712f988-ff83-4f3d-a832-a82a040221d9");
  private final UUID networkId = UUID.fromString("6712f988-ff83-4f3d-a832-a82a04022123");
  private final UUID stationId = UUID.fromString("9812f988-ff83-4f3d-a832-a82a04022154");
  private final String comment = "Question everything.";
  private final Instant actualTime = Instant.now().minusSeconds(100);
  private final Instant systemTime = Instant.now();
  private final StatusType status = StatusType.ACTIVE;


  @Test
  public void equalsAndHashcodeTest() {
    TestUtilities.checkClassEqualsAndHashcode(ReferenceNetworkMembership.class);
  }

  @Test
  public void testReferenceNetworkMembCreateNullArguments() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
        ReferenceNetworkMembership.class, "create", comment, actualTime,
        networkId, stationId, status);
  }

  @Test
  public void testReferenceNetworkMembFromNullArguments() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
        ReferenceNetworkMembership.class, "from", id, comment, actualTime, systemTime,
      networkId, stationId, status);

  }
    /**
     * Test that arguments are saved correctly.
     * @throws Exception
     */
    @Test
    public void testReferenceNetworkMembCreate() {
      ReferenceNetworkMembership alias = ReferenceNetworkMembership.create(comment, actualTime,
           networkId, stationId, status);
      assertNotEquals(id, alias.getId());
      assertEquals(comment, alias.getComment());
      assertEquals(actualTime, alias.getActualChangeTime());
      assertNotEquals(systemTime, alias.getSystemChangeTime());
      assertEquals(networkId, alias.getNetworkId());
      assertEquals(stationId, alias.getStationId());
      assertEquals(status, alias.getStatus());
    }

    /**
     * Test that arguments are saved correctly.  We check that the name was
     * converted to uppercase.
     * @throws Exception
     */
    @Test
    public void testReferenceNetworkMembFrom() {
      ReferenceNetworkMembership alias = ReferenceNetworkMembership.from(id, comment, actualTime,
          systemTime, networkId, stationId, status);
      assertEquals(id, alias.getId());
      assertEquals(comment, alias.getComment());
      assertEquals(actualTime, alias.getActualChangeTime());
      assertEquals(systemTime, alias.getSystemChangeTime());
      assertEquals(networkId, alias.getNetworkId());
      assertEquals(stationId, alias.getStationId());
      assertEquals(status, alias.getStatus());
    }



}

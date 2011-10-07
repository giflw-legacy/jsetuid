package jsetuid;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import junit.framework.Assert;
import org.junit.Test;

public class SetUIDTest {

	private static final int GID = 20;
	private static final int UID = 501;
	private static final int UMASK = 0127; // -rw-r-----

	@Test
	public void testGetUID() throws Exception {

		// retrieve GID
		final int gid1 = SetUID.getgid();
		System.out.printf("gid: %s%n", gid1);
		Assert.assertTrue("gid must be >= 0", gid1 >= 0);

		// retrieve UID
		final int uid1 = SetUID.getuid();
		System.out.printf("uid: %s%n", uid1);
		Assert.assertTrue("uid must be >= 0", uid1 >= 0);

		System.out.println();

		// change GID, retrieve new value
		// remember to do this before changing the UID
		Assert.assertEquals("setgid returned error", 0, SetUID.setgid(GID));
		final int gid2 = SetUID.getgid();
		System.out.printf("gid: %s%n", gid2);
		Assert.assertTrue("gid must be > 0", gid2 > 0);

		// change UID, retrieve new value
		Assert.assertEquals("setuid returned error", 0, SetUID.setuid(UID));
		final int uid2 = SetUID.getuid();
		System.out.printf("uid: %s%n", uid2);
		Assert.assertTrue("uid must be > 0", uid2 > 0);

		// set future file permissions
		SetUID.setumask(UMASK);

		// prove that file is writable aftert setuid
		final File f = new File("setuid-" + System.currentTimeMillis() + ".tmp");
		final OutputStream out = new FileOutputStream(f);
		out.write(f.getAbsolutePath().getBytes());
		out.close();
		// f.delete();

	}

}

package jsetuid;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import junit.framework.Assert;
import org.jruby.ext.posix.FileStat;
import org.jruby.ext.posix.POSIX;
import org.junit.Test;

public class SetUIDTest {

	private static final int UMASK = 0127; // -rw-r-----

	@Test
	public void testPosix() throws Exception {

		System.out.println();
		System.out.println("********** jna-posix **********");
		System.out.println();

		final POSIX posix = PosixUtil.current();

		// take the uid and gid from the owner of the current directory
		final FileStat st = posix.lstat(System.getProperty("user.dir"));
		final int GID = st.gid();
		final int UID = st.uid();

		// retrieve GID
		final int gid1 = posix.getgid();
		System.out.printf("gid: %s%n", gid1);
		Assert.assertTrue("gid must be >= 0", gid1 >= 0);

		// retrieve UID
		final int uid1 = posix.getuid();
		System.out.printf("uid: %s%n", uid1);
		Assert.assertTrue("uid must be >= 0", uid1 >= 0);

		System.out.println();

		// change GID, retrieve new value
		// remember to do this before changing the UID
		Assert.assertEquals("setgid returned error", 0, posix.setgid(GID));
		final int gid2 = posix.getgid();
		System.out.printf("gid: %s%n", gid2);
		Assert.assertTrue("gid must be > 0", gid2 > 0);

		// change UID, retrieve new value
		Assert.assertEquals("setuid returned error", 0, posix.setuid(UID));
		final int uid2 = posix.getuid();
		System.out.printf("uid: %s%n", uid2);
		Assert.assertTrue("uid must be > 0", uid2 > 0);

		// set future file permissions
		posix.umask(UMASK);

		// prove that file is writable aftert setuid
		final File f = new File("setuid-" + System.currentTimeMillis() + ".tmp");
		final OutputStream out = new FileOutputStream(f);
		out.write(f.getAbsolutePath().getBytes());
		out.close();
		// f.delete();

	}

}

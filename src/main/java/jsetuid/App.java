package jsetuid;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;

import org.jruby.ext.posix.FileStat;
import org.jruby.ext.posix.POSIX;

public class App {

	public static void main(final String[] args) throws Exception {

		System.out.println();
		System.out.println("Drop privileges test program.");
		System.out.println();

		final POSIX posix = PosixUtil.current();

		// retrieve UID
		final int rootUid = posix.getuid();
		assertTrue("Must be run as root. Aborting.", rootUid == 0);

		// retrieve GID
		final int rootGid = posix.getgid();
		assertTrue("Group ID must be 0", rootGid == 0);

		// create a file owned by root and accessible to only the owner
		posix.umask(0177); // -rw-------
		final File ownedByRoot = makeFile();

		// assert that file owner and group are equal to current user and group
		final FileStat stOwnedByRoot = posix.lstat(ownedByRoot.getPath());
		assertEquals("root file owner must be 0", 0, stOwnedByRoot.uid());
		// assertEquals("root file group must be 0", 0, stOwnedByRoot.gid());

		// open up a server socket on a privileged port
		final ServerSocket rootSocket = new ServerSocket(1);
		rootSocket.close();

		System.out.println();

		// discover a valid user on the system
		// take the uid and gid from the owner of the current directory
		final FileStat st = posix.lstat(System.getProperty("user.dir"));
		final int normalUserUid = st.uid();
		assertTrue("normal user id must be > 0", normalUserUid > 0);
		final int normalUserGid = st.gid();
		assertTrue("normal user group id must be > 0", normalUserGid > 0);

		// change identity from root to normal user
		// remember to change group-id before user-id
		assertEquals("setgid returned error", 0, posix.setgid(normalUserGid));
		assertEquals("setuid returned error", 0, posix.setuid(normalUserUid));

		// create a file owned by normal user
		posix.umask(0122); // -rw-r--r--
		final File ownedByNormalUser = makeFile();
		// assert that file owner and group are equal to current user and group
		final FileStat stOwnedByNormalUser = posix.lstat(ownedByNormalUser.getPath());
		assertEquals("bad normal file owner", normalUserUid, stOwnedByNormalUser.uid());
		assertEquals("bad normal file group", normalUserGid, stOwnedByNormalUser.gid());

		// try to access contents of file owned by root, expect error
		assertTrue("normal user access to root-only file", !ownedByRoot.canRead());
		try {
			loadFile(ownedByRoot);
			throw new AssertionError("normal user access to root-only file");
		} catch (final Exception x) {
			System.out.println("Good! Denied access to root-only file. " + x.toString());
		}

		// open up a server socket on a privileged port, expect error
		try {
			final ServerSocket userSocket = new ServerSocket(1);
			userSocket.close();
			throw new AssertionError("normal user access to privileged port");
		} catch (final Exception x) {
			System.out.println("Good! Denied access to privileged port. " + x.toString());
		}

	}

	private static void assertEquals(final String msg, final int v1, final int v2) {
		if (v1 != v2)
			throw new AssertionError(msg + " -- v1: " + v1 + ", v2: " + v2);
	}

	private static void assertTrue(final String msg, final boolean value) {
		if (!value)
			throw new AssertionError(msg);
	}

	private static byte[] loadFile(final File f) throws IOException {
		final byte[] contents = new byte[(int) f.length()];
		final InputStream in = new FileInputStream(f);
		in.read(contents);
		in.close();
		return contents;
	}

	private static File makeFile() throws IOException {
		final File f = new File("setuid-" + System.nanoTime() + ".tmp");
		final OutputStream out = new FileOutputStream(f);
		out.write(f.getAbsolutePath().getBytes());
		out.close();
		// f.delete();
		return f;
	}

}

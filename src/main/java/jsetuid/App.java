package jsetuid;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jruby.ext.posix.FileStat;
import org.jruby.ext.posix.POSIX;

public class App {

	public static class UserGroup {
		public final int gid;
		public final int uid;

		public UserGroup(final int uid, final int gid) {
			this.uid = uid;
			this.gid = gid;
		}
	}

	private static POSIX posix = null;

	public static void main(final String[] args) throws Exception {

		System.out.println();
		System.out.println("Drop privileges test program.");
		System.out.println();

		final int privilegedPort = 1;

		posix = PosixUtil.current();
		posix.umask(0177); // -rw-------

		// confirm current user is root
		assertRoot(getCurrentUser());
		System.out.println("User is root.");

		// create file accessible only to root
		final File rootFile = makeFile();
		// assertRoot(getOwnerOfFile(rootFile));
		assertUserIsRoot(getOwnerOfFile(rootFile));
		System.out.println("File created is owned by root.");

		// discover uid/gid of normal user on system
		final UserGroup normalUser = getOwnerOfCurrentWorkingDirectory();
		assertNotRoot(normalUser);
		System.out.println("Normal user is not root.");

		// open server socket on privileged port
		final ServerSocket rootSocket = new ServerSocket(privilegedPort);
		System.out.println("Opened server socket on privileged port.");

		// change to normal user
		dropPrivilegesToUser(normalUser);
		System.out.println("Dropped privileges to normal user.");

		// check that new user cannot access root-only file
		assertFileInaccessible(rootFile);
		System.out.println("Normal user cannot read root-only file.");

		// create file as normal user
		assertNotRoot(getOwnerOfFile(makeFile()));
		System.out.println("File created is owned by normal user.");

		// test that server socket can accept connections as normal user
		testServerSocket(rootSocket, privilegedPort);

	}

	private static void assertEquals(final String msg, final int v1, final int v2) {
		if (v1 != v2)
			throw new AssertionError(msg + " -- v1: " + v1 + ", v2: " + v2);
	}

	private static void assertFileInaccessible(final File f) {

		if (f.canRead())
			throw new AssertionError("file can be read");

		try {
			loadFile(f);
			throw new AssertionError("file is accessible");
		} catch (final Exception x) {
			// ignore, expected
		}

	}

	private static void assertNotRoot(final UserGroup u) {
		assertTrue("uid is 0", u.uid > 0);
		assertTrue("gid is 0", u.gid > 0);
	}

	private static void assertRoot(final UserGroup u) {
		assertEquals("uid is not 0", 0, u.uid);
		assertEquals("gid is not 0", 0, u.gid);
	}

	private static void assertTrue(final String msg, final boolean value) {
		if (!value)
			throw new AssertionError(msg);
	}

	private static void assertUserIsRoot(final UserGroup u) {
		assertEquals("uid is not 0", 0, u.uid);
	}

	private static void dropPrivilegesToUser(final UserGroup u) {
		assertEquals("setgid returned error", 0, posix.setgid(u.gid));
		assertEquals("setuid returned error", 0, posix.setuid(u.uid));
	}

	private static UserGroup getCurrentUser() {
		return new UserGroup(posix.getuid(), posix.getgid());
	}

	private static UserGroup getOwnerOfCurrentWorkingDirectory() {
		return getOwnerOfFile(System.getProperty("user.dir"));
	}

	private static UserGroup getOwnerOfFile(final File f) {
		return getOwnerOfFile(f.getPath());
	}

	private static UserGroup getOwnerOfFile(final String path) {
		final FileStat st = posix.lstat(path);
		return new UserGroup(st.uid(), st.gid());
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

	private static void testServerSocket(final ServerSocket s, final int port) {

		System.out.println("Test receiving connections as normal user on privileged port.");

		try {

			final ExecutorService x = Executors.newSingleThreadExecutor();

			final CountDownLatch latch = new CountDownLatch(1);

			x.submit(new Runnable() {
				@Override
				public void run() {
					try {
						System.out.println("Server awaiting connections.");
						final Socket b = s.accept();
						System.out.println("Server received client connection.");
						final InputStream in = b.getInputStream();
						final OutputStream out = b.getOutputStream();
						in.close();
						out.close();
						b.close();
						System.out.println("Server closed client connection.");
					} catch (final IOException x) {
						x.printStackTrace();
					}
					latch.countDown();
				} // run
			});

			System.out.println("Client connecting to privileged port.");
			final Socket client = new Socket("localhost", port);
			System.out.println("Client connected to privileged port.");
			latch.await();
			client.close();
			System.out.println("Client closed connection.");

			x.shutdownNow();

		} catch (final Exception x) {
			x.printStackTrace();
		}

		System.out.println("Network tests complete.");

	}

}

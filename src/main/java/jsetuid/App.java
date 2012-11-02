package jsetuid;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Test application to demonstrate how to start a Java program as root and then to later drop its
 * permissions to that of a normal user.
 * 
 * @author karl
 * 
 */
public class App {

	private static class UserGroup {
		public final int gid;
		public final int uid;

		public UserGroup(final int uid, final int gid) {
			this.uid = uid;
			this.gid = gid;
		}
	}

	static {
		System.setProperty("java.awt.headless", Boolean.TRUE.toString());
	}

	private static final int PRIVILEGED_PORT = 1;

	public static void main(final String[] args) throws Exception {

		if (args.length == 0) {
			System.out.println("Usage: jsetuid <username>");
			return;
		}

		final Passwd passwd = LibC.getpwnam(args[0]);
		System.out.println("retrieved user info");

		assertRoot(getCurrentUser());
		System.out.println("User is root.");

		// open server socket on privileged port
		final ServerSocket rootSocket = new ServerSocket(PRIVILEGED_PORT);
		System.out.println("Opened server socket on privileged port.");

		// change to normal user
		dropPrivilegesToUser(new UserGroup(passwd.pw_uid, passwd.pw_gid));
		assertNotRoot(getCurrentUser());
		System.setProperty("user.name", passwd.pw_name);
		System.setProperty("user.home", passwd.pw_dir);
		System.out.println("Dropped privileges to normal user.");

		// create file as normal user
		LibC.umask(0177); // -rw-------
		makeFile();

		System.out.println();

		// test that server socket can accept connections as normal user
		testServerSocket(rootSocket, PRIVILEGED_PORT);

	}

	private static void assertEquals(final String msg, final int v1, final int v2) {
		if (v1 != v2)
			throw new AssertionError(msg + " -- v1: " + v1 + ", v2: " + v2);
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

	private static void dropPrivilegesToUser(final UserGroup u) {
		assertEquals("setgid returned error", 0, LibC.setgid(u.gid));
		assertEquals("setuid returned error", 0, LibC.setuid(u.uid));
	}

	private static UserGroup getCurrentUser() {
		return new UserGroup(LibC.getuid(), LibC.getgid());
	}

	private static File makeFile() throws IOException {
		final File f = new File("setuid-" + System.nanoTime() + ".tmp");
		// f.deleteOnExit();
		final OutputStream out = new FileOutputStream(f);
		out.write(f.getAbsolutePath().getBytes());
		out.close();
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

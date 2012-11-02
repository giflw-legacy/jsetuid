package jsetuid;

import com.sun.jna.Native;

public class LibC {

	static {
		Native.register("c");
	}

	public static native int getuid();

	public static native int setuid(int uid);

	public static native int getgid();

	public static native int setgid(int gid);

	public static native Passwd getpwnam(String login);

	public static native int umask(int mask);

	private LibC() {
	}

}

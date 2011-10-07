package jsetuid;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;

public class SetUID {

	public interface CLibrary extends Library {

		CLibrary INSTANCE = (CLibrary) Native.loadLibrary((Platform.isWindows() ? "msvcrt" : "c"), CLibrary.class);

		int getgid();

		int getuid();

		int setgid(int gid);

		int setuid(int uid);

		int umask(int umask);

	}

	public static final int ERROR = -1;
	public static final int OK = 0;

	public static int getgid() {
		if (Platform.isWindows())
			return ERROR;
		return CLibrary.INSTANCE.getgid();
	}

	public static int getuid() {
		if (Platform.isWindows())
			return ERROR;
		return CLibrary.INSTANCE.getuid();
	}

	public static int setgid(final int gid) {
		if (Platform.isWindows())
			return OK;
		return CLibrary.INSTANCE.setgid(gid);
	}

	public static int setuid(final int uid) {
		if (Platform.isWindows())
			return OK;
		return CLibrary.INSTANCE.setuid(uid);
	}

	public static int setumask(final int umask) {
		if (Platform.isWindows())
			return OK;
		return CLibrary.INSTANCE.umask(umask);
	}

}

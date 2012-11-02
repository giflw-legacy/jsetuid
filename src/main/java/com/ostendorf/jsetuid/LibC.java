/*
 * Copyright (C) 2012 Karl Ostendorf, http://ostendorf.com/

 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * 
 */

package com.ostendorf.jsetuid;

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

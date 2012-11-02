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

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

public class Passwd extends Structure {

	public String pw_name; /* getlogin() */
	public String pw_passwd; /* "" */
	public int pw_uid; /* getuid() */
	public int pw_gid; /* getgid() */
	public String pw_gecos; /* getlogin() */
	public String pw_dir; /* "/" or getenv("HOME") */
	public String pw_shell; /* "/bin/sh" or getenv("SHELL") */

	@Override
	@SuppressWarnings("rawtypes")
	protected List getFieldOrder() {
		return Arrays.asList(new String[] {
			"pw_name", "pw_passwd", "pw_uid", "pw_gid", "pw_gecos", "pw_dir", "pw_shell",
		});
	}

}



package com.azad.musicplayer.filters;

import java.io.*;

public class DirectoryFilter implements FileFilter {
	public boolean accept(File pathname) {
		return pathname.isDirectory();
	}
}

package com.catalogWatcher.server;

/**
 * 
 */

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class FileTreeViver {

	public FileTreeViver(String dirName) {
		this.dirName = dirName;
	}

	private String tree;
	private String dirName;
	
	private class Finder extends SimpleFileVisitor<Path> {

	    @Override
	    public FileVisitResult postVisitDirectory(Path dir,
	                                          IOException exc) {
	    	tree += dir + "\n";
	        return FileVisitResult.CONTINUE;
	    }
	    
		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
				throws IOException {
			tree += file + "\n";
			return FileVisitResult.CONTINUE;
		}

	}
	
	public String getTree() {
		tree = "";
		try {
			Files.walkFileTree(Paths.get(dirName), new Finder()); // Текущий
																	// каталог
		} catch (IOException e) {
			e.printStackTrace();
		}
		return tree;
	}

}

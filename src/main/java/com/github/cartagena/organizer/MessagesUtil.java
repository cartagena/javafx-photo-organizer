package com.github.cartagena.organizer;

class MessagesUtil {
	
	private MessagesUtil() {}

	static String success(final boolean move) {
		return move ? "Picture '%s' moved to new path '%s'" : "Picture '%s' copied to new path '%s'";
	}
	
	static String skip(final boolean move) {
		return move ? "Will not move '%s', already on destination path." : "Will not copy '%s', already on destination path.";
	}
	
	static String error(final boolean move) {
		return move ? "Failed to move picture '%s'." : "Failed to copy picture '%s'." ;
	}
	
	static String starting(final boolean move) {
		return move ? "Starting to move %s pictures." : "Starting to copy %s pictures.";
	}
	
	static String end(final boolean move) {
		return move ? "Process finished. Moved %s of %s pictures." : "Process finished. Copied %s of %s pictures.";
	}
}

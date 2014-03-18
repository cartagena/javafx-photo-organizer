package com.github.cartagena.organizer;

import static com.drew.metadata.exif.ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL;
import static java.io.File.separator;
import static java.lang.String.format;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;
import static org.apache.commons.io.FileUtils.copyFileToDirectory;
import static org.apache.commons.io.FileUtils.listFiles;
import static org.apache.commons.io.FileUtils.moveFileToDirectory;

import java.io.File;
import java.util.Calendar;
import java.util.Collection;

import lombok.extern.slf4j.Slf4j;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;

@Slf4j
public class PicturesOrganizer {

	private static int processPictures(final String sourcePath, final String destinationPath, boolean move, final PicturesOrganizerListener listener) {
		File source = new File(sourcePath);
		File destination = new File(destinationPath);
		
		Collection<File> allFiles = listFiles(source, new String[] {"JPEG", "JPG", "jpeg", "jpg"}, true);
		int total = allFiles.size();
		int processed = 0;
		int i = 0;
		
		listener.onStart(total);

		try {
            Thread.sleep(50);
        } catch (InterruptedException interrupted) {
        }
		
		for(File picture : allFiles) {
			i++;
			try {
				String toPath = extractPhotoPath(picture);
				File destDir = new File(destination, toPath);
				
				if(move) {
					moveFileToDirectory(picture, destDir, true);
				} else {
					copyFileToDirectory(picture, destDir, true);
				}
				
				listener.onProcess(i, total, destDir.getAbsolutePath(), picture.getAbsolutePath());
				processed++;
			} catch (Exception e) {
				log.info("Could not process file '{}'", picture.getAbsolutePath());
				log.debug(format("Could not processfile '%s'.", picture.getAbsolutePath()), e);
				
				listener.onProcessError(i,  total, picture.getAbsolutePath());
			}
			
			 try {
                 Thread.sleep(70);
             } catch (InterruptedException interrupted) {
             }
		}
		
		listener.onEnd(total, processed);
		
		return processed;
	}
	
	static int copyPictures(final String sourcePath, final String destinationPath, final PicturesOrganizerListener listener) {
		return processPictures(sourcePath, destinationPath, false, listener);
	}
	
	static int movePictures(final String sourcePath, final String destinationPath, final PicturesOrganizerListener listener) {
		return processPictures(sourcePath, destinationPath, true, listener);
	}
	
	public static String extractPhotoPath(final File photo) {
		try {
			Calendar takenAt = Calendar.getInstance();
			takenAt.setTimeInMillis(0);
			
			Metadata metadata = ImageMetadataReader.readMetadata(photo);
			
			if(metadata.containsDirectory(ExifSubIFDDirectory.class)) {
				ExifSubIFDDirectory directory = metadata.getDirectory(ExifSubIFDDirectory.class);
				
				if(directory.containsTag(TAG_DATETIME_ORIGINAL)) {
					takenAt.setTime(directory.getDate(TAG_DATETIME_ORIGINAL));
				}
			}
			
			return format("%s%s%02d", takenAt.get(YEAR), separator, (takenAt.get(MONTH) + 1));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	static interface PicturesOrganizerListener {
		
		void onProcess(final int current, final int total, final String newPath, final String originalPath);
		
		void onProcessError(final int current, final int total, final String originalPath);
		
		void onStart(final int total);
		
		void onEnd(final int total, final int processed);
	}

}

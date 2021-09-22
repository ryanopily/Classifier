package ml.zer0dasho.classifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Course {

	public List<String> fields;
	
	// For multiple meeting times.
	public String customDbFmt = null;
	
	// http://www.buffalo.edu/class-schedule?switch=showclass{detail}
	private String detail;
	
	public Course(List<String> fields, String detail) {
		this.fields = fields;
		this.detail = detail;
		
		cleanFields();
	}
	
	public void cleanFields() {
		// Clean up stupid HTML special encodings - this field is a number
		fields.set(0, fields.get(0).replaceAll("\\&[^;]+;", ""));
		
		// Trim whitespace and clean up encodings
		for(int i = 1; i < fields.size(); i++) {
			fields.set(i, fields.get(i).trim()
									   .replaceAll("\\&amp;", "&")
							    	   .replaceAll("\\&[^;]+;", ""));
		}

		String[] deptCourse = fields.get(1).split(" ");
		fields.set(1, deptCourse[0]);
		fields.add(2, deptCourse[1]);
		
		fields.add(9, "?");

		// Class meets in different rooms on different days and at different times - requires us to insert multiple records for the same class
		if(fields.get(7).matches("Multiple meeting patterns.") || fields.get(8).matches("See class detail")) {
			multipleMeetingPatterns(fields.get(1), fields.get(0));
		}
		
		else if(fields.get(8).matches("[^ ]+ .*")) {
			
			// Separate building from room number
			String[] buildRoom = fields.get(8).split(" ");
			String[] field8 = new String[buildRoom.length-1];
			
	        System.arraycopy(buildRoom, 0, field8, 0, field8.length);

			fields.set(8, String.join(" ", field8));
			fields.set(9, buildRoom[buildRoom.length-1]);
		}
		
		// TBA, etc.
		else
			fields.set(9, fields.get(8));
		
		for(int i = 1; i < fields.size(); i++) 
			fields.set(i, "\"" + fields.get(i) + "\"");
	}
	
	@Override
	public String toString() {
		return Arrays.toString(fields.toArray());
	}
	
	public String databaseFormat() {
		return customDbFmt == null ? "(" + String.join(", ", fields.toArray(new String[0])) + ")" : customDbFmt;
	}
	
	public void multipleMeetingPatterns(String dept, String regnum) {
		
		// Details are all the multiple meeting times - (days, time, building, room) fields
		List<String> details = new ArrayList<String>();
		details.addAll(Arrays.asList(Scraper.getCourseDetail(CLASS_DETAIL + detail)));
		
		// Clean up whitespace and encodings.
		for(int i = 0; i < details.size(); i++) {
			details.set(i, details.get(i).trim()
					   				   .replaceAll("\\&amp;", "&")
					   				   .replaceAll("\\&[^;]+;", ""));
		}
		
		for(int i = 1; i < fields.size(); i++) {
			fields.set(i, "\"" + fields.get(i) + "\"");
		}
		
		String format = "";

		while(details.size() > 0) {
			
			// Details should always come in 4, if not, we should fill in the blanks
			if(details.size() < 4) {
				String[] bloat = new String[4-details.size()];
				Arrays.fill(bloat, "?");
				details.addAll(Arrays.asList(bloat));
			}
			
			fields.set(6, details.get(0));
			fields.set(7, details.get(1));
				
			if(details.get(2).matches("[^ ]+ .*")) {
				String[] buildRoom = details.get(2).split(" ");
				String[] field8 = new String[buildRoom.length-1];
				
		        System.arraycopy(buildRoom, 0, field8, 0, field8.length);

				fields.set(8, String.join(" ", field8));
				fields.set(9, buildRoom[buildRoom.length-1]);
			}
				
			else {
				fields.set(8, details.get(2));
				fields.set(9, details.get(2));
			}
			
			details.remove(0);
			details.remove(0);
			details.remove(0);
			details.remove(0);
			
			for(int i = 6; i < 10; i++) 
				fields.set(i, "\"" + fields.get(i) + "\"");
			
			format += databaseFormat() + ",";
		}
		
		customDbFmt = format.substring(0,format.length()-1);
	}
	
	private static String CLASS_DETAIL = "http://www.buffalo.edu/class-schedule?switch=showclass";
}

package ml.zer0dasho.classifier;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Scraper {
	
	// Undergrad list of departments
	public static final String DEPARTMENT_LIST = "https://www.buffalo.edu/class-schedule?semester=fall";
	
	// Graduate list of departments
	public static final String DEPARTMENT_LIST_2 = "http://www.buffalo.edu/class-schedule?semester=fall&division=2";
	
	
	public static List<String> getDepartmentCourseLinks() {
		String page = Requests.Try(() -> Requests.readAllBytes(Requests.get(DEPARTMENT_LIST).getEntity().getContent()));
		return getDepartmentCourseLinks(page);
	}
	
	public static List<String> getDepartmentCourseLinks2() {
		String page = Requests.Try(() -> Requests.readAllBytes(Requests.get(DEPARTMENT_LIST_2).getEntity().getContent()));
		return getDepartmentCourseLinks(page);
	}
	
	public static List<Course> getCourses(String departmentCourseLink) {
		String page = Requests.Try(() -> Requests.readAllBytes(Requests.get(departmentCourseLink).getEntity().getContent()));
		return getCourses(page, false);
	}

	private static List<String> getDepartmentCourseLinks(String departmentListPage) {
		Pattern pattern = Pattern.compile("http:\\/\\/www\\.buffalo\\.edu\\/class-schedule\\?switch=showcourses[^\"]+");
		Matcher matcher = pattern.matcher(departmentListPage);

		List<String> links = new ArrayList<String>();
		
		while(matcher.find())
			links.add(matcher.group());
		
		return links;
	}
	
	private static List<Course> getCourses(String departmentCoursePage, boolean x) {
	// Gets rid of unnecessary whitespace
	departmentCoursePage = departmentCoursePage.replaceAll("\\n|  ", "");
	
	// Find course information
	Pattern pattern = Pattern.compile("(<td class=\"padding\">|<td class=\"open\">|<td class=\"closed\">|<td class=\"arrows\">|<a href=\"http://www\\.buffalo\\.edu/class-schedule\\?switch=showclass)" + "(.+?(?=<\\/td>|<\\/a>))");
	Matcher matcher = pattern.matcher(departmentCoursePage);

	List<Course> courses = new ArrayList<Course>();
	List<String> course_builder = new ArrayList<String>();
	
	String course_detail = null;
	
	while(matcher.find()) {
		String match = matcher.group(2);

		// Hacky way of doing things. Used to get regnum and course page link consistently.
		if(match.contains("regnum")) {
			
			// Class number (regnum)
			course_detail = match.replaceAll("([^\"]+).*", "$1");
			course_builder.set(0, match.replaceAll(".*regnum=(\\d+).*", "$1"));
			
			// Department + Course
			match = match.replaceAll("[^>]+>(.*)", "$1");
		}
		
		course_builder.add(match);
		
		// Find next course
		if(course_builder.size() == 11) {
			courses.add(new Course(course_builder, course_detail.replaceAll("&amp;", "&")));
			course_builder = new ArrayList<String>();
			course_detail = null;
		}
	}
	
	return courses;
}
	
	public static String[] getCourseDetail(String coursePageLink) {
		String page = Requests.Try(() -> Requests.readAllBytes(Requests.get(coursePageLink).getEntity().getContent()));
		return getCourseDetail(page, false);
	}

	private static String[] getCourseDetail(String coursePage, boolean x) {
		// Gets rid of unnecessary whitespace
		coursePage = coursePage.replaceAll("\\n", "").replaceAll("[ ]{2,}", " ");
		
		// Find course information
		Pattern pattern = Pattern.compile("<td class=\"info\"><br/>(.+?(?=<\\/td>))");
		Matcher matcher = pattern.matcher(coursePage);
		
		if(matcher.find()) {
			// Gets rid of unnecessary whitespace
			String result = matcher.group(1)
								   .replaceAll("&nbsp;", "")
								   .replaceAll("[ ]{2,}", " ")
								   .replaceAll("TBA[^,]", "TBA,")
								   .trim();
			
			String[] fields = result.split(",|<br />");
			
			return fields;
		}
		
		return null;
	}
}
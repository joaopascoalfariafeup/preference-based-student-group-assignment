package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ilog.concert.IloIntVar;
import ilog.concert.IloLinearIntExpr;

public class Student {
	private String code;
	private String name;
	private float avgGrade;
	private List<StudentPreference> preferences;
	private Set<Course> enrolledCourses; // List of the mandatory courses this student enrolled in
	private List<Boolean> wantedPeriods; // True if student selected period of index N in one of their preferences, false otherwise 	
	private Map<Course, Set<Group>> wantedCourseGroups; // List of the course-group pairs this student selected over all their preferences

	// JPF09FEV2020
	private int maxOptionalCourses; // maximum number of optional courses, among all student preferences
	
	// CPLEX variables and expressions
	private Map<Course, Map<Group, IloIntVar>> courseGroupAssignments; // Course code -> (group code -> (boolean variable indicating assignment))
	private IloIntVar hasCompleteAssignment; // Boolean variable indicating if this student was assigned to all courses they enrolled in
	private Map<Course, IloLinearIntExpr> sumAllAssignmentsPerCourse; 
	
	// JPF23OCT2020
	private boolean mustFullfillPreference = false;	
	public boolean getMustFullfillPreference() {
		return mustFullfillPreference ;
	}
	public void setMustFullfillPreference(boolean mustFullfillPreference) {
		this.mustFullfillPreference = mustFullfillPreference ;
	}

	public Student(String code, String name) {
		this.code = code;
		this.name = name;
		this.avgGrade = 10; // JPF Default grade is minimum (was -1)
		this.preferences = new ArrayList<>();
		this.enrolledCourses = new HashSet<>();
		this.wantedPeriods = new ArrayList<>();
		this.wantedCourseGroups = new HashMap<>();
		this.courseGroupAssignments = new HashMap<>();
		this.sumAllAssignmentsPerCourse = new HashMap<>();
		
		for (int i = 0; i < 12; ++i) {
			this.wantedPeriods.add(false);
		}
	}
	
	public boolean isOdd() {
		if (code != null && code.length() > 0){
			switch (code.charAt(code.length()-1)) {
				case '1': case '3': case '5': case '7': case '9':
					//System.out.println("isOdd:" + code);
					return true;
				default: 
					return false;
			}
		}
		return false;
	}
	
	public boolean isEven() {
		if (code != null && code.length() > 0){
			switch (code.charAt(code.length()-1)) {
				case '0': case '2': case '4': case '6': case '8':
					//System.out.println("isEven:" + code);
					return true;
				default: 
					return false;
			}
		}
		return false;
	}

	public String getCode() {
		return code;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public float getAvgGrade() {
		return avgGrade;
	}
	
	public void setAvgGrade(float avgGrade) {
		this.avgGrade = avgGrade;
	}
	
	public List<StudentPreference> getPreferences() {
		return preferences;
	}
	
	public void setPreferences(List<StudentPreference> preferences) {
		this.preferences = preferences;
		
		// Add all course-group pairs from all preferences to the set of wanted course-group pairs
		for (StudentPreference preference : preferences) {
			for (Course course : preference.getCourseGroupPairs().keySet()) {
				Group group = preference.getCourseGroupPairs().get(course);				
				addEnrolledCourse(course, false);
				addWantedCourseGroup(course, group);
			}
		}
	}
	
	public Set<Course> getEnrolledCourses() {
		return enrolledCourses;
	}
	
	public void addEnrolledCourse(Course course, boolean addGroups) {
		if (! enrolledCourses.contains(course)) {
			enrolledCourses.add(course);
			wantedCourseGroups.put(course, new HashSet<>());
			course.incNumEnrollments();
			if (addGroups)
				for (Group group : course.getGroups().values())
					addWantedCourseGroup(course, group);
		}	
	}
		
	public boolean getWantedPeriod(int period) {
		return wantedPeriods.get(period);
	}
	
	public void setWantedPeriodsTrue(Set<Integer> periods) {
		for (int period : periods) {
			wantedPeriods.set(period, true);
		}
	}
	
	public boolean getWantedCourseGroup(Course course, Group group) {
		try {
			return wantedCourseGroups.get(course).contains(group);
		}
		catch (NullPointerException e) {
			return false;
		}
	}
	
	public void addWantedCourseGroup(Course course, Group group) {
		//wantedCourseGroups.putIfAbsent(course, new HashSet<>());
		wantedCourseGroups.get(course).add(group);
		setWantedPeriodsTrue(group.getOccupiedPeriods());
	}
	
	
	public Map<Course, Map<Group, IloIntVar>> getCourseGroupAssignments() {
		return courseGroupAssignments;
	}
	
	public IloIntVar getHasCompleteAssignment() {
		return hasCompleteAssignment;
	}
	
	public void setHasCompleteAssignment(IloIntVar hasCompleteAssignment) {
		this.hasCompleteAssignment = hasCompleteAssignment;
	}
	
	// JPF09FEV2020
	public void setMaxOptionalCourses(int maxOptionalCourses) {
		this.maxOptionalCourses = maxOptionalCourses;		
	}

	public int getMaxOptionalCourses() {
		return maxOptionalCourses;		
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Student) {
			return ((Student) obj).code.equals(code);
		}
		else return false;
	}
	
	@Override
	public int hashCode() {
		return code.hashCode();
	}
	
	@Override
	public String toString() {
		return code;
	}

	// JPF09FEV2020
	public void setSumAllAssignmentsPerCourse(Map<Course, IloLinearIntExpr> sumAllAssignmentsPerCourse) {
		this.sumAllAssignmentsPerCourse = sumAllAssignmentsPerCourse;		
	}

	// JPF09FEV2020
	public Map<Course, IloLinearIntExpr> getSumAllAssignmentsPerCourse() {
		return sumAllAssignmentsPerCourse;		
	}

}

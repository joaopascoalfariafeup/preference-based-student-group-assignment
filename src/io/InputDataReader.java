package io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.Course;
import model.Group;
import model.Schedule;
import model.Student;
import model.StudentPreference;

public class InputDataReader {
	private String coursesFilename, groupsFilename, scheduleFilename, groupCompositesFilename, preferencesFilename, gradesFilename, enrollmentsFilename, procVersion;
	private int semester;
	private Map<String, Course> courses;
	private Schedule schedule;
	private Map<String, Student> students;
	
	// JPF 11Fev2019, for improved error handling
	public static class InvalidInputDataException extends Exception {
		public InvalidInputDataException(String msg){
			super(msg);
		}
	}
	
	public InputDataReader(String coursesFilename, String groupsFilename, String scheduleFilename, String groupCompositesFilename, String preferencesFilename, String gradesFilename, String enrollmentsFilename, int semester, String procVersion) throws IOException {
		this.coursesFilename = coursesFilename;
		this.groupsFilename = groupsFilename;
		this.scheduleFilename = scheduleFilename;
		this.groupCompositesFilename = groupCompositesFilename;
		this.preferencesFilename = preferencesFilename;
		this.gradesFilename = gradesFilename;
		this.enrollmentsFilename = enrollmentsFilename;
		this.semester = semester;
		this.procVersion = procVersion;
		this.courses = new HashMap<>();
		this.schedule = new Schedule();
		this.students = new HashMap<>();
	}
	
	public void readData() throws IOException, InvalidInputDataException {
		readCourses();
		readGroups();
		readSchedule();
		readStudents();
		makeStudentsAdjustments();
		readEnrollments();
		readStudentsGrades();		
	}
	
	public Map<String, Course> getCourses() {
		return courses;
	}
	
	public Schedule getSchedule() {
		return schedule;
	}
	
	public Map<String, Student> getStudents() {
		return students;
	}

	// JPF 11Fev2019
	private String[] weekDayNames = {"seg", "ter", "qua", "qui", "sex", "sab"};
	
	// JPF 11Fev2019
	private String[] splitAndTrim(String line) {
		String[] s = line.split(";");
		for (int i = 0; i < s.length; i++)
			s[i] = s[i].trim();
		return s;
	}

	// JPF 11Fev2019
	private int parseWeekDay(String weekDayStr) throws InvalidInputDataException {
		if (weekDayStr.length() == 1)
			return Integer.parseInt(weekDayStr) - 2;
		else
			for (int i = 0; i < weekDayNames.length; i++)
				if (weekDayNames[i].toLowerCase().startsWith(weekDayStr.toLowerCase()))
					return i;
		throw new InvalidInputDataException("Invalid week day: " + weekDayStr);
	}
	
	// JPF 11Fev2019
	private float parseFloat(String num) {
		return Float.parseFloat(num.replace(',', '.'));
	}
	
	private void readCourses() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(coursesFilename));
		reader.readLine();
		String fileLine;
		
		while ((fileLine = reader.readLine()) != null) {
			String[] line = splitAndTrim(fileLine);
			
			if (!((semester == 1 && line[3].equals("1S")) || (semester == 2 && line[3].equals("2S")))) 
				continue;
			
			String courseCode = line[0];
			int weeklyTimeslots = Integer.parseInt(line[4]) * 2; // Input data is in hours (2*timeslots)
			boolean mandatory = (Integer.parseInt(line[5]) == 0) ? true : false;
			
			courses.put(courseCode, new Course(courseCode, mandatory, weeklyTimeslots));
		}
		
		reader.close();
	}
	
	private void readGroups() throws IOException, InvalidInputDataException {
		BufferedReader reader = new BufferedReader(new FileReader(groupsFilename));
		reader.readLine();
		String fileLine;
		
		while ((fileLine = reader.readLine()) != null) {
			String[] line = splitAndTrim(fileLine);
			
			String courseCode = line[0];
			String groupCode = line[1];
			int groupCapacity = Integer.parseInt(line[2]);
			if (line.length == 4) {
				int numStudentsEnrolled = Integer.parseInt(line[3]);
				groupCapacity -= numStudentsEnrolled;
			}

			// JPF 11Fev2019
			if (groupCapacity < 0)
				groupCapacity = 0;
			
			Course course = courses.get(courseCode);
			
			// JPF 11Fev2019
			if (course == null) {
				reader.close();
				throw new InvalidInputDataException("Course not found: " + courseCode);
			}
			Group thisGroup = new Group(groupCode, groupCapacity);
			course.addGroup(thisGroup);
		}
		
		reader.close();
	}
	
	private Map<String, Set<String>> readGroupComposites() throws IOException {
		Map<String, Set<String>> groupComposites = new HashMap<>();
		
		BufferedReader reader = new BufferedReader(new FileReader(groupCompositesFilename));
		reader.readLine();
		String fileLine;
		
		while ((fileLine = reader.readLine()) != null) {
			String[] line = splitAndTrim(fileLine);
			
			String compositeName = line[0];
			
			//Set<String> groupCodes = new HashSet<>(); JPF 10FEV2019			
			Set<String> groupCodes = groupComposites.get(compositeName);
			if (groupCodes == null)
				groupCodes = new HashSet<>();
			
			for (int i = 1; i < line.length; ++i) {
				String groupCode = line[i];
				
				if (groupCode.equals("")) 
					break;
				
				groupCodes.add(groupCode);
			}
			
			groupComposites.put(compositeName, groupCodes);
		}
		
		reader.close();
		
		return groupComposites;
	}
	
	private void readSchedule() throws IOException, InvalidInputDataException {
		Map<String, Set<String>> groupComposites = readGroupComposites();
		
		BufferedReader reader = new BufferedReader(new FileReader(scheduleFilename));
		reader.readLine();
		String fileLine;
		
		while ((fileLine = reader.readLine()) != null) {
			String[] line = splitAndTrim(fileLine);
			
			String groupCode = line[0];
			String courseCode = line [1];
			int weekDay = parseWeekDay(line[2]);
			int startTime = (int) ((parseFloat(line[3]) - 8) * 2  + 0.00001); // JPF 14Set2019 soma 0.001
			int duration = (int) (parseFloat(line[4]) * 2 + 0.00001); // JPF 14Set2019 soma 0.0001
			boolean isPracticalClass = line[5].equals("T") ? false : true;
			
			Course thisCourse = courses.get(courseCode);

			// JPF 11Fev2019
			if (thisCourse == null) {
				reader.close();
				throw new InvalidInputDataException("Course not found: " + courseCode);
			}	
			Set<String> groupsFromComposite = groupComposites.get(groupCode);
			
			if (groupsFromComposite == null) { // If this is not a group composite, add it to the schedule
				Group thisGroup = thisCourse.getGroups().get(groupCode);
				if (thisGroup == null)
					System.err.println("Grupo não encontrado: " + groupCode);
				else
					schedule.addCourseGroup(thisCourse, thisGroup, isPracticalClass, weekDay, startTime, duration);
			}
			else {
				for (String groupFromComposite : groupsFromComposite) { // If it is, add all the individual groups to the schedule
					Group thisGroup = thisCourse.getGroups().get(groupFromComposite);
					if (thisGroup != null) {
						schedule.addCourseGroup(thisCourse, thisGroup, isPracticalClass, weekDay, startTime, duration);
					}
				}
			}
		}
		
		reader.close();
	}
	
	
	private void readStudents() throws IOException, InvalidInputDataException {
		BufferedReader reader = new BufferedReader(new FileReader(preferencesFilename));
		reader.readLine();
		String fileLine;
		
		while ((fileLine = reader.readLine()) != null) {
			String[] line = splitAndTrim(fileLine);
			
			if (!line[0].equals(procVersion)) continue;
			
			String studentCode = line[1];
			String studentName = line[2];
			int preferenceOrder = Integer.parseInt(line[6]);
			String courseCode = line[7];
			String groupCode = line[8];
			
			Student thisStudent = students.get(studentCode);
			if (thisStudent == null) { // If this is a new student, create them and add them to the students map
				thisStudent = new Student(studentCode, studentName);
				students.put(studentCode, thisStudent);
			}
			
			StudentPreference thisPreference;
			
			try {
				thisPreference = thisStudent.getPreferences().get(preferenceOrder - 1);
			} catch (IndexOutOfBoundsException e) { // If this is a new preference, create it and add it to the student's preference list
				thisPreference = new StudentPreference(preferenceOrder);
				thisStudent.getPreferences().add(thisPreference);
			}
			
			Course thisCourse = courses.get(courseCode);

			// JPF 11Fev2019
			if (thisCourse == null) {
				reader.close();
				throw new InvalidInputDataException("Course not found: " + courseCode);
			}
			
			Group thisGroup = thisCourse.getGroups().get(groupCode);

			// JPF 11FEV2019
			if (thisGroup == null) {
				reader.close();
				throw new InvalidInputDataException("Course-Group combination not found: " + courseCode + "-" + groupCode);
			}
			
			thisPreference.addCourseGroupPair(thisCourse, thisGroup);
			
			//thisStudent.getEnrolledCourses().add(thisCourse); // Add it to the list of this student's enrollments
			//thisStudent.setWantedPeriodsTrue(thisGroup.getOccupiedPeriods());
		}
		
		reader.close();
	}
	
	// JPF 18Fev2019
	private void readEnrollments() throws IOException, InvalidInputDataException {
		BufferedReader reader = new BufferedReader(new FileReader(enrollmentsFilename));
		reader.readLine();
		String fileLine;
		
		while ((fileLine = reader.readLine()) != null) {
			String[] line = splitAndTrim(fileLine);
			
			String studentCode = line[0];
			String studentName = line[1];
			String courseCode = line[2];
			
			Student thisStudent = students.get(studentCode);
			if (thisStudent == null) { // If this is a new student, create and add to the students map
				thisStudent = new Student(studentCode, studentName);
				students.put(studentCode, thisStudent);
			}
			
			Course thisCourse = courses.get(courseCode);

			if (thisCourse == null) {
				//reader.close();
				//throw new InvalidInputDataException("Course not found: " + courseCode);
				System.out.println("Course not found (enrollment ignored): " + courseCode);
			}
			else
				thisStudent.addEnrolledCourse(thisCourse, true); 
		}
		
		reader.close();
	}
	
	private void makeStudentsAdjustments() {
		for (Student student : students.values()) {
			// Remove duplicate preferences
			List<StudentPreference> preferences = student.getPreferences();
			List<StudentPreference> preferencesWithoutDuplicates = new ArrayList<>();
			
			for (StudentPreference preference : preferences) {
				if (!preferencesWithoutDuplicates.contains(preference)) {
					preferencesWithoutDuplicates.add(preference);
					preference.setOrder(preferencesWithoutDuplicates.size());
				}
			}
			
			student.setPreferences(preferencesWithoutDuplicates);
			
			// Count each course's number of students enrolled
			//for (Course course : student.getEnrolledCourses()) {
			//	course.incNumEnrollments();
			//}

			// JPF09FEV2020
			// Determine maxOptionalCourses 
			int maxOptionalCourses = 0;
			for (StudentPreference preference : student.getPreferences()) {
				int num = 0;
				for (Course course : preference.getCourseGroupPairs().keySet())
					if (!course.getMandatory())
						num++;
				if (num > maxOptionalCourses)
					maxOptionalCourses = num;
			}
			student.setMaxOptionalCourses(maxOptionalCourses);
		}
	}
	
	private void readStudentsGrades() throws IOException, InvalidInputDataException {
		BufferedReader reader = new BufferedReader(new FileReader(gradesFilename));
		reader.readLine();
		String fileLine;
		
		while ((fileLine = reader.readLine()) != null) {
			String[] line = splitAndTrim(fileLine);
			
			String studentCode = line[0];
			float studentGrade = (line.length == 1 || line[1].equals("0")) ? 10 : parseFloat(line[1]); // Some students have missing grade information
			
			Student thisStudent = students.get(studentCode);
			if (thisStudent != null) { // If the student isn't found, it means they're not being assigned to groups in this process version
				thisStudent.setAvgGrade(studentGrade);

				// JPF23OCT2021
				if (line.length >= 3 && parseFloat(line[2]) == 1.0)
					thisStudent.setMustFullfillPreference(true);
			}
			
		}
		
		reader.close();
	}
}

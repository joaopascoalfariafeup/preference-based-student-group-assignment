package problem;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearIntExpr;
import ilog.concert.IloLinearIntExprIterator;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import io.InputDataReader;
import io.InputDataReader.InvalidInputDataException;
import io.OutputDataWriter;
import model.Course;
import model.Group;
import model.Schedule;
import model.Student;
import model.StudentPreference;
import model.Timeslot;

public class AssignmentProblem {
	public enum PreferenceWeightingMode {TIMES, EXPONENT};
	
	private final static int numTimePeriods = 12; // From monday morning to saturday afternoon
	
	private Map<String, Course> courses;
	private Schedule schedule;
	private Map<String, Student> students;
	private boolean isMandatoryAssignment;
	private PreferenceWeightingMode preferenceWeightingMode;
	private float weightMaximizeSumAllAssignments, 
		weightMaximizeCompleteStudents,
		weightMaximizeOccupiedTimeslots, 
		weightMaximizeFulfilledPreferences,
		weightMinimizeGroupUtilizationSlacks, 
		weightMinimizeOccupiedPeriodsWithNoPreferenceAssigned, 
		weightMinimizeUnwantedOccupiedPeriods,
		weightMinimizeAssignmentsToUnwantedGroups,
		weightMinDiffOddEven; // JPF22SET2020
	
	private int sumGroupCapacity = 0;// JPF04FEV2021

	private IloCplex cplex;
	private OutputDataWriter writer;
	private int timeout = 300; // seconds	
	private int targetNumOccupiedTimeslots;
	private IloLinearNumExpr weightedSumAllAssignments, 
		weightedSumAllCompleteStudents, 
		weightedSumFulfilledPreferences, 
		sumAllGroupUtilizationSlacks,
		weightedSumAllAssignmentsToUnwantedGroups;
	private IloLinearIntExpr sumAllOccupiedTimeslots, 
		sumAllOccupiedPeriodsWithNoPreferenceAssigned, 
		sumAllUnwantedOccupiedPeriods,
		sumDiffOddEven; // JPF22SET2020
	
	private IloNumExpr objMaximizeSumAllAssignments = null,
					objMaximizeCompleteStudents = null,
					objMaximizeOccupiedTimeslots = null,
					objMaximizeFulfilledPreferences = null,
					objMinimizeGroupUtilizationSlacks = null,
					objMinimizeOccupiedPeriodsWithNoPreferenceAssigned = null,
					objMinimizeUnwantedOccupiedPeriods = null,
					objMinimizeAssignmentsToUnwantedGroups = null,
					objMinDiffOddEven = null;

		
	public AssignmentProblem(String coursesFilename, String groupsFilename, String scheduleFilename, String groupCompositesFilename, String preferencesFilename,
			String gradesFilename, String enrollmentsFilename, int semester, String procVersion, boolean isMandatoryAssignment, PreferenceWeightingMode preferenceWeightingMode,
			float weightMaximizeSumAllAssignments, float weightMaximizeCompleteStudents, float weightMaximizeOccupiedTimeslots, float weightMaximizeFulfilledPreferences,
			float weightMinimizeGroupUtilizationSlacks, float weightMinimizeOccupiedPeriodsWithNoPreferenceAssigned, float weightMinimizeUnwantedOccupiedPeriods,
			float weightMinimizeAssignmentsToUnwantedGroups, 
			float weightMinDiffOddEven, 
			String outputPath, int timeout) throws IloException, IOException, InvalidInputDataException {

		InputDataReader reader = new InputDataReader(coursesFilename, groupsFilename, scheduleFilename, groupCompositesFilename, preferencesFilename, gradesFilename, enrollmentsFilename, semester, procVersion);
		reader.readData();
		
		this.courses = reader.getCourses();
		this.schedule = reader.getSchedule();
		this.students = reader.getStudents();
		this.isMandatoryAssignment = isMandatoryAssignment;
		this.preferenceWeightingMode = preferenceWeightingMode;
		this.weightMaximizeSumAllAssignments = weightMaximizeSumAllAssignments;
		this.weightMaximizeCompleteStudents = weightMaximizeCompleteStudents;
		this.weightMaximizeOccupiedTimeslots = weightMaximizeOccupiedTimeslots;
		this.weightMaximizeFulfilledPreferences = weightMaximizeFulfilledPreferences;
		this.weightMinimizeGroupUtilizationSlacks = weightMinimizeGroupUtilizationSlacks;
		this.weightMinimizeOccupiedPeriodsWithNoPreferenceAssigned = weightMinimizeOccupiedPeriodsWithNoPreferenceAssigned;
		this.weightMinimizeUnwantedOccupiedPeriods = weightMinimizeUnwantedOccupiedPeriods;
		this.weightMinimizeAssignmentsToUnwantedGroups = weightMinimizeAssignmentsToUnwantedGroups;
		this.weightMinDiffOddEven = weightMinDiffOddEven; // JPF22SET2020
		this.cplex = new IloCplex();
		this.writer = new OutputDataWriter(cplex, cplex.getParam(IloCplex.DoubleParam.EpRHS), courses, students, outputPath);
		
		this.targetNumOccupiedTimeslots = 0;
		this.timeout = timeout;
	}
	
	public void run() throws IloException, IOException {
		writer.checkGroupCapacities();
		defineManualAssignmentProblem();
		solve();
	}
	

	private void addAssignmentDecisionVariables() throws IloException {
		for (Student student : students.values()) {
			Map<Course, Map<Group, IloIntVar>> courseAssignments = student.getCourseGroupAssignments();
			for (Course course : student.getEnrolledCourses()) {				
				Map<Group, IloIntVar> groupAssignments = new HashMap<>();
				for (Group group : course.getGroups().values()) {
					// VARIABLE: student assigned to this course-group pair?
					IloIntVar groupAssignment = cplex.boolVar("(" + student.getCode() + ": " + course.getCode() + "-" + group.getCode() + ")");
					groupAssignments.put(group, groupAssignment);
				}
				courseAssignments.put(course, groupAssignments);
			}
		}
	}

	/*
	private void addUniquenessAssignmentConstraints() throws IloException {
		for (Student student : students.values()) {
			for (Course course : student.getEnrolledCourses()) {				
				Map<Group, IloIntVar> groupAssignments = student.getCourseGroupAssignments().get(course);		
				IloLinearIntExpr sumAllAssignmentsPerStudentPerCourse = cplex.linearIntExpr();		
				for (Group group : course.getGroups().values())
			        sumAllAssignmentsPerStudentPerCourse.addTerm(1, groupAssignments.get(group));

				// CONSTRAINT: a student can be assigned to at most 1 group per course
				cplex.addLe(sumAllAssignmentsPerStudentPerCourse, 1); 			
			}
		}
	}*/

	
	private void defineManualAssignmentProblem() throws IloException {
		weightedSumAllAssignments = cplex.linearNumExpr(); // Summation of each student's assignments multiplied by their grade
		weightedSumAllCompleteStudents = cplex.linearNumExpr(); // Summation of all variables indicating a student assigned to all of their courses multiplied by their grade
		weightedSumFulfilledPreferences = cplex.linearNumExpr(); // Summation of all variables indicating a student preference fulfilled multiplied by their grade
		sumAllOccupiedTimeslots = cplex.linearIntExpr(); // Sum of all timeslots occupied individually by all students
		sumAllGroupUtilizationSlacks = cplex.linearNumExpr(); // Sum of all group utilization slack variables for the group balance soft constraint
		sumAllOccupiedPeriodsWithNoPreferenceAssigned = cplex.linearIntExpr(); // Sum of all periods occupied individually by all students who weren't assigned to one of their preferences
		sumAllUnwantedOccupiedPeriods = cplex.linearIntExpr(); // Sum of all periods occupied individually by students who didn't choose them in one of their preferences
		weightedSumAllAssignmentsToUnwantedGroups = cplex.linearNumExpr(); // Weighted sum of all assignments of students to course-group pairs they didn't include in one of their preferences
		sumDiffOddEven = cplex.linearIntExpr(); // JPF22SET2020
		
		float sumEnrollmentsTimesAvgGrade = 0; // Summation of each student's number of course enrollments multiplied by their grade
		float sumEnrollments = 0; // Summation of each student's number of course enrollments 
		float sumAvgGrades = 0; // Sum of every student's grade
		float sumAvgPow = 0; // Sum of 2^(every student's grade) * 10
		
		for (Student student : students.values()) {
			processStudent(student);
			
			int studentEnrollments = student.getEnrolledCourses().size();
			float studentAvgGrade = student.getAvgGrade();
			
			sumEnrollmentsTimesAvgGrade += studentEnrollments * studentAvgGrade;
			sumAvgGrades += studentAvgGrade;
			sumEnrollments += studentEnrollments;
			
			if (preferenceWeightingMode == PreferenceWeightingMode.EXPONENT) {
				sumAvgPow += Math.pow(2, student.getAvgGrade()) * 10;
			}
		}
		
		float sumTargetNumStudentsAssigned = 0;
		
		for (Course course : courses.values()) {
			if (isMandatoryAssignment) {
				sumTargetNumStudentsAssigned += processCourseMandatory(course);
			}
			else if (!course.getMandatory()) {
				processCourseOptional(course);
			}
		}
		
		objMaximizeSumAllAssignments = (sumEnrollmentsTimesAvgGrade != 0) ? cplex.prod(1. / sumEnrollmentsTimesAvgGrade, weightedSumAllAssignments) : cplex.constant(1);
		objMaximizeCompleteStudents = (sumAvgGrades != 0) ? cplex.prod(1. / sumAvgGrades, weightedSumAllCompleteStudents) : cplex.constant(1);
		objMaximizeOccupiedTimeslots = (targetNumOccupiedTimeslots != 0) ? cplex.prod(1. / targetNumOccupiedTimeslots, sumAllOccupiedTimeslots) : cplex.constant(1);
		objMaximizeFulfilledPreferences = null;
		if (preferenceWeightingMode == PreferenceWeightingMode.EXPONENT) {
			objMaximizeFulfilledPreferences = (sumAvgPow != 0) ? cplex.prod(1. / sumAvgPow, weightedSumFulfilledPreferences) : cplex.constant(1);
		}
		else if (preferenceWeightingMode == PreferenceWeightingMode.TIMES) {
			objMaximizeFulfilledPreferences = (sumAvgGrades != 0) ? cplex.prod(1. / (sumAvgGrades * 10), weightedSumFulfilledPreferences) : cplex.constant(1);
		}
		objMinimizeGroupUtilizationSlacks = (sumTargetNumStudentsAssigned != 0) ? cplex.sum(1, cplex.prod(-1. / sumTargetNumStudentsAssigned, sumAllGroupUtilizationSlacks)) : cplex.constant(1);
		objMinimizeOccupiedPeriodsWithNoPreferenceAssigned = cplex.sum(1, cplex.prod(-1. / (students.size() * numTimePeriods), sumAllOccupiedPeriodsWithNoPreferenceAssigned));
		objMinimizeUnwantedOccupiedPeriods = cplex.sum(1, cplex.prod(-1. / (students.size() * numTimePeriods), sumAllUnwantedOccupiedPeriods));
		objMinimizeAssignmentsToUnwantedGroups = cplex.sum(1, cplex.prod(-1. / sumEnrollmentsTimesAvgGrade, weightedSumAllAssignmentsToUnwantedGroups));
		
		//objMinDiffOddEven = cplex.sum(1, cplex.prod(-1. / sumEnrollments, sumDiffOddEven));
		// JPF04FEV2021
		// JPF22OCT2021
		// objMinDiffOddEven = cplex.sum(1.0, cplex.prod(-1. / (double)sumGroupCapacity, sumDiffOddEven));
						
		if (isMandatoryAssignment) {
			cplex.addMaximize(cplex.sum(
					cplex.prod(weightMaximizeSumAllAssignments, objMaximizeSumAllAssignments),
					cplex.prod(weightMaximizeCompleteStudents, objMaximizeCompleteStudents),
					cplex.prod(weightMaximizeOccupiedTimeslots, objMaximizeOccupiedTimeslots),
					cplex.prod(weightMaximizeFulfilledPreferences, objMaximizeFulfilledPreferences),
					cplex.prod(weightMinimizeGroupUtilizationSlacks, objMinimizeGroupUtilizationSlacks),
					cplex.prod(weightMinimizeOccupiedPeriodsWithNoPreferenceAssigned, objMinimizeOccupiedPeriodsWithNoPreferenceAssigned),
					cplex.prod(weightMinimizeUnwantedOccupiedPeriods, objMinimizeUnwantedOccupiedPeriods),
				    cplex.prod(weightMinimizeAssignmentsToUnwantedGroups, objMinimizeAssignmentsToUnwantedGroups)));
					/*
					cplex.sum(
							cplex.prod(weightMinDiffOddEven, objMinDiffOddEven))
					)
					*/
		}
		else {
			//cplex.addMaximize(objMaximizeFulfilledPreferences);

			// JPF09FEV2020
			cplex.addMaximize(cplex.sum(
					cplex.prod(weightMaximizeSumAllAssignments, objMaximizeSumAllAssignments),
					cplex.prod(weightMaximizeFulfilledPreferences, objMaximizeFulfilledPreferences)));
		}
		
	}
	
	private void processStudent(Student student) throws IloException {
		float avgGrade = student.getAvgGrade();
		IloLinearIntExpr sumAllAssignmentsPerStudent = processAssignmentsPerStudent(student); // Sum of all assignments for this student
		
		
		IloLinearIntExprIterator studentAssignmentsIterator = sumAllAssignmentsPerStudent.linearIterator();
		while (studentAssignmentsIterator.hasNext()) { // Iterating over this student's assignment variables to add them to the objective function multiplied by their grade
			IloIntVar studentAssignment = studentAssignmentsIterator.nextIntVar();
			weightedSumAllAssignments.addTerm(avgGrade, studentAssignment);
		}
		
		IloIntVar completeStudent = cplex.boolVar("(Complete assignment for " + student.getCode() + ")"); // VARIABLE: student was assigned to all of their courses?
		cplex.add(cplex.ifThen(cplex.le(sumAllAssignmentsPerStudent, student.getEnrolledCourses().size() - 1), cplex.eq(completeStudent, 0))); // CONSTRAINT: if sum of all assignments < number of enrolled courses, then it's not a complete assignment
		cplex.add(cplex.ifThen(cplex.eq(sumAllAssignmentsPerStudent, student.getEnrolledCourses().size()), cplex.eq(completeStudent, 1))); // CONSTRAINT: if sum of all assignments = number of enrolled courses, then it is a complete assignment
		
		weightedSumAllCompleteStudents.addTerm(avgGrade, completeStudent);
		student.setHasCompleteAssignment(completeStudent); // Set this student's complete status variable
		
		IloLinearIntExpr sumStudentFulfilledPreferences = cplex.linearIntExpr(); // Sum of all fulfilled preferences for this student
		for (StudentPreference preference : student.getPreferences()) {
			IloIntVar fulfilledPreference = processStudentPreference(student, preference, sumAllAssignmentsPerStudent);
			
			sumStudentFulfilledPreferences.addTerm(1, fulfilledPreference);
		}
		
		// Process the student's timeslots and occupied time periods
		
		int currentPeriod = -2;
		IloLinearIntExpr currentSumOccupiedTimeslots = null;
		
		Iterator<Timeslot> schItr = schedule.iterator();
		while (schItr.hasNext()) {
			Timeslot timeslot = schItr.next();
			IloIntVar timeslotOccupied = processStudentTimeslot(student, timeslot);
			
			// Adding this timeslot to the calculation of this student's occupied periods...
			
			int timeslotPeriod = timeslot.getPeriod();
			
			if (timeslotPeriod == -1) continue; // Don't do anything with timeslots at 1:00-1:30pm and 1:30-2:00pm
			
			if (timeslotPeriod != currentPeriod || !schItr.hasNext()) { // If this is a new period or the last one...
				if (currentSumOccupiedTimeslots != null) { // If this is not the first period...
					if (!schItr.hasNext()) { // If this is the last period...
						currentSumOccupiedTimeslots.addTerm(1, timeslotOccupied);
					}
					
					IloIntVar occupiedPeriod = cplex.boolVar();
					
					// CONSTRAINT: if the student wasn't assigned to any of his/her preferences
					// and the sum of all occupied timeslots in this period >= 1,
					// then the period is occupied (otherwise the minimization goal will set it to 0)
					cplex.add(cplex.ifThen(cplex.and(
							cplex.eq(sumStudentFulfilledPreferences, 0),
							cplex.ge(currentSumOccupiedTimeslots, 1)),
							cplex.eq(occupiedPeriod, 1)));
					
					sumAllOccupiedPeriodsWithNoPreferenceAssigned.addTerm(1, occupiedPeriod);
					
					if (!student.getWantedPeriod(currentPeriod)) { // If the student didn't choose this period in one of their preferences, add it to the sum of unwanted periods
						sumAllUnwantedOccupiedPeriods.addTerm(1, occupiedPeriod);
					}
				}
				
				currentPeriod = timeslotPeriod;
				currentSumOccupiedTimeslots = cplex.linearIntExpr();
			}
			
			if (schItr.hasNext()) { // If this isn't the last period...
				currentSumOccupiedTimeslots.addTerm(1, timeslotOccupied);
			}
		}

		// JPF09FEV2020
		// CONSTRAINT: a student can be assigned to at most the maximum number of
		// optional units among his/her preferences
		cplex.addLe(calcSumAssignmentsPerStudentOptional(student), student.getMaxOptionalCourses()); 			

		
		// JPF23OCT2021
		// Case in which the student must be assigned to one of the preferences
		// (applicable when changing allocation)
		if (student.getMustFullfillPreference())
			cplex.addGe(sumStudentFulfilledPreferences, 1);
	}
	
	// Generate expression with sum of course-group assignments (0/1) for this student
	private IloLinearIntExpr processAssignmentsPerStudent(Student student) throws IloException {
		IloLinearIntExpr sumAllAssignmentsPerStudent = cplex.linearIntExpr();
		Map<Course, IloLinearIntExpr> sumAllAssignmentsPerCourse = new HashMap<>();
		
		for (Course course : student.getEnrolledCourses()) {
			targetNumOccupiedTimeslots += course.getWeeklyTimeslots();
			
			IloLinearIntExpr sumAllAssignmentsPerStudentPerCourse = processAssignmentsPerStudentPerCourse(student, course); // Sum of all assignments for this student and this course
			sumAllAssignmentsPerCourse.put(course, sumAllAssignmentsPerStudentPerCourse);
			sumAllAssignmentsPerStudent.add(sumAllAssignmentsPerStudentPerCourse);
		}
		
		student.setSumAllAssignmentsPerCourse(sumAllAssignmentsPerCourse);
		return sumAllAssignmentsPerStudent;
	}
	
	// JPF09FEV2020
	// Generate expression with sum of course-group assignments (0/1) for this student,
	// restricted to optional courses
	private IloLinearIntExpr calcSumAssignmentsPerStudentOptional(Student student) throws IloException {
		IloLinearIntExpr sumAllAssignmentsPerStudentOptional = cplex.linearIntExpr();
		for (Course course : student.getEnrolledCourses())
			if (!course.getMandatory())
				sumAllAssignmentsPerStudentOptional.add(student.getSumAllAssignmentsPerCourse().get(course));
		return sumAllAssignmentsPerStudentOptional;
	}
	
	// Generate expression with sum of group assignments (0/1) for this student and course
	private IloLinearIntExpr processAssignmentsPerStudentPerCourse(Student student, Course course) throws IloException {
		IloLinearIntExpr sumAllAssignmentsPerStudentPerCourse = cplex.linearIntExpr();
		
		IloIntVar assignedToUnwantedGroup = cplex.boolVar(); // 1 if student gets assigned to an unwanted group, 0 otherwise
		IloLinearIntExpr assignmentsToUnwantedGroups = cplex.linearIntExpr(); // Sum of all assignment variables to unwanted groups
		
		Map<Group, IloIntVar> groupAssignments = new HashMap<>();
		
		for (Group group : course.getGroups().values()) {
			IloIntVar studentGroupAssignment = processAssignmentsPerStudentPerCoursePerGroup(student, course, group);
			sumAllAssignmentsPerStudentPerCourse.addTerm(1, studentGroupAssignment);
			groupAssignments.put(group, studentGroupAssignment);
			
			if (!student.getWantedCourseGroup(course, group)) { // If the student didn't want this group...
				assignmentsToUnwantedGroups.addTerm(1, studentGroupAssignment);
			}
		}
		
		// Variable 'assignedToUnwantedGroup' is 1 if the student was assigned to an unwanted group, 0 otherwise
		cplex.add(cplex.ifThen(cplex.ge(assignmentsToUnwantedGroups, 1), cplex.eq(assignedToUnwantedGroup, 1)));
		cplex.add(cplex.ifThen(cplex.eq(assignmentsToUnwantedGroups, 0), cplex.eq(assignedToUnwantedGroup, 0)));
		
		weightedSumAllAssignmentsToUnwantedGroups.addTerm(student.getAvgGrade(), assignedToUnwantedGroup);
		
		// CONSTRAINT: a student can be assigned to at most 1 group per course
		cplex.addLe(sumAllAssignmentsPerStudentPerCourse, 1); 
		
		student.getCourseGroupAssignments().put(course, groupAssignments);
		
		return sumAllAssignmentsPerStudentPerCourse;
	}


	private IloIntVar processAssignmentsPerStudentPerCoursePerGroup(Student student, Course course, Group group) throws IloException {
		IloIntVar studentGroupAssignment = cplex.boolVar("(" + student.getCode() + ": " + course.getCode() + "-" + group.getCode() + ")"); // VARIABLE: student assigned to this course-group pair?
		
		group.addTermToSumAllAssignedStudents(cplex, studentGroupAssignment, student);
		
		return studentGroupAssignment;
	}
	
	private IloIntVar processStudentPreference(Student student, StudentPreference preference, IloLinearIntExpr sumAllAssignmentsPerStudent) throws IloException {
		int preferenceOrder = preference.getOrder();
		int preferenceSize = preference.getSize();
		IloLinearIntExpr sumIndividualGroupAssignments = cplex.linearIntExpr();
		
		for (Map.Entry<Course, Group> preferenceCourseGroup : preference.getCourseGroupPairs().entrySet()) { // Get the course-group pair
			Course preferenceCourse = preferenceCourseGroup.getKey();
			Group preferenceGroup = preferenceCourseGroup.getValue();
			
			IloIntVar groupAssignment = student.getCourseGroupAssignments().get(preferenceCourse).get(preferenceGroup);
			sumIndividualGroupAssignments.addTerm(1, groupAssignment);
		}
		
		IloIntVar fulfilledPreference = cplex.boolVar("(Complete preference order " + preferenceOrder + " for " + student.getCode() + ")");
		preference.setWasFulfilled(fulfilledPreference);
		
		if (preferenceWeightingMode == PreferenceWeightingMode.EXPONENT) {
			weightedSumFulfilledPreferences.addTerm(Math.pow(2, student.getAvgGrade()) * (10 - (preferenceOrder - 1)), fulfilledPreference);
		}
		else if (preferenceWeightingMode == PreferenceWeightingMode.TIMES) {
			weightedSumFulfilledPreferences.addTerm(student.getAvgGrade() * (10 - (preferenceOrder - 1)), fulfilledPreference);
		}
		
		// CONSTRAINT: if sum of all assignments in this preference < number of course-group pairs in it
		// or sum of all assignments in this preference < sum of the student's total assignments,
		// then it's not completely fulfilled
		cplex.add(cplex.ifThen(cplex.or(
				cplex.le(sumIndividualGroupAssignments, preferenceSize - 1),
				cplex.le(sumIndividualGroupAssignments, cplex.sum(sumAllAssignmentsPerStudent, -1))),
				cplex.eq(fulfilledPreference, 0)));
		
		/*// CONSTRAINT: if sum of all assignments in this preference = number of course-group pairs in it
		// and sum of all assignments in this preference = sum of the student's total assignments,
		// then it is completely fulfilled
		cplex.add(cplex.ifThen(cplex.and(
				cplex.eq(sumIndividualGroupAssignments, preferenceSize),
				cplex.eq(sumIndividualGroupAssignments, sumAllAssignmentsPerStudent)),
				cplex.eq(fulfilledPreference, 1)));*/
		
		return fulfilledPreference;
	}
	
	private IloIntVar processStudentTimeslot(Student student, Timeslot timeslot) throws IloException {
		IloIntVar timeslotOccupied = cplex.boolVar(); // VARIABLE: student has this timeslot occupied?
		IloLinearIntExpr sumAllPracticalClasses = cplex.linearIntExpr(); // Sum of all practical classes for this student in this timeslot
		IloLinearIntExpr sumAllClasses = cplex.linearIntExpr(); // Sum of all classes for this student in this timeslot
		
		sumAllOccupiedTimeslots.addTerm(1, timeslotOccupied);
		
		for (Map.Entry<Course, Set<Group>> practicalClass : timeslot.getPracticalClasses().entrySet()) {
			Course course = practicalClass.getKey();
			
			if (student.getEnrolledCourses().contains(course)) {
				for (Group group : practicalClass.getValue()) {
					IloIntVar assignmentVariable = student.getCourseGroupAssignments().get(course).get(group);
					sumAllPracticalClasses.addTerm(1, assignmentVariable);
				}
			}
		}
		
		cplex.addLe(sumAllPracticalClasses, 1); // CONSTRAINT: a student can have at most 1 concurrent practical class
		sumAllClasses.add(sumAllPracticalClasses);
		
		for (Map.Entry<Course, Set<Group>> lectureClass : timeslot.getLectureClasses().entrySet()) {
			Course course = lectureClass.getKey();
			
			if (student.getEnrolledCourses().contains(course)) {
				for (Group group : lectureClass.getValue()) {
					IloIntVar assignmentVariable = student.getCourseGroupAssignments().get(course).get(group);
					sumAllClasses.addTerm(1, assignmentVariable);
				}
			}
		}
		
		cplex.add(cplex.ifThen(cplex.eq(sumAllClasses, 0), cplex.eq(timeslotOccupied, 0))); // CONSTRAINT: if sum of all classes in this timeslot = 0, the timeslot isn't occupied
		cplex.add(cplex.ifThen(cplex.not(cplex.eq(sumAllClasses, 0)), cplex.eq(timeslotOccupied, 1))); // CONSTRAINT: if sum of all classes in this timeslot != 0, the timeslot is occupied
		
		return timeslotOccupied;
	}
	
	// generate hard and soft occupation constraints for a mandatory course
	private int processCourseMandatory(Course course) throws IloException {
		int numStudentsEnrolledThisCourse = course.getNumEnrollments();
		int sumGroupCapacitiesThisCourse = course.calculateSumGroupCapacities();
		
		for (Group group : course.getGroups().values()) {
			processCourseGroupMandatory(course, group, numStudentsEnrolledThisCourse, sumGroupCapacitiesThisCourse);
		}
		
		return numStudentsEnrolledThisCourse;
	}
	
	private void processCourseGroupMandatory(Course course, Group group, int numStudentsEnrolledThisCourse, int sumGroupCapacitiesThisCourse) throws IloException {
		IloLinearIntExpr sumAllAssignedStudents = group.getSumAllAssignedStudents();
		int groupCapacity = group.getCapacity();

		if (sumAllAssignedStudents == null) 
			return; // Some courses might not have candidate students
		
		if (!isMandatoryAssignment || course.getMandatory()) {
			// CONSTRAINT: sum of all assigned students <= group's capacity
			cplex.addLe(sumAllAssignedStudents, groupCapacity); 

			// Constraints for DiffOddEven per course-group - JPF22SET2020
			// Neste momento hardcoded só para 2º a 4º ano, mas mais tarde vira no input
			
			if ((group.getCode().charAt(0) == '2'
				|| group.getCode().charAt(0) == '3'
				|| group.getCode().charAt(0) == '4') && !course.getCode().equals("EIC0106"))//LGPR
			{
				// JPF22SET2020
				IloLinearIntExpr sumAllAssignedStudentsOdd = group.getSumAllAssignedStudentsOdd();
				IloLinearIntExpr sumAllAssignedStudentsEven = group.getSumAllAssignedStudentsEven();
				IloIntVar diffOddEven = cplex.intVar(0, groupCapacity/*+10000*/); 
				
				sumGroupCapacity += groupCapacity; // JPF04FEV2021
				cplex.addLe(cplex.diff(sumAllAssignedStudentsOdd, sumAllAssignedStudentsEven), diffOddEven);
				cplex.addLe(cplex.diff(sumAllAssignedStudentsEven, sumAllAssignedStudentsOdd), diffOddEven);
				sumDiffOddEven.addTerm(1, diffOddEven);
				cplex.addLe(diffOddEven, 5); // hardcoded was 4
				
			}
			
		}
		// Else (if we're assigning mandatory courses but this course is optional), 
		// don't add a constraint for the group capacity, since we know for sure everyone fits
		
		float targetNumStudentsAssigned = ((float)groupCapacity / (float)sumGroupCapacitiesThisCourse) * numStudentsEnrolledThisCourse;
		
		IloNumVar groupUtilizationSlack = cplex.numVar(0, targetNumStudentsAssigned);
		sumAllGroupUtilizationSlacks.addTerm(1, groupUtilizationSlack);
		
		// SOFT CONSTRAINT: try to balance students assigned to groups according to each group's target minimum utilization of the total group capacities for the same course
		cplex.addGe(cplex.sum(sumAllAssignedStudents, groupUtilizationSlack), targetNumStudentsAssigned);
		

	}
	
	// Generate occupation constraints for optional courses
	private void processCourseOptional(Course course) throws IloException {
		for (Group group : course.getGroups().values()) {
			processCourseGroupOptional(course, group);
		}
	}

	// Generate occupation constraints for optional course groups
	private void processCourseGroupOptional(Course course, Group group) throws IloException {
		IloLinearIntExpr sumAllAssignedStudents = group.getSumAllAssignedStudents();
		int groupCapacity = group.getCapacity();
		
		if (sumAllAssignedStudents == null) 
			return; // Some courses might not have candidate students
		
		// CONSTRAINT: sum of all assigned students <= group's capacity
		cplex.addLe(sumAllAssignedStudents, groupCapacity); 
		
		// Impose a minimum group utilization constraint??
		float groupMinUtilization = 0.1f;		
		cplex.addGe(sumAllAssignedStudents, groupMinUtilization * groupCapacity);
	}
	
	private void solve() throws IOException, IloException {
		cplex.setParam(IloCplex.DoubleParam.TiLim, timeout); 
		
		// Solve the problem
		if (cplex.solve()) {
			System.out.println();
			System.out.println("Solution found by CPLEX is " + cplex.getStatus() + ".");
			
			// TODO: DEBUG
			System.out.println();
			
			// Print the value of each optimization goal (0 to 1)
			if (objMaximizeSumAllAssignments != null)
				System.out.println("objMaximizeSumAllAssignments = " + cplex.getValue(objMaximizeSumAllAssignments));
			if (objMaximizeCompleteStudents != null)
				System.out.println("objMaximizeCompleteStudents = " + cplex.getValue(objMaximizeCompleteStudents));
			if (objMaximizeOccupiedTimeslots != null)
				System.out.println("objMaximizeOccupiedTimeslots = " + cplex.getValue(objMaximizeOccupiedTimeslots));
			if (objMaximizeFulfilledPreferences != null)
				System.out.println("objMaximizeFulfilledPreferences = " + cplex.getValue(objMaximizeFulfilledPreferences));
			if (objMinimizeGroupUtilizationSlacks != null)
				System.out.println("objMinimizeGroupUtilizationSlacks = " + cplex.getValue(objMinimizeGroupUtilizationSlacks));
			if (objMinimizeOccupiedPeriodsWithNoPreferenceAssigned != null)
				System.out.println("objMinimizeOccupiedPeriodsWithNoPreferenceAssigned = " + cplex.getValue(objMinimizeOccupiedPeriodsWithNoPreferenceAssigned));
			if (objMinimizeUnwantedOccupiedPeriods != null)
				System.out.println("objMinimizeUnwantedOccupiedPeriods = " + cplex.getValue(objMinimizeUnwantedOccupiedPeriods));
			if (objMinimizeAssignmentsToUnwantedGroups != null)
				System.out.println("objMinimizeAssignmentsToUnwantedGroups = " + cplex.getValue(objMinimizeAssignmentsToUnwantedGroups));
			if (objMinDiffOddEven != null)
				System.out.println("objMinDiffOddEven= " + cplex.getValue(objMinDiffOddEven));
			
			System.out.println("weightedSumAllAssignments = " + cplex.getValue(weightedSumAllAssignments));
			System.out.println("weightedSumAllCompleteStudents = " + cplex.getValue(weightedSumAllCompleteStudents));
			System.out.println("sumAllOccupiedTimeslots = " + cplex.getValue(sumAllOccupiedTimeslots));
			System.out.println("weightedSumFulfilledPreferences = " + cplex.getValue(weightedSumFulfilledPreferences));
			System.out.println("sumAllGroupUtilizationSlacks = " + cplex.getValue(sumAllGroupUtilizationSlacks));
			System.out.println("sumAllOccupiedPeriodsWithNoPreferenceAssigned = " + cplex.getValue(sumAllOccupiedPeriodsWithNoPreferenceAssigned));
			System.out.println("sumAllUnwantedOccupiedPeriods = " + cplex.getValue(sumAllUnwantedOccupiedPeriods));
			System.out.println("weightedSumAllAssignmentsToUnwantedGroups = " + cplex.getValue(weightedSumAllAssignmentsToUnwantedGroups));
			if (sumDiffOddEven!= null)
				System.out.println("sumDiffOddEven= " + cplex.getValue(sumDiffOddEven));
			System.out.println("weightMinDiffOddEven = " + weightMinDiffOddEven);
			
			writer.writeOutputData();
		}
		else {
			System.out.println("Failed to solve problem.");
		}
		
		// Free CPLEX resources
		cplex.end();
	}
}

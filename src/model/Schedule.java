package model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Schedule implements Iterable<Timeslot> {
	private List<List<Timeslot>> schedule;
	
	public Schedule() {
		schedule = new ArrayList<>();
		
		for (int day = 0; day < 6; ++day) {
			List<Timeslot> dayList = new ArrayList<>();
			
			for (int timeslot = 0; timeslot < 25; ++timeslot) {
				dayList.add(new Timeslot());
			}
			
			schedule.add(dayList);
		}
	}
	
	public Timeslot getTimeslot(int day, int timeslot) {
		return schedule.get(day).get(timeslot);
	}
	
	public void addCourseGroup(Course course, Group group, boolean practicalClass, int weekDay, int startTime, int duration) {
		for (int i = 0; i < duration; ++i) {
			Timeslot timeslot = getTimeslot(weekDay, startTime + i);
			
			Map<Course, Set<Group>> timeslotClasses = (practicalClass ? timeslot.getPracticalClasses() : timeslot.getLectureClasses());
			
			Set<Group> timeslotCourseGroups = timeslotClasses.get(course);
			if (timeslotCourseGroups == null) timeslotCourseGroups = new HashSet<>();
			
			timeslotCourseGroups.add(group);
			
			if (practicalClass) {
				timeslot.addPracticalClass(course, group);
			}
			else {
				timeslot.addLectureClass(course, group);
			}
		}
	}
	
	public Iterator<Timeslot> iterator() {
		return new ScheduleIterator();
	}
	
	private class ScheduleIterator implements Iterator<Timeslot> {
		int currentDay = 0, currentTimeslot = 0;
		
		@Override
		public boolean hasNext() {
			if (currentDay < 6 && currentTimeslot < 25) return true;
			else return false;
		}

		@Override
		public Timeslot next() {
			Timeslot toReturn = Schedule.this.schedule.get(currentDay).get(currentTimeslot);
			
			if (currentTimeslot == 24) {
				++currentDay;
				currentTimeslot = 0;
			}
			else {
				++currentTimeslot;
			}
			
			return toReturn;
		}
	}
}
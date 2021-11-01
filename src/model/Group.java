package model;

import java.util.HashSet;
import java.util.Set;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearIntExpr;
import ilog.cplex.IloCplex;


public class Group {
	private String code;
	private int capacity;
	private IloLinearIntExpr sumAllAssignedStudents; // Sum of all decision variables indicating whether a student has been assigned to this group
	private IloLinearIntExpr sumAllAssignedStudentsEven; // with even code
	private IloLinearIntExpr sumAllAssignedStudentsOdd; // with even code
	private Set<Integer> occupiedPeriods;

	
	public Group(String code, int capacity) {
		this.code = code;
		this.capacity = capacity;
		this.occupiedPeriods = new HashSet<>();
	}
	
	public String getCode() {
		return code;
	}
	
	public int getCapacity() {
		return capacity;
	}
	
	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}
	
	public IloLinearIntExpr getSumAllAssignedStudents() {
		return sumAllAssignedStudents;
	}
	
	public void addTermToSumAllAssignedStudents(IloCplex cplex, IloIntVar var_preferenceAssigned, Student student) throws IloException {
		if (sumAllAssignedStudents == null) 
			sumAllAssignedStudents = cplex.linearIntExpr();
		if (sumAllAssignedStudentsOdd == null) 
			sumAllAssignedStudentsOdd = cplex.linearIntExpr();
		if (sumAllAssignedStudentsEven == null) 
			sumAllAssignedStudentsEven = cplex.linearIntExpr();
		
		sumAllAssignedStudents.addTerm(1, var_preferenceAssigned);
		
		// JPF 22SET2020
		if (student.isEven())
			sumAllAssignedStudentsEven.addTerm(1, var_preferenceAssigned);
		if (student.isOdd())
			sumAllAssignedStudentsOdd.addTerm(1, var_preferenceAssigned);
	}
	
	public Set<Integer> getOccupiedPeriods() {
		return occupiedPeriods;
	}
	
	public void addOccupiedPeriod(int period) {
		occupiedPeriods.add(period);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Group) {
			return ((Group) obj).code.equals(code);
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

	public IloLinearIntExpr getSumAllAssignedStudentsOdd() {
		return sumAllAssignedStudentsOdd;
	}

	public IloLinearIntExpr getSumAllAssignedStudentsEven() {
		return sumAllAssignedStudentsEven;
	}
}

package ui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import ilog.concert.IloException;
import io.InputDataReader.InvalidInputDataException;
import net.miginfocom.swing.MigLayout;
import problem.AssignmentProblem;

public class MainWindow {
	private JFrame frmGroupAssignment;
	private JLabel lblGroupSchedules;
	private JLabel lblGroupComposites;
	private JLabel lblStudentPreferences;
	private JLabel lblStudentGrades;
	private JLabel lblEnrollments;
	private JTextField txtGroupSchedules;
	private JTextField txtGroups;
	private JTextField txtGroupComposites;
	private JTextField txtStudentPreferences;
	private JTextField txtStudentGrades;
	private JTextField txtEnrollments;
	
	private JButton btnGroupSchedules;
	private JButton btnGroupComposites;
	private JButton btnStudentPreferences;
	private JButton btnStudentGrades;
	private JButton btnEnrollments;
	
	private JLabel lblCourses;
	private JTextField txtCourses;
	private JButton btnCourses;
	private JTabbedPane tabbedPane;
	private JPanel panelObjectiveWeights;
	private JPanel panelGeneral;
	private JLabel lblSemester;
	private JRadioButton radioButton1stSemester;
	private JRadioButton radioButton2ndSemester;
	private JLabel lblProcVersion;
	private JTextField txtProcVersion;
	private JLabel lblCourseTypes;
	private JLabel lblObjMaximizeSumAllAssignments;
	private JTextField txtObjMaximizeSumAllAssignments;
	private JLabel lblObjMaximizeCompleteStudents;
	private JTextField txtObjMaximizeCompleteStudents;
	private JLabel lblObjMaximizeOccupiedTimeslots;
	private JTextField txtObjMaximizeOccupiedTimeslots;
	private JLabel lblObjMaximizeFulfilledPreferences;
	private JTextField txtObjMaximizeFulfilledPreferences;
	private JLabel lblObjMinimizeGroupUtilizationSlacks;
	private JTextField txtObjMinimizeGroupUtilizationSlacks;
	private JLabel lblObjMinimizeOccupiedPeriodsWithNoPreferenceAssigned;
	private JTextField txtObjMinimizeOccupiedPeriodsWithNoPreferenceAssigned;
	private JLabel lblObjMinimizeUnwantedOccupiedPeriods;
	private JTextField txtObjMinimizeUnwantedOccupiedPeriods;
	private JLabel lblObjMinimizeAssignmentsToUnwantedGroups;
	private JTextField txtObjMinimizeAssignmentsToUnwantedGroups;
	
	private JLabel lblObjMinDiffOddEven; // JPF22SET2020
	private JTextField txtObjMinDiffOddEven; // JPF22SET2020
	
	private JLabel lblTimeout;
	private JTextField txtTimeout;
	
	private JLabel lblOutput;
	private JTextField txtOutput;
	private JButton btnOutput;
	private JPanel panelOutput;
	private JTextArea txtOutputArea;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Throwable e) {
			e.printStackTrace();
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainWindow window = new MainWindow();
					window.frmGroupAssignment.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	/**
	 * Create the application.
	 */
	public MainWindow() {
		initialize();
	}
	
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmGroupAssignment = new JFrame();
		frmGroupAssignment.setTitle("Atribui��o de turmas MIEIC");
		frmGroupAssignment.setResizable(false);
		frmGroupAssignment.setBounds(100, 100, 450, 350);
		frmGroupAssignment.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmGroupAssignment.getContentPane().setLayout(new BorderLayout(0, 0));
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		frmGroupAssignment.getContentPane().add(tabbedPane, BorderLayout.CENTER);
		
		panelGeneral = new JPanel();
		tabbedPane.addTab("Geral", null, panelGeneral, null);
		
		lblSemester = new JLabel("Semestre:");
		
		radioButton1stSemester = new JRadioButton("1.�");
		radioButton1stSemester.setSelected(true);
		
		radioButton2ndSemester = new JRadioButton("2.�");
		
		ButtonGroup semesterRadioButtons = new ButtonGroup();
		semesterRadioButtons.add(radioButton1stSemester);
		semesterRadioButtons.add(radioButton2ndSemester);
		
		lblProcVersion = new JLabel("N.� de vers�o do processo:");
		
		txtProcVersion = new JTextField();
		txtProcVersion.setHorizontalAlignment(SwingConstants.CENTER);
		txtProcVersion.setColumns(2);
		
		lblCourseTypes = new JLabel("Atribui��o de unidades curriculares:");
		
		JRadioButton radioButtonOptionalCourses = new JRadioButton("Optativas");
		radioButtonOptionalCourses.setSelected(true);
		
		JRadioButton radioButtonMandatoryCourses = new JRadioButton("Obrigat�rias");
		
		ButtonGroup courseTypeRadioButtons = new ButtonGroup();
		courseTypeRadioButtons.add(radioButtonOptionalCourses);
		courseTypeRadioButtons.add(radioButtonMandatoryCourses);
		
		GroupLayout gl_panelGeneral = new GroupLayout(panelGeneral);
		gl_panelGeneral.setHorizontalGroup(
				gl_panelGeneral.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelGeneral.createSequentialGroup()
						.addContainerGap()
						.addGroup(gl_panelGeneral.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_panelGeneral.createSequentialGroup()
										.addComponent(lblSemester)
										.addPreferredGap(ComponentPlacement.UNRELATED)
										.addComponent(radioButton1stSemester)
										.addPreferredGap(ComponentPlacement.UNRELATED)
										.addComponent(radioButton2ndSemester))
								.addGroup(gl_panelGeneral.createSequentialGroup()
										.addComponent(lblProcVersion)
										.addGap(10)
										.addComponent(txtProcVersion, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
								.addGroup(gl_panelGeneral.createSequentialGroup()
										.addComponent(lblCourseTypes)
										.addPreferredGap(ComponentPlacement.UNRELATED)
										.addComponent(radioButtonOptionalCourses)
										.addPreferredGap(ComponentPlacement.UNRELATED)
										.addComponent(radioButtonMandatoryCourses)))
						.addContainerGap(22, Short.MAX_VALUE))
				);
		gl_panelGeneral.setVerticalGroup(
				gl_panelGeneral.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelGeneral.createSequentialGroup()
						.addContainerGap()
						.addGroup(gl_panelGeneral.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblSemester)
								.addComponent(radioButton1stSemester)
								.addComponent(radioButton2ndSemester))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(gl_panelGeneral.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblProcVersion)
								.addComponent(txtProcVersion, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(gl_panelGeneral.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblCourseTypes)
								.addComponent(radioButtonOptionalCourses)
								.addComponent(radioButtonMandatoryCourses))
						.addContainerGap(161, Short.MAX_VALUE))
				);
		panelGeneral.setLayout(gl_panelGeneral);
		
		JPanel panelFiles = new JPanel(new MigLayout("", "[][418.00,grow][]", "[][][][][][][][]"));
		
		lblCourses = new JLabel("Unidades curriculares:");
		panelFiles.add(lblCourses, "cell 0 0,alignx trailing");
		
		txtCourses = new JTextField();
		panelFiles.add(txtCourses, "cell 1 0,growx");
		txtCourses.setColumns(10);
		
		btnCourses = new JButton("Procurar...");
		panelFiles.add(btnCourses, "cell 2 0");
		btnCourses.addActionListener(new CsvBrowseButtonListener(txtCourses));
		
		JLabel lblGroups = new JLabel("Turmas:");
		panelFiles.add(lblGroups, "cell 0 1,alignx trailing,aligny center");
		
		txtGroups = new JTextField();
		panelFiles.add(txtGroups, "cell 1 1,growx");
		txtGroups.setColumns(10);
		
		JButton btnGroups = new JButton("Procurar...");
		panelFiles.add(btnGroups, "cell 2 1,alignx left,aligny top");
		btnGroups.addActionListener(new CsvBrowseButtonListener(txtGroups));
		
		lblGroupSchedules = new JLabel("Hor�rios das turmas:");
		panelFiles.add(lblGroupSchedules, "cell 0 2,alignx trailing");
		
		txtGroupSchedules = new JTextField();
		panelFiles.add(txtGroupSchedules, "cell 1 2,growx");
		txtGroupSchedules.setColumns(10);
		
		btnGroupSchedules = new JButton("Procurar...");
		panelFiles.add(btnGroupSchedules, "cell 2 2");
		btnGroupSchedules.addActionListener(new CsvBrowseButtonListener(txtGroupSchedules));
		
		lblGroupComposites = new JLabel("Compostos de turmas:");
		panelFiles.add(lblGroupComposites, "cell 0 3,alignx trailing");
		
		txtGroupComposites = new JTextField();
		panelFiles.add(txtGroupComposites, "cell 1 3,growx");
		txtGroupComposites.setColumns(10);
		
		btnGroupComposites = new JButton("Procurar...");
		panelFiles.add(btnGroupComposites, "cell 2 3");
		btnGroupComposites.addActionListener(new CsvBrowseButtonListener(txtGroupComposites));
		
		lblStudentPreferences = new JLabel("Escolhas dos alunos:");
		panelFiles.add(lblStudentPreferences, "cell 0 4,alignx trailing");
		
		txtStudentPreferences = new JTextField();
		panelFiles.add(txtStudentPreferences, "cell 1 4,growx");
		txtStudentPreferences.setColumns(10);
		
		btnStudentPreferences = new JButton("Procurar...");
		panelFiles.add(btnStudentPreferences, "cell 2 4");
		btnStudentPreferences.addActionListener(new CsvBrowseButtonListener(txtStudentPreferences));
		
		lblStudentGrades = new JLabel("M�dias dos alunos:");
		panelFiles.add(lblStudentGrades, "cell 0 5,alignx trailing");

		txtStudentGrades = new JTextField();
		panelFiles.add(txtStudentGrades, "cell 1 5,growx");
		txtStudentGrades.setColumns(10);
		
		btnStudentGrades = new JButton("Procurar...");
		panelFiles.add(btnStudentGrades, "cell 2 5");
		btnStudentGrades.addActionListener(new CsvBrowseButtonListener(txtStudentGrades));

		lblEnrollments = new JLabel("Inscri��es em UCs:");
		panelFiles.add(lblEnrollments, "cell 0 6,alignx trailing");

		txtEnrollments = new JTextField();
		panelFiles.add(txtEnrollments, "cell 1 6,growx");
		txtEnrollments.setColumns(10);
		
		btnEnrollments = new JButton("Procurar...");
		panelFiles.add(btnEnrollments, "cell 2 6");
		btnEnrollments.addActionListener(new CsvBrowseButtonListener(txtEnrollments));

		
		tabbedPane.addTab("Ficheiros", panelFiles);
		
		lblOutput = new JLabel("Diret�rio de sa�da:");
		panelFiles.add(lblOutput, "cell 0 7,alignx trailing");
		
		txtOutput = new JTextField();
		panelFiles.add(txtOutput, "cell 1 7,growx");
		txtOutput.setColumns(10);
		
		btnOutput = new JButton("Procurar...");
		panelFiles.add(btnOutput, "cell 2 7");
		btnOutput.addActionListener(new DirectoryBrowseButtonListener(txtOutput));
		
		panelObjectiveWeights = new JPanel();
		tabbedPane.addTab("Pesos dos crit�rios e par�metros", null, panelObjectiveWeights, null);
		panelObjectiveWeights.setLayout(new MigLayout("", "[][grow]", "[][][][][][][][][][]")); // JPF22SET2020
		
		lblObjMaximizeSumAllAssignments = new JLabel("objMaximizeSumAllAssignments:");
		panelObjectiveWeights.add(lblObjMaximizeSumAllAssignments, "cell 0 0,alignx trailing");
		
		txtObjMaximizeSumAllAssignments = new JTextField();
		txtObjMaximizeSumAllAssignments.setHorizontalAlignment(SwingConstants.CENTER);
		txtObjMaximizeSumAllAssignments.setText("25");
		panelObjectiveWeights.add(txtObjMaximizeSumAllAssignments, "flowx,cell 1 0,alignx leading");
		txtObjMaximizeSumAllAssignments.setColumns(3);
		
		lblObjMaximizeCompleteStudents = new JLabel("objMaximizeCompleteStudents:");
		panelObjectiveWeights.add(lblObjMaximizeCompleteStudents, "cell 0 1,alignx trailing");
		
		txtObjMaximizeCompleteStudents = new JTextField();
		txtObjMaximizeCompleteStudents.setText("10");
		txtObjMaximizeCompleteStudents.setHorizontalAlignment(SwingConstants.CENTER);
		panelObjectiveWeights.add(txtObjMaximizeCompleteStudents, "cell 1 1,alignx leading");
		txtObjMaximizeCompleteStudents.setColumns(3);
		
		lblObjMaximizeOccupiedTimeslots = new JLabel("objMaximizeOccupiedTimeslots:");
		panelObjectiveWeights.add(lblObjMaximizeOccupiedTimeslots, "cell 0 2,alignx trailing");
		
		txtObjMaximizeOccupiedTimeslots = new JTextField();
		txtObjMaximizeOccupiedTimeslots.setText("10");
		txtObjMaximizeOccupiedTimeslots.setHorizontalAlignment(SwingConstants.CENTER);
		panelObjectiveWeights.add(txtObjMaximizeOccupiedTimeslots, "cell 1 2,alignx leading");
		txtObjMaximizeOccupiedTimeslots.setColumns(3);
		
		lblObjMaximizeFulfilledPreferences = new JLabel("objMaximizeFulfilledPreferences:");
		panelObjectiveWeights.add(lblObjMaximizeFulfilledPreferences, "cell 0 3,alignx trailing");
		
		txtObjMaximizeFulfilledPreferences = new JTextField();
		txtObjMaximizeFulfilledPreferences.setText("10");
		txtObjMaximizeFulfilledPreferences.setHorizontalAlignment(SwingConstants.CENTER);
		panelObjectiveWeights.add(txtObjMaximizeFulfilledPreferences, "cell 1 3,alignx leading");
		txtObjMaximizeFulfilledPreferences.setColumns(3);
		
		lblObjMinimizeGroupUtilizationSlacks = new JLabel("objMinimizeGroupUtilizationSlacks:");
		panelObjectiveWeights.add(lblObjMinimizeGroupUtilizationSlacks, "cell 0 4,alignx trailing");
		
		txtObjMinimizeGroupUtilizationSlacks = new JTextField();
		txtObjMinimizeGroupUtilizationSlacks.setText("15");
		txtObjMinimizeGroupUtilizationSlacks.setHorizontalAlignment(SwingConstants.CENTER);
		panelObjectiveWeights.add(txtObjMinimizeGroupUtilizationSlacks, "cell 1 4,alignx leading");
		txtObjMinimizeGroupUtilizationSlacks.setColumns(3);
		
		lblObjMinimizeOccupiedPeriodsWithNoPreferenceAssigned = new JLabel("objMinimizeOccupiedPeriodsWithNoPreferenceAssigned:");
		panelObjectiveWeights.add(lblObjMinimizeOccupiedPeriodsWithNoPreferenceAssigned, "cell 0 5,alignx trailing");
		
		txtObjMinimizeOccupiedPeriodsWithNoPreferenceAssigned = new JTextField();
		txtObjMinimizeOccupiedPeriodsWithNoPreferenceAssigned.setText("10");
		txtObjMinimizeOccupiedPeriodsWithNoPreferenceAssigned.setHorizontalAlignment(SwingConstants.CENTER);
		panelObjectiveWeights.add(txtObjMinimizeOccupiedPeriodsWithNoPreferenceAssigned, "cell 1 5,alignx leading");
		txtObjMinimizeOccupiedPeriodsWithNoPreferenceAssigned.setColumns(3);
		
		lblObjMinimizeUnwantedOccupiedPeriods = new JLabel("objMinimizeUnwantedOccupiedPeriods:");
		panelObjectiveWeights.add(lblObjMinimizeUnwantedOccupiedPeriods, "cell 0 6,alignx trailing");
		
		txtObjMinimizeUnwantedOccupiedPeriods = new JTextField();
		txtObjMinimizeUnwantedOccupiedPeriods.setText("10");
		txtObjMinimizeUnwantedOccupiedPeriods.setHorizontalAlignment(SwingConstants.CENTER);
		panelObjectiveWeights.add(txtObjMinimizeUnwantedOccupiedPeriods, "cell 1 6,alignx leading");
		txtObjMinimizeUnwantedOccupiedPeriods.setColumns(3);
		
		lblObjMinimizeAssignmentsToUnwantedGroups = new JLabel("objMinimizeAssignmentsToUnwantedGroups:");
		panelObjectiveWeights.add(lblObjMinimizeAssignmentsToUnwantedGroups, "cell 0 7,alignx trailing");
		
		txtObjMinimizeAssignmentsToUnwantedGroups = new JTextField();
		txtObjMinimizeAssignmentsToUnwantedGroups.setText("10");
		txtObjMinimizeAssignmentsToUnwantedGroups.setHorizontalAlignment(SwingConstants.CENTER);
		panelObjectiveWeights.add(txtObjMinimizeAssignmentsToUnwantedGroups, "cell 1 7,alignx leading");
		txtObjMinimizeAssignmentsToUnwantedGroups.setColumns(3);

		// JPF22SET2020
		lblObjMinDiffOddEven = new JLabel("objMinDiffOddEven:");
		panelObjectiveWeights.add(lblObjMinDiffOddEven, "cell 0 8,alignx trailing");		
		txtObjMinDiffOddEven = new JTextField();
		txtObjMinDiffOddEven.setText("0");
		txtObjMinDiffOddEven.setHorizontalAlignment(SwingConstants.CENTER);
		panelObjectiveWeights.add(txtObjMinDiffOddEven, "cell 1 8,alignx leading");
		txtObjMinDiffOddEven.setColumns(3);

		
		lblTimeout = new JLabel("Timeout (seconds):");
		panelObjectiveWeights.add(lblTimeout, "cell 0 9,alignx trailing");

		txtTimeout = new JTextField();
		txtTimeout.setText("300");
		txtTimeout.setHorizontalAlignment(SwingConstants.CENTER);
		panelObjectiveWeights.add(txtTimeout, "cell 1 9,alignx leading");
		txtTimeout.setColumns(3);

		panelOutput = new JPanel();
		tabbedPane.addTab("Sa�da", null, panelOutput, null);
		panelOutput.setLayout(new BorderLayout(0, 0));
		
		txtOutputArea = new JTextArea();
		txtOutputArea.setEditable(false);
		PrintStream outStream = new PrintStream(new TextAreaOutputStream(txtOutputArea));
		System.setOut(outStream);
		
		JScrollPane outputScrollPane = new JScrollPane(txtOutputArea);
		panelOutput.add(outputScrollPane);
		
		JButton btnNewButton = new JButton("Executar");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int semester = radioButton1stSemester.isSelected() ? 1 : 2;
				boolean isMandatoryAssignment = radioButtonOptionalCourses.isSelected() ? false : true;
				
				try {
					AssignmentProblem problem = new AssignmentProblem(txtCourses.getText(), txtGroups.getText(), txtGroupSchedules.getText(), txtGroupComposites.getText(),
							txtStudentPreferences.getText(), txtStudentGrades.getText(), txtEnrollments.getText(), semester, txtProcVersion.getText(), isMandatoryAssignment,
							AssignmentProblem.PreferenceWeightingMode.EXPONENT, Float.parseFloat(txtObjMaximizeSumAllAssignments.getText()),
							Float.parseFloat(txtObjMaximizeCompleteStudents.getText()), Float.parseFloat(txtObjMaximizeOccupiedTimeslots.getText()),
							Float.parseFloat(txtObjMaximizeFulfilledPreferences.getText()), 
							Float.parseFloat(txtObjMinimizeGroupUtilizationSlacks.getText()),
							Float.parseFloat(txtObjMinimizeOccupiedPeriodsWithNoPreferenceAssigned.getText()), 
							Float.parseFloat(txtObjMinimizeUnwantedOccupiedPeriods.getText()),
							Float.parseFloat(txtObjMinimizeAssignmentsToUnwantedGroups.getText()), 
							Float.parseFloat(txtObjMinDiffOddEven.getText()), 
							txtOutput.getText() + File.separator,
							Integer.parseInt(txtTimeout.getText()));
					problem.run();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (IloException e) {
					e.printStackTrace();
				} catch (NumberFormatException e) {
					e.printStackTrace();
				} catch (InvalidInputDataException e) {
					e.printStackTrace();
				}
			}
		});
		frmGroupAssignment.getContentPane().add(btnNewButton, BorderLayout.SOUTH);
		
		try {
			frmGroupAssignment.setIconImage(ImageIO.read(new File("res" + File.separator + "icon_feup.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

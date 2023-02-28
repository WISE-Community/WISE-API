package org.wise.portal.presentation.web.controllers;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.easymock.Mock;
import org.junit.After;
import org.junit.Before;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.wise.portal.domain.Tag;
import org.wise.portal.domain.authentication.Gender;
import org.wise.portal.domain.authentication.MutableUserDetails;
import org.wise.portal.domain.authentication.Schoollevel;
import org.wise.portal.domain.authentication.impl.PersistentGrantedAuthority;
import org.wise.portal.domain.authentication.impl.StudentUserDetails;
import org.wise.portal.domain.authentication.impl.TeacherUserDetails;
import org.wise.portal.domain.group.Group;
import org.wise.portal.domain.group.impl.PersistentGroup;
import org.wise.portal.domain.project.Project;
import org.wise.portal.domain.project.impl.ProjectImpl;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.run.impl.RunImpl;
import org.wise.portal.domain.user.User;
import org.wise.portal.domain.user.impl.UserImpl;
import org.wise.portal.domain.workgroup.impl.WorkgroupImpl;
import org.wise.portal.service.authentication.UserDetailsService;
import org.wise.portal.service.group.GroupService;
import org.wise.portal.service.portal.PortalService;
import org.wise.portal.service.project.ProjectService;
import org.wise.portal.service.run.RunService;
import org.wise.portal.service.user.UserService;
import org.wise.portal.service.vle.wise5.VLEService;
import org.wise.portal.service.workgroup.WorkgroupService;

public abstract class APIControllerTest {

  protected final String ADMIN_USERNAME = "MrKrabb";
  protected final String RUN1_RUNCODE = "orca123";
  protected final String RUN2_RUNCODE = "panda123";
  protected final String RUN3_RUNCODE = "giraffe123";
  protected final String RUN1_PERIOD1_NAME = "1";
  protected final String RUN1_PERIOD2_NAME = "2";
  protected final String SHOULD_NOT_HAVE_THROWN_EXCEPTION = "Should not have thrown an exception";
  protected final String STUDENT_FIRSTNAME = "SpongeBob";
  protected final String STUDENT_LASTNAME = "Squarepants";
  protected final String STUDENT_PASSWORD = "studentPass";
  protected final String STUDENT_USERNAME = "SpongeBobS0101";
  protected final String STUDENT1_GOOGLE_ID = "google-user-12345";
  protected final String STUDENT2_FIRSTNAME = "Patrick";
  protected final String STUDENT2_LASTNAME = "Starr";
  protected final String STUDENT2_USERNAME = "PatrickS0101";
  protected final String TEACHER_FIRSTNAME = "Squidward";
  protected final String TEACHER_LASTNAME = "Tentacles";
  protected final String TEACHER_USERNAME = "SquidwardTentacles";
  protected final String TEACHER2_FIRSTNAME = "Sandy";
  protected final String TEACHER2_LASTNAME = "Cheeks";
  protected final String TEACHER2_USERNAME = "SandyCheeks";
  protected final String USERNAME_NOT_IN_DB = "usernameNotInDB";

  protected Authentication adminAuth, studentAuth, studentAuth2, teacherAuth, teacherAuth2;
  protected ProjectImpl project1, project2, project3;
  protected Long projectId1 = 1L;
  protected Long projectId2 = 2L;
  protected Long projectId3 = 3L;
  protected RunImpl run1, run2, run3;
  protected List<Tag> run1Tags;
  protected PersistentGroup run1Period1, run1Period2, run2Period2, run3Period4;
  protected Long run1Period1Id = 1L;
  protected Long run1Period2Id = 2L;
  protected Long run2Period2Id = 3L;
  protected Long run3Period4Id = 4L;
  protected Long runId1 = 1L;
  protected Long runId2 = 2L;
  protected Long runId3 = 3L;
  protected List<Run> runs;
  protected Long student1Id = 94678L;
  protected Long student2Id = 94679L;
  protected User student1, student2, teacher1, teacher2, admin1;
  protected StudentUserDetails student1UserDetails, student2UserDetails;
  protected Long teacher1Id = 94210L;
  protected Long teacher2Id = 94211L;
  protected TeacherUserDetails teacher1UserDetails, teacher2UserDetails, admin1UserDetails;
  protected WorkgroupImpl workgroup1, workgroup2, workgroup3, teacher1Run1Workgroup;
  protected Long workgroup1Id = 1L;
  protected Long workgroup2Id = 2L;
  protected Long workgroup3Id = 3L;

  protected String run1Node1Id = "run1Node1";

  protected String run1Component1Id = "run1Component1";

  @Mock
  protected Environment appProperties;

  @Mock
  protected GroupService groupService;

  @Mock
  protected PortalService portalService;

  @Mock
  protected ProjectService projectService;

  @Mock
  protected HttpServletRequest request;

  @Mock
  protected RunService runService;

  @Mock
  protected UserService userService;

  @Mock
  protected VLEService vleService;

  @Mock
  protected WorkgroupService workgroupService;

  @Before
  public void setUp() {
    createAdmin();
    createTeachers();
    createStudents();
    createProjects();
    createRuns();
    createWorkgroups();
  }

  private void createStudents() {
    student1UserDetails = createStudentUserDetails(STUDENT_FIRSTNAME, STUDENT_LASTNAME,
        STUDENT_USERNAME, Gender.MALE, 5, STUDENT1_GOOGLE_ID);
    student1 = createStudent(student1Id, student1UserDetails);
    studentAuth = createAuthentication(student1UserDetails);
    student2UserDetails = createStudentUserDetails(STUDENT2_FIRSTNAME, STUDENT2_LASTNAME,
        STUDENT2_USERNAME, Gender.MALE, 10, null);
    student2 = createStudent(student2Id, student2UserDetails);
    studentAuth2 = createAuthentication(student2UserDetails);
  }

  private void createTeachers() {
    teacher1UserDetails = createTeacherUserDetails(TEACHER_FIRSTNAME, TEACHER_LASTNAME,
        TEACHER_USERNAME, Schoollevel.COLLEGE, 5);
    teacher1 = createTeacher(teacher1Id, teacher1UserDetails);
    teacherAuth = createAuthentication(teacher1UserDetails);
    teacher2UserDetails = createTeacherUserDetails(TEACHER2_FIRSTNAME, TEACHER2_LASTNAME,
        TEACHER2_USERNAME, Schoollevel.COLLEGE, 5);
    teacher2 = createTeacher(teacher2Id, teacher2UserDetails);
    teacherAuth2 = createAuthentication(teacher2UserDetails);
  }

  private void createAdmin() {
    admin1UserDetails = createAdminUserDetails(ADMIN_USERNAME);
    admin1 = createAdmin(admin1UserDetails);
    adminAuth = createAuthentication(admin1UserDetails);
  }

  private void createProjects() {
    project1 = createProject(projectId1, "/1/project.json", teacher1, 5);
    project2 = createProject(projectId2, "/2/project.json", teacher2, 5);
    project3 = createProject(projectId3, "/3/project.json", teacher2, 5);
  }

  private void createRuns() {
    run1Period1 = createPeriod(run1Period1Id, RUN1_PERIOD1_NAME, student1);
    run1Period2 = createPeriod(run1Period2Id, RUN1_PERIOD2_NAME);
    HashSet<Group> run1Periods = new HashSet<Group>(Arrays.asList(run1Period1, run1Period2));
    run1 = createRun(runId1, teacher1, new Date(100), new Date(101), 3, RUN1_RUNCODE, project1,
        run1Periods);
    run2Period2 = createPeriod(run2Period2Id, "Run2Period2", student1);
    HashSet<Group> run2Periods = new HashSet<Group>(Arrays.asList(run2Period2));
    run2 = createRun(runId2, teacher1, new Date(200), new Date(201), 3, RUN2_RUNCODE, project2,
        run2Periods);
    run3Period4 = createPeriod(run3Period4Id, "Run3Period4", student1);
    HashSet<Group> run3Periods = new HashSet<Group>(Arrays.asList(run3Period4));
    run3 = createRun(runId3, teacher2, new Date(300), new Date(301), 3, RUN3_RUNCODE, project3,
        run3Periods);
    runs = Arrays.asList(run1, run2, run3);
  }

  private void createWorkgroups() {
    workgroup1 = createWorkgroup(workgroup1Id, run1, run1Period1, student1);
    workgroup2 = createWorkgroup(workgroup2Id, run1, run1Period1, student2);
    workgroup3 = createWorkgroup(workgroup3Id, run2, run2Period2, student1);
    teacher1Run1Workgroup = createTeacherWorkgroup(run1, teacher1);
  }

  @After
  public void tearDown() {
    student1 = null;
    student2 = null;
    teacher1 = null;
    teacher2 = null;
    run1 = null;
    run2 = null;
    run3 = null;
    runs = null;
  }

  public APIControllerTest() {
  }

  protected User createStudent(Long id, StudentUserDetails studentUserDetails) {
    User student = new UserImpl();
    student.setId(id);
    student.setUserDetails(studentUserDetails);
    return student;
  }

  protected WorkgroupImpl createWorkgroup(Long id, Run run, Group period) {
    WorkgroupImpl workgroup = new WorkgroupImpl();
    workgroup.setId(id);
    workgroup.setRun(run);
    workgroup.setPeriod(period);
    return workgroup;
  }

  protected WorkgroupImpl createWorkgroup(Long id, Run run, Group period, User student) {
    WorkgroupImpl workgroup = createWorkgroup(id, run, period);
    workgroup.addMember(student);
    return workgroup;
  }

  protected WorkgroupImpl createTeacherWorkgroup(Run run, User teacher) {
    WorkgroupImpl workgroup = new WorkgroupImpl();
    workgroup.setRun(run);
    workgroup.addMember(teacher);
    return workgroup;
  }

  protected StudentUserDetails createStudentUserDetails(String firstName, String lastName,
      String username, Gender gender, Integer numberOfLogins, String googleUserId) {
    StudentUserDetails studentUserDetails = new StudentUserDetails();
    studentUserDetails.setFirstname(firstName);
    studentUserDetails.setLastname(lastName);
    studentUserDetails.setUsername(username);
    studentUserDetails.setGender(gender);
    studentUserDetails.setNumberOfLogins(numberOfLogins);
    PersistentGrantedAuthority studentAuthority = new PersistentGrantedAuthority();
    studentAuthority.setAuthority(UserDetailsService.STUDENT_ROLE);
    studentUserDetails.setAuthorities(new GrantedAuthority[] { studentAuthority });
    studentUserDetails.setGoogleUserId(googleUserId);
    return studentUserDetails;
  }

  protected User createTeacher(Long id, TeacherUserDetails teacherUserDetails) {
    User teacher = new UserImpl();
    teacher.setId(id);
    teacher.setUserDetails(teacherUserDetails);
    return teacher;
  }

  protected TeacherUserDetails createTeacherUserDetails(String firstName, String lastName,
      String username, Schoollevel schoolLevel, Integer numberOfLogins) {
    TeacherUserDetails teacherUserDetails = new TeacherUserDetails();
    teacherUserDetails.setFirstname(firstName);
    teacherUserDetails.setLastname(lastName);
    teacherUserDetails.setUsername(username);
    teacherUserDetails.setSchoollevel(schoolLevel);
    teacherUserDetails.setNumberOfLogins(numberOfLogins);
    PersistentGrantedAuthority teacherAuthority = new PersistentGrantedAuthority();
    teacherAuthority.setAuthority(UserDetailsService.TEACHER_ROLE);
    teacherUserDetails.setAuthorities(new GrantedAuthority[] { teacherAuthority });
    return teacherUserDetails;
  }

  protected User createAdmin(TeacherUserDetails teacherUserDetails) {
    User admin = new UserImpl();
    admin.setUserDetails(teacherUserDetails);
    return admin;
  }

  protected TeacherUserDetails createAdminUserDetails(String username) {
    TeacherUserDetails adminUserDetails = new TeacherUserDetails();
    PersistentGrantedAuthority adminAuthority = new PersistentGrantedAuthority();
    adminAuthority.setAuthority(UserDetailsService.ADMIN_ROLE);
    adminUserDetails.setAuthorities(new GrantedAuthority[] { adminAuthority });
    adminUserDetails.setUsername(username);
    return adminUserDetails;
  }

  protected Authentication createAuthentication(MutableUserDetails userDetails) {
    return new TestingAuthenticationToken(userDetails, null);
  }

  protected RunImpl createRun(Long id, User owner, Date startTime, Date lastRunTime,
      Integer maxWorkgroupSize, String runCode, Project project, HashSet<Group> periods) {
    RunImpl run = new RunImpl();
    run.setId(id);
    run.setOwner(owner);
    run.setStarttime(startTime);
    run.setLastRun(lastRunTime);
    run.setMaxWorkgroupSize(maxWorkgroupSize);
    run.setRuncode(runCode);
    run.setProject(project);
    run.setPeriods(periods);
    return run;
  }

  protected ProjectImpl createProject(Long id, String modulePath, User teacher,
      Integer wiseVersion) {
    ProjectImpl project = new ProjectImpl();
    project.setId(id);
    project.setModulePath(modulePath);
    project.setOwner(teacher);
    project.setWISEVersion(wiseVersion);
    project.setMaxTotalAssetsSize(15728640L);
    return project;
  }

  protected PersistentGroup createPeriod(Long id, String name) {
    PersistentGroup period = new PersistentGroup();
    period.setId(id);
    period.setName(name);
    return period;
  }

  protected PersistentGroup createPeriod(Long id, String name, User student) {
    PersistentGroup period = createPeriod(id, name);
    period.addMember(student);
    return period;
  }
}

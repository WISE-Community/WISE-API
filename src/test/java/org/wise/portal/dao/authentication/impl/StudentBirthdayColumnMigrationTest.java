package org.wise.portal.dao.authentication.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.Date;
import java.util.Properties;

import javax.sql.DataSource;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.wise.portal.domain.authentication.Gender;
import org.wise.portal.domain.authentication.impl.StudentUserDetails;
import org.wise.portal.junit.AbstractTransactionalDbTests;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:studentBirthdayColumnMigrationTest;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL",
    "spring.flyway.enabled=false"
})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class StudentBirthdayColumnMigrationTest extends AbstractTransactionalDbTests {

  @Autowired
  private DataSource dataSource;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Autowired
  private HibernateUserDetailsDao userDetailsDao;

  @Autowired
  private PlatformTransactionManager transactionManager;

  @Test
  public void migrateExistingStudentWithoutChangingUsernameAndKeepStudentRetrievable()
      throws Exception {
    TransactionTemplate transaction = new TransactionTemplate(transactionManager);
    transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    transaction.execute(status -> {
      StudentUserDetails existingStudent = new StudentUserDetails();
      existingStudent.setUsername("ExistingS0214");
      existingStudent.setPassword("password");
      existingStudent.setFirstname("Existing");
      existingStudent.setLastname("Student");
      existingStudent.setGender(Gender.FEMALE);
      existingStudent.setSignupdate(new Date());
      existingStudent.setNumberOfLogins(0);
      userDetailsDao.save(existingStudent);
      flush();
      return null;
    });

    if (!hasBirthdayColumn()) {
      jdbcTemplate.execute("ALTER TABLE student_user_details ADD birthday TIMESTAMP");
    }
    jdbcTemplate.update(
        "UPDATE student_user_details SET birthday = TIMESTAMP '2000-02-14' WHERE id = "
            + "(SELECT id FROM user_details WHERE username = ?)",
        "ExistingS0214");

    Flyway.configure()
        .dataSource(dataSource)
        .locations("classpath:db/migration")
        .baselineOnMigrate(true)
        .baselineVersion("4")
        .load()
        .migrate();

    StudentUserDetails student = retrieveStudentFromPersistenceLayerAfterMigration();

    assertNotNull(student);
    assertEquals("ExistingS0214", student.getUsername());
    assertEquals("Existing", student.getFirstname());
    assertEquals("Student", student.getLastname());

    assertFalse(hasBirthdayColumn());
  }

  private StudentUserDetails retrieveStudentFromPersistenceLayerAfterMigration()
      throws Exception {
    LocalContainerEntityManagerFactoryBean migratedEntityManagerFactory =
        new LocalContainerEntityManagerFactoryBean();
    migratedEntityManagerFactory.setDataSource(dataSource);
    migratedEntityManagerFactory.setPackagesToScan("org.wise");
    migratedEntityManagerFactory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

    Properties properties = new Properties();
    properties.setProperty("hibernate.hbm2ddl.auto", "none");
    migratedEntityManagerFactory.setJpaProperties(properties);

    try {
      migratedEntityManagerFactory.afterPropertiesSet();
      EntityManagerFactory entityManagerFactory = migratedEntityManagerFactory.getObject();
      assertNotNull(entityManagerFactory);
      EntityManager entityManager = entityManagerFactory.createEntityManager();
      try {
        return entityManager
            .createQuery(
                "select student from StudentUserDetails student where student.username = :username",
                StudentUserDetails.class)
            .setParameter("username", "ExistingS0214")
            .getSingleResult();
      } finally {
        entityManager.close();
      }
    } finally {
      migratedEntityManagerFactory.destroy();
    }
  }

  private boolean hasBirthdayColumn() throws Exception {
    try (Connection connection = dataSource.getConnection()) {
      DatabaseMetaData metadata = connection.getMetaData();
      try (ResultSet columns =
          metadata.getColumns(null, null, "student_user_details", "birthday")) {
        return columns.next();
      }
    }
  }
}

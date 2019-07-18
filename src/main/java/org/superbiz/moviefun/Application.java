package org.superbiz.moviefun;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})

public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public ServletRegistrationBean actionServletRegistration(ActionServlet actionServlet) {
        return new ServletRegistrationBean(actionServlet, "/moviefun/*");
    }

    @Bean
    public DatabaseServiceCredentials dbCred(){
        String vcap_services = System.getenv("VCAP_SERVICES");
        System.out.println("VCAP ::: >>>>" + vcap_services);
        return new DatabaseServiceCredentials(vcap_services);
    }

    @Bean
    public DataSource albumsDataSource(DatabaseServiceCredentials serviceCredentials) {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setURL(serviceCredentials.jdbcUrl("albums-mysql"));
        HikariConfig config = new HikariConfig();
        config.setDataSource(dataSource);
        return new HikariDataSource(config);
    }

    @Bean

    public DataSource moviesDataSource(DatabaseServiceCredentials serviceCredentials) {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setURL(serviceCredentials.jdbcUrl("movies-mysql"));
        HikariConfig config = new HikariConfig();
        config.setDataSource(dataSource);
        return new HikariDataSource(config);
    }

    @Bean
    public HibernateJpaVendorAdapter jpaAdaptor(){
        HibernateJpaVendorAdapter hibernateJpaVendorAdapter = new HibernateJpaVendorAdapter();
        hibernateJpaVendorAdapter.setDatabase(Database.MYSQL);
        hibernateJpaVendorAdapter.setGenerateDdl(true);
        hibernateJpaVendorAdapter.setDatabasePlatform("org.hibernate.dialect.MySQL5Dialect");
        return hibernateJpaVendorAdapter;
    }

    @Bean

    public LocalContainerEntityManagerFactoryBean albumBean(@Qualifier(value = "albumsDataSource") DataSource dataSource, HibernateJpaVendorAdapter vendorAdaptor) {
        LocalContainerEntityManagerFactoryBean bean = new LocalContainerEntityManagerFactoryBean();
        bean.setDataSource(dataSource);
        bean.setJpaVendorAdapter(vendorAdaptor);
        bean.setPackagesToScan(this.getClass().getPackage().getName()+".albums");
        bean.setPersistenceUnitName("album-unit");
        return bean;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean movieBean(@Qualifier(value = "moviesDataSource") DataSource dataSource, HibernateJpaVendorAdapter vendorAdaptor    ) {

        LocalContainerEntityManagerFactoryBean bean = new LocalContainerEntityManagerFactoryBean();
        bean.setDataSource(dataSource);
        bean.setJpaVendorAdapter(vendorAdaptor);
        System.out.println("Package..................."+this.getClass().getPackage().getName());
        bean.setPackagesToScan(this.getClass().getPackage().getName()+".movies");
        bean.setPersistenceUnitName("movies-unit");
        return bean;
    }

    @Bean
    public PlatformTransactionManager albumTransactionManager(@Qualifier(value = "albumBean") EntityManagerFactory entityFactoryBean) {

        return new JpaTransactionManager(entityFactoryBean);
    }

    @Bean
    public PlatformTransactionManager movieTransactionManager(@Qualifier(value = "movieBean") EntityManagerFactory entityFactoryBean) {


        return new JpaTransactionManager(entityFactoryBean);
    }


}

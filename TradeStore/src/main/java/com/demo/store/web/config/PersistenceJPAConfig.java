package com.demo.store.web.config;

import com.demo.store.web.security.AESEncryptionSecurityProvider;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.core.env.Environment;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@PropertySources({
    @PropertySource(value = "application-core.properties", ignoreResourceNotFound = true),
    @PropertySource(value = "application.properties", ignoreResourceNotFound = true)
})
@ComponentScan({"com.demo.store.web"})
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.demo.store.web")
public class PersistenceJPAConfig {

    @Autowired
    private Environment env;
    
    @Autowired
	private AESEncryptionSecurityProvider aesEncryptionSecurityProvider;
	
    public PersistenceJPAConfig() {
        super();
    }
	
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        final LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactoryBean.setDataSource(dataSource());
        entityManagerFactoryBean.setPackagesToScan(new String[] {
            "com.demo.store.web.entity"
        });

        final HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        entityManagerFactoryBean.setJpaVendorAdapter(vendorAdapter);
        entityManagerFactoryBean.setJpaProperties(additionalProperties());
        
        return entityManagerFactoryBean;
    }

    final Properties additionalProperties() {
        final Properties hibernateProperties = new Properties();
        hibernateProperties.setProperty("hibernate.hbm2ddl.auto", env.getProperty("hibernate.hbm2ddl.auto"));
        hibernateProperties.setProperty("hibernate.dialect", env.getProperty("hibernate.dialect"));
        hibernateProperties.setProperty("hibernate.cache.use_second_level_cache", env.getProperty("hibernate.cache.use_second_level_cache"));
        hibernateProperties.setProperty("hibernate.cache.use_query_cache", env.getProperty("hibernate.cache.use_query_cache"));
        // hibernateProperties.setProperty("hibernate.globally_quoted_identifiers", "true");
         hibernateProperties.setProperty("hibernate.jdbc.lob.non_contextual_creation",
                 env.getProperty("spring.jpa.properties.hibernate.jdbc.lob.non_contextual_creation"));
         hibernateProperties.setProperty("hibernate.default_schema", env.getProperty("hibernate.default_schema"));
         hibernateProperties.setProperty("org.hibernate.envers.audit_table_suffix", env.getProperty("org.hibernate.envers.audit_table_suffix"));
         return hibernateProperties;
    }

    @Bean
    public DataSource dataSource() {
    	HikariConfig config = new HikariConfig();
    	config.setJdbcUrl(env.getProperty("jdbc.url"));
        config.setUsername(env.getProperty("jdbc.user"));
        String decryptedPassword = aesEncryptionSecurityProvider
        		.decryptAES256(env.getProperty("jdbc.pass"), env.getProperty("aes.key"));
        config.setPassword( decryptedPassword);
        
        config.setMaximumPoolSize(env.getProperty("datasource.max.poosize", int.class, 20));
        config.addDataSourceProperty( "cachePrepStmts", 
        		env.getProperty("datasource.cache.prepstmts") );
        config.addDataSourceProperty( "prepStmtCacheSize" , 
        		env.getProperty("datasource.cache.prepstmt.size") );
        config.addDataSourceProperty( "prepStmtCacheSqlLimit" , 
        		env.getProperty("datasource.cache.prepstmt.sql.limit") );
        HikariDataSource dataSource = new HikariDataSource( config );
        
        return dataSource;
    }

    @Bean
    public PlatformTransactionManager transactionManager(final EntityManagerFactory emf) {
        final JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(emf);
        return transactionManager;
    }

    @Bean
    public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
        return new PersistenceExceptionTranslationPostProcessor();
    }	
}

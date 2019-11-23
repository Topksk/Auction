package com.bas.auction.core.config;

import com.bas.auction.core.Conf;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.*;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.session.ExpiringSession;
import org.springframework.session.MapSessionRepository;
import org.springframework.session.SessionRepository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import javax.mail.Session;
import javax.sql.DataSource;
import java.util.Properties;
import java.util.concurrent.Executor;

@Configuration
@ComponentScan(basePackages = "com.bas.auction",
        excludeFilters = {
                @Filter(type = FilterType.CUSTOM, classes = {ControllerTypeFilterImpl.class}),
                @Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {WebMvcConfig.class})
        })
@EnableTransactionManagement
@EnableScheduling
@EnableAsync
public class AppConfig {
    @Profile("prod")
    @Bean
    public DataSource dataSource() {
        JndiDataSourceLookup dsLookup = new JndiDataSourceLookup();
        dsLookup.setResourceRef(true);
        return dsLookup.getDataSource("jdbc/AuctionConn");
    }

    @Bean
    public PlatformTransactionManager txManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }

    @Bean
    @Qualifier("planImportTaskExecutor")
    public Executor planImportTaskExecutor(Conf conf) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(Runtime.getRuntime().availableProcessors());
        executor.setMaxPoolSize(conf.getPlanImportThreadPoolMaxSize());
        executor.setQueueCapacity(10);
        return executor;
    }

    @Bean
    @Qualifier("mailTaskExecutor")
    public Executor mailTaskExecutor(Conf conf) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setMaxPoolSize(conf.getMailThreadPoolMaxSize());
        executor.setQueueCapacity(10);
        return executor;
    }

    @Bean
    @Lazy
    public Session mailSession(Conf conf) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", conf.getSmtpAuth());
        props.put("mail.smtp.port", conf.getSmtpPort());
        props.put("mail.smtp.starttls.enable", conf.getSmtpStarttlsEnable());
        return Session.getInstance(props);
    }

    @Bean
    @Lazy
    public SessionRepository<ExpiringSession> sessionRepository() {
        return new MapSessionRepository();
    }

    @Profile("prod")
    @Bean(destroyMethod = "close")
    @Lazy
    public Client prodElasticsearchTransportClient(Conf conf) {
        /*String host = conf.getElasticsearchHost();
        String p = conf.getElasticsearchPort();
        String clusterName = conf.getElasticsearchClusterName();
        return elasticsearchTransportClient(host, p, clusterName);*/
        return null;
    }

    protected Client elasticsearchTransportClient(String host, String p, String clusterName) {
        /*int port = 9300;
        if (host == null)
            host = "localhost";
        if (p != null)
            port = Integer.parseInt(p);
        if (clusterName == null)
            clusterName = "elasticsearch";
        Settings settings = ImmutableSettings.settingsBuilder().put("cluster.name", clusterName).build();
        Client client = new TransportClient(settings);
        ((TransportClient) client).addTransportAddress(new InetSocketTransportAddress(host, port));
        return client;*/
        return null;
    }

}

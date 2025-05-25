package org.fiddich.coreinfradomain;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EntityScan("org.fiddich.coreinfradomain")
@EnableJpaRepositories("org.fiddich.coreinfradomain")
@EnableJpaAuditing
public class CoreDomainConfig {
}

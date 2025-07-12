package org.fiddich.coreinfraredis;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@Configuration
@EnableRedisRepositories("org.fiddich.coreinfraredis")
public class CoreRedisConfig {
}

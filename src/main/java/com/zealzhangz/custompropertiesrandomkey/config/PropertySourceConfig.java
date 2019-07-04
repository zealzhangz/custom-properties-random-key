package com.zealzhangz.custompropertiesrandomkey.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;

import javax.annotation.PostConstruct;

/**
 * @author Created by ao.zhang/ao.zhang@iluvatar.ai.<br/>
 * @version Version: 0.0.1
 * @date DateTime: 2019/07/04 16:46:00<br/>
 */
@Configuration
public class PropertySourceConfig {
    @Autowired
    private ConfigurableEnvironment env;

    @PostConstruct
    public void init() throws Exception {
        env.getPropertySources().addFirst(new RandomKeyPropertySource());
    }
}
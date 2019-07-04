package com.zealzhangz.custompropertiesrandomkey.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.context.config.RandomValuePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.util.StringUtils;

import java.util.Random;

/**
 * @author Created by ao.zhang/ao.zhang@iluvatar.ai.<br/>
 * @version Version: 0.0.1
 * @date DateTime: 2019/07/04 15:02:00<br/>
 */
public class RandomKeyPropertySource extends PropertySource<Random> {
    public static final String RANDOM_PROPERTY_SOURCE_NAME = "randomKey";

    private static final String PREFIX = "randomKey.";

    private static final Log logger = LogFactory.getLog(RandomKeyPropertySource.class);

    private static String CONSTANT_STRING = "0123456789abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public RandomKeyPropertySource(String name) {
        super(name, new Random());
    }

    public RandomKeyPropertySource(){
        this(RANDOM_PROPERTY_SOURCE_NAME);
    }

    @Override
    public Object getProperty(String name) {
        if (!name.startsWith(PREFIX)) {
            return null;
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Generating random property for '" + name + "'");
        }
        return getRandomValue(name.substring(PREFIX.length()));
    }

    private String getRandomValue(String type) {
        if (type.equals("key")) {
            return randomString(64);
        }
        String range = getRange(type, "key");
        if (range != null) {
            return getNextKeyRange(range);
        }
        return null;
    }

    private String getRange(String type, String prefix) {
        if (type.startsWith(prefix)) {
            int startIndex = prefix.length() + 1;
            if (type.length() > startIndex) {
                return type.substring(startIndex, type.length() - 1);
            }
        }
        return null;
    }

    private String getNextKeyRange(String range) {
        String[] tokens = StringUtils.commaDelimitedListToStringArray(range);
        int start = Integer.parseInt(tokens[0]);
        if (tokens.length == 1) {
            return randomString(start);
        }
        return null;
    }

    private String randomString(int length){
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < length; i++){
            int number = getSource().nextInt(CONSTANT_STRING.length());
            sb.append(CONSTANT_STRING.charAt(number));
        }
        return sb.toString();
    }

    public static void addToEnvironment(ConfigurableEnvironment environment) {
        environment.getPropertySources().addAfter(
                StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                new RandomValuePropertySource(RANDOM_PROPERTY_SOURCE_NAME));
        logger.trace("RandomValuePropertySource add to Environment");
    }
}
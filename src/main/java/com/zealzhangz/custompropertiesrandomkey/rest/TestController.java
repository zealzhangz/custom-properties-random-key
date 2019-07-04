package com.zealzhangz.custompropertiesrandomkey.rest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Created by ao.zhang/ao.zhang@iluvatar.ai.<br/>
 * @version Version: 0.0.1
 * @date DateTime: 2019/07/04 20:26:00<br/>
 */
@RestController
public class TestController {
    @Value("${randomKey.value1}")
    private String value1;
    @Value("${randomKey.value2}")
    private String value2;

    @GetMapping("/test")
    public Map<String,String> getRandomKey(){
        Map<String,String> map = new HashMap<>(2);
        map.put("value1",value1);
        map.put("value2",value2);
        return map;
    }
}

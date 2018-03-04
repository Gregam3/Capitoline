package com.greg.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
@Service
public class JSONUtils {
    private static final Logger LOG = Logger.getLogger(JSONUtils.class);
    public static ObjectMapper OBJECT_MAPPER = new ObjectMapper();
}
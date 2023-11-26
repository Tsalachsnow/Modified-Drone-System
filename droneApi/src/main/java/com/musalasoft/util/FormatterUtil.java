package com.musalasoft.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class FormatterUtil {
    public String decimalFormatter(double req) {
        if (req % 1 == 0) {
            int wholeNumber = (int) req;
            log.info("whole number:: "+wholeNumber);
            return String.valueOf(wholeNumber);
        } else {
            log.info("Original number:: "+req);
            return String.valueOf(req);
        }
    }
}

package com.goalias.common.rateLimiter.annotation;


import com.goalias.common.rateLimiter.enums.FlowGradeEnum;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface GoaliasHot {

    FlowGradeEnum grade();

    int count();

    int duration();
}

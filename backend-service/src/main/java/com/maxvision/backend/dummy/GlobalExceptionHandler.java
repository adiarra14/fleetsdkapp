package com.maxvision.backend.dummy;

import org.springframework.stereotype.Component;

/**
 * Temporary placeholder to satisfy Spring dependency while the real SDK
 * implementation is excluded.  It does nothing except exist as a bean so that
 * component wiring succeeds.
 */
@Component("globalExceptionHandler")
public class GlobalExceptionHandler {
    // No-op placeholder
}

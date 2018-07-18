package uk.ac.susx.shl.micromacro.webapp.health;

import com.codahale.metrics.health.HealthCheck;

/**
 * Created by sw206 on 18/04/2018.
 */
public class DefaultHealthCheck extends HealthCheck{
    @Override
    protected Result check() throws Exception {
        return Result.healthy();
    }
}

package org.craftsmenlabs.gareth.json.persist;

import lombok.Data;
import org.craftsmenlabs.gareth.api.context.ExperimentPartState;

import java.time.LocalDateTime;

/**
 * Created by hylke on 30/10/15.
 */
@Data
public class JsonExperimentContextData {

    private String hash;
    private LocalDateTime baselineRun, assumeRun, successRun, failureRun;
    private ExperimentPartState baselineState, assumeState, successState, failureState;
}

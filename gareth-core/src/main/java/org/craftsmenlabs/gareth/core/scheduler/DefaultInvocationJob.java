package org.craftsmenlabs.gareth.core.scheduler;

import com.xeiam.sundial.Job;
import com.xeiam.sundial.JobContext;
import com.xeiam.sundial.exceptions.JobInterruptException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.craftsmenlabs.gareth.api.context.ExperimentContext;
import org.craftsmenlabs.gareth.api.context.ExperimentPartState;
import org.craftsmenlabs.gareth.api.context.ExperimentRunContext;
import org.craftsmenlabs.gareth.api.invoker.MethodDescriptor;
import org.craftsmenlabs.gareth.api.invoker.MethodInvoker;
import org.craftsmenlabs.gareth.api.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * Created by hylke on 17/08/15.
 */
public class DefaultInvocationJob extends Job {

    private static final Logger logger = LoggerFactory.getLogger(DefaultInvocationJob.class);

    @Setter(AccessLevel.PROTECTED) // Setter for testing purposes (Adding this method for test is more important)
    private JobContext jobContext;

    @Override
    public void doRun() throws JobInterruptException {
        final MethodInvoker methodInvoker = getJobContext().getRequiredValue("methodInvoker");
        final ExperimentRunContext experimentContext = getJobContext().getRequiredValue("experimentRunContext");

        try {
            logger.debug("Invoking assumption");
            experimentContext.setAssumeState(ExperimentPartState.RUNNING);
            invoke(methodInvoker, experimentContext.getExperimentContext().hasStorage(), experimentContext.getExperimentContext().getAssume(), experimentContext.getStorage());
            experimentContext.setAssumeRun(LocalDateTime.now());
            experimentContext.setAssumeState(ExperimentPartState.FINISHED);
            if (null != experimentContext.getExperimentContext().getSuccess()) {
                logger.debug("Invoking success");
                experimentContext.setSuccessState(ExperimentPartState.RUNNING);
                invoke(methodInvoker, experimentContext.getExperimentContext().hasStorage(), experimentContext.getExperimentContext().getSuccess(), experimentContext.getStorage());
                experimentContext.setSuccessRun(LocalDateTime.now());
                experimentContext.setSuccessState(ExperimentPartState.FINISHED);
            }
        } catch (final Exception e) {
            experimentContext.setAssumeState(ExperimentPartState.ERROR);
            if (null != experimentContext.getExperimentContext().getFailure()) {
                logger.debug("Invoking failure");
                experimentContext.setFailureState(ExperimentPartState.RUNNING);
                invoke(methodInvoker, experimentContext.getExperimentContext().hasStorage(), experimentContext.getExperimentContext().getFailure(), experimentContext.getStorage());
                experimentContext.setFailureRun(LocalDateTime.now());
                experimentContext.setFailureState(ExperimentPartState.FINISHED);
            }
        }
    }

    private void invoke(final MethodInvoker methodInvoker, final boolean storageRequired, final MethodDescriptor methodDescriptor, final Storage storage) {
        if (storageRequired) {
            methodInvoker.invoke(methodDescriptor, storage);
        } else {
            methodInvoker.invoke(methodDescriptor);
        }
    }

    @Override
    protected JobContext getJobContext() {
        JobContext returnJobContext;
        if (null != this.jobContext) {
            returnJobContext = this.jobContext;
        } else {
            returnJobContext = super.getJobContext();
        }
        return returnJobContext;
    }
}

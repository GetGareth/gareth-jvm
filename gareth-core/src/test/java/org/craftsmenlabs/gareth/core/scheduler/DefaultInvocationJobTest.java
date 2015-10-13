package org.craftsmenlabs.gareth.core.scheduler;

import com.xeiam.sundial.JobContext;
import org.craftsmenlabs.gareth.api.context.ExperimentContext;
import org.craftsmenlabs.gareth.api.context.ExperimentPartState;
import org.craftsmenlabs.gareth.api.context.ExperimentRunContext;
import org.craftsmenlabs.gareth.api.invoker.MethodDescriptor;
import org.craftsmenlabs.gareth.api.invoker.MethodInvoker;
import org.craftsmenlabs.gareth.api.storage.Storage;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by hylke on 18/09/15.
 */
public class DefaultInvocationJobTest {


    private DefaultInvocationJob defaultInvocationJob;

    @Mock
    private JobContext mockJobContext;

    @Mock
    private MethodInvoker mockMethodInvoker;

    @Mock
    private ExperimentRunContext mockExperimentRunContext;

    @Mock
    private ExperimentContext mockExperimentContext;

    @Mock
    private MethodDescriptor mockMethodDescriptor;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        defaultInvocationJob = new DefaultInvocationJob();
        defaultInvocationJob.setJobContext(mockJobContext);
        when(mockExperimentRunContext.getExperimentContext()).thenReturn(mockExperimentContext);
        when(mockJobContext.getRequiredValue("methodInvoker")).thenReturn(mockMethodInvoker);
        when(mockJobContext.getRequiredValue("experimentRunContext")).thenReturn(mockExperimentRunContext);


    }

    @Test
    public void testDoRun() throws Exception {
        defaultInvocationJob.doRun();
        verify(mockExperimentRunContext).setAssumeState(ExperimentPartState.RUNNING);
        verify(mockExperimentRunContext).setAssumeState(ExperimentPartState.FINISHED);
        verify(mockExperimentRunContext, never()).setSuccessState(any(ExperimentPartState.class));
        verify(mockExperimentRunContext).setAssumeRun(any(LocalDateTime.class));
        verify(mockExperimentContext).getAssume();
        verify(mockMethodInvoker).invoke(any(MethodDescriptor.class));
    }

    @Test
    public void testDoRunWithStorage() throws Exception {
        when(mockExperimentContext.hasStorage()).thenReturn(true);
        defaultInvocationJob.doRun();
        verify(mockExperimentRunContext).setAssumeState(ExperimentPartState.RUNNING);
        verify(mockExperimentRunContext).setAssumeState(ExperimentPartState.FINISHED);
        verify(mockExperimentRunContext, never()).setSuccessState(any(ExperimentPartState.class));
        verify(mockExperimentRunContext).setAssumeRun(any(LocalDateTime.class));
        verify(mockExperimentContext).getAssume();
        verify(mockMethodInvoker).invoke(any(MethodDescriptor.class), any(Storage.class));
    }

    @Test
    public void testDoRunWithSuccess() throws Exception {
        when(mockExperimentContext.getSuccess()).thenReturn(mockMethodDescriptor);
        defaultInvocationJob.doRun();
        verify(mockExperimentRunContext).setAssumeState(ExperimentPartState.RUNNING);
        verify(mockExperimentRunContext).setAssumeState(ExperimentPartState.FINISHED);
        verify(mockExperimentRunContext).setSuccessState(ExperimentPartState.RUNNING);
        verify(mockExperimentRunContext).setSuccessState(ExperimentPartState.FINISHED);
        verify(mockExperimentRunContext).setAssumeRun(any(LocalDateTime.class));
        verify(mockExperimentRunContext).setSuccessRun(any(LocalDateTime.class));
        verify(mockExperimentContext).getAssume();
        verify(mockExperimentContext, times(2)).getSuccess();
        verify(mockExperimentContext, never()).getFailure();
        verify(mockMethodInvoker, times(2)).invoke(any(MethodDescriptor.class));
    }

    @Test
    public void testDoRunWithFailure() throws Exception {
        when(mockExperimentContext.getFailure()).thenReturn(mockMethodDescriptor);
        doThrow(new IllegalArgumentException()).doNothing().when(mockMethodInvoker).invoke(any());
        defaultInvocationJob.doRun();
        verify(mockExperimentRunContext).setAssumeState(ExperimentPartState.RUNNING);
        verify(mockExperimentRunContext).setAssumeState(ExperimentPartState.ERROR);
        verify(mockExperimentRunContext).setFailureState(ExperimentPartState.RUNNING);
        verify(mockExperimentRunContext).setFailureState(ExperimentPartState.FINISHED);
        verify(mockExperimentRunContext, never()).setAssumeRun(any(LocalDateTime.class));
        verify(mockExperimentRunContext, never()).setSuccessRun(any(LocalDateTime.class));
        verify(mockExperimentRunContext).setFailureRun(any(LocalDateTime.class));
        verify(mockExperimentContext).getAssume();
        verify(mockExperimentContext, never()).getSuccess();
        verify(mockExperimentContext, times(2)).getFailure();
        verify(mockMethodInvoker, times(2)).invoke(any(MethodDescriptor.class));
    }

    @Test
    public void testDoRunWithFailureWithSuccessDefined() throws Exception {
        when(mockExperimentContext.getSuccess()).thenReturn(mockMethodDescriptor);
        when(mockExperimentContext.getFailure()).thenReturn(mockMethodDescriptor);
        doThrow(new IllegalArgumentException()).doNothing().when(mockMethodInvoker).invoke(any());
        defaultInvocationJob.doRun();
        verify(mockExperimentRunContext).setAssumeState(ExperimentPartState.RUNNING);
        verify(mockExperimentRunContext).setAssumeState(ExperimentPartState.ERROR);
        verify(mockExperimentRunContext).setFailureState(ExperimentPartState.RUNNING);
        verify(mockExperimentRunContext).setFailureState(ExperimentPartState.FINISHED);
        verify(mockExperimentRunContext, never()).setAssumeRun(any(LocalDateTime.class));
        verify(mockExperimentRunContext, never()).setSuccessRun(any(LocalDateTime.class));
        verify(mockExperimentRunContext).setFailureRun(any(LocalDateTime.class));
        verify(mockExperimentContext).getAssume();
        verify(mockExperimentContext, never()).getSuccess();
        verify(mockExperimentContext, times(2)).getFailure();
        verify(mockMethodInvoker, times(2)).invoke(any(MethodDescriptor.class));
    }
}
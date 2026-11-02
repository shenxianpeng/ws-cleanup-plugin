package hudson.plugins.ws_cleanup;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import hudson.FilePath;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.CreateFileBuilder;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.RealJenkinsRule;

/**
 * The plugin must load and clean workspaces when matrix-project is not installed.
 */
public class NoMatrixProjectTest {

    @Rule
    public RealJenkinsRule rr = new RealJenkinsRule().omitPlugins("matrix-project");

    @Test
    public void cleansWithoutMatrixProject() throws Throwable {
        rr.then(NoMatrixProjectTest::_cleansWithoutMatrixProject);
    }

    private static void _cleansWithoutMatrixProject(JenkinsRule r) throws Throwable {
        assertNull(r.jenkins.getPlugin("matrix-project"));
        assertNotNull(r.jenkins.getPlugin("ws-cleanup"));
        assertTrue(r.jenkins.getPluginManager().getPlugin("ws-cleanup").isActive());

        FreeStyleProject p = r.createFreeStyleProject();
        p.getBuildersList().add(new CreateFileBuilder("foo.txt", "content"));
        WsCleanup cleanup = new WsCleanup();
        cleanup.setDisableDeferredWipeout(true);
        p.getPublishersList().add(cleanup);
        FreeStyleBuild b = r.buildAndAssertSuccess(p);
        r.assertLogContains(WsCleanup.LOG_PREFIX + "done", b);
        FilePath ws = b.getWorkspace();
        assertNotNull(ws);
        assertFalse(ws.child("foo.txt").exists());
        assertFalse(cleanup.isMatrixProject(p));

        WorkflowJob job = r.createProject(WorkflowJob.class, "pipeline");
        job.setDefinition(new CpsFlowDefinition(
                "node { writeFile file: 'bar.txt', text: 'content'; cleanWs disableDeferredWipeout: true }", true));
        WorkflowRun run = r.buildAndAssertSuccess(job);
        r.assertLogContains(WsCleanup.LOG_PREFIX + "done", run);
    }
}

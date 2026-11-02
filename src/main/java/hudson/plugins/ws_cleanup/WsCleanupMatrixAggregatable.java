package hudson.plugins.ws_cleanup;

import hudson.Extension;
import hudson.Launcher;
import hudson.matrix.MatrixAggregatable;
import hudson.matrix.MatrixAggregator;
import hudson.matrix.MatrixBuild;
import hudson.model.BuildListener;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

/**
 * Cleans up the workspace of a matrix parent build when {@link WsCleanup#getCleanupMatrixParent()} is set.
 *
 * <p>Registered as an optional extension so that the plugin works without matrix-project installed.
 */
@Extension(optional = true)
@Restricted(NoExternalUse.class)
public final class WsCleanupMatrixAggregatable implements MatrixAggregatable {

    @Override
    public MatrixAggregator createAggregator(MatrixBuild build, Launcher launcher, BuildListener listener) {
        WsCleanup publisher = build.getParent().getPublishersList().get(WsCleanup.class);
        if (publisher == null || !publisher.getCleanupMatrixParent()) {
            return null;
        }
        return new WsCleanupMatrixAggregator(
                build,
                launcher,
                listener,
                publisher.getPatterns(),
                publisher.getDeleteDirs(),
                publisher.isCleanWhenSuccess(),
                publisher.isCleanWhenUnstable(),
                publisher.isCleanWhenFailure(),
                publisher.isCleanWhenNotBuilt(),
                publisher.isCleanWhenAborted(),
                publisher.getNotFailBuild(),
                publisher.getExternalDelete());
    }
}

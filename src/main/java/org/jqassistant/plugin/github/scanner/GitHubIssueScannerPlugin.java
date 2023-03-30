package org.jqassistant.plugin.github.scanner;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.impl.scanner.AbstractUriScannerPlugin;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

public class GitHubIssueScannerPlugin extends AbstractUriScannerPlugin<GHRepository> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitHubIssueScannerPlugin.class);

    @Override
    public boolean accepts(URI uri, String path, Scope scope) {
        return GitHubScope.REPOSITORY == scope;
    }

    @Override
    protected Optional<GHRepository> getResource(URI uri, ScannerContext context) {
        return resolve(uri, () -> connect(uri), context);
    }

    private GHRepository connect(URI uri) {
        try {
            GitHub gitHub = GitHub.connect();
            String[] repoSplit = uri.getPath().split("/");
            if (repoSplit.length != 3) { // leading /
                LOGGER.error("Invalid GitHub repository URL specified. Needs to be <owner>/<name>");
                return null;
            }
            return gitHub.getRepository(repoSplit[1] + "/" + repoSplit[2]);
        } catch (IOException e) {
            throw new RuntimeException("Unable to connect to GitHub repository", e);
        }
    }
}

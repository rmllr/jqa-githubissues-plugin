package org.jqassistant.plugin.github.scanner;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jqassistant.plugin.github.model.*;
import org.jqassistant.plugin.github.cache.CacheEndpoint;
import org.kohsuke.github.*;
import org.kohsuke.github.GitHub;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * The GraphBuilder gets build only once per execution of the GitHub-Issues plugin
 * {@link GitHubIssueScannerPlugin#scan(FileResource, String, Scope, Scanner)} method.
 * <p>
 * It takes a list of specified repositories and starts to analyze them by following url paths.
 * <p>
 * The different tree depths are represented by the corresponding methods.
 */
@Slf4j
@RequiredArgsConstructor
class GraphBuilder {

    private final CacheEndpoint cacheEndpoint;
    GitHubRepository scanRepository(GHRepository ghRepository, String branch) throws IOException {

        //log.info("Scanning repository {} in organization {}", repositoryName, repositoryOwner);

        GitHub gitHub = GitHub.connect();
        log.info("Rate limit for user: {}", gitHub.getRateLimit());

        GitHubRepository gitHubRepository = cacheEndpoint.findOrCreateGitHubRepository(ghRepository);

        // branch dependent
        importBranch(ghRepository, gitHubRepository, branch);
        importPullRequests(ghRepository, gitHubRepository, branch); // first import PRs, because they'll also be returned when fetching issues

        // branch independent
        importMilestones(ghRepository, gitHubRepository);
        importIssues(ghRepository, gitHubRepository);
        importTags(ghRepository, gitHubRepository);
        importReleases(ghRepository, gitHubRepository);

        log.info("Repository scan complete");
        log.info("Remaining rate limit for user: {}", gitHub.getRateLimit());
        return gitHubRepository;
    }

    private void importBranch(GHRepository repository, GitHubRepository gitHubRepository, String branch) {
        try {
            GHBranch ghBranch = repository.getBranch(branch);
            List<GHCommit> ghCommits = repository.queryCommits().from(branch).list().toList();
            gitHubRepository.setBranch(cacheEndpoint.findOrCreateGitHubBranch(ghBranch, ghCommits));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void importPullRequests(GHRepository repository, GitHubRepository gitHubRepository, String branch) {
        List<GitHubPullRequest> pullRequests = new LinkedList<>();
        for (GHPullRequest ghPullRequest : repository.queryPullRequests().base(branch).list()) {
            log.debug("Found pull request: {}", ghPullRequest.getNumber());
            pullRequests.add(cacheEndpoint.findOrCreatePullRequest(ghPullRequest));
        }
        gitHubRepository.getPullRequests().addAll(pullRequests);
        log.info("Imported {} pull requests", pullRequests.size());
    }

    private void importMilestones(GHRepository repository, GitHubRepository gitHubRepository) {
        List<GitHubMilestone> milestones = new LinkedList<>();
        for (GHMilestone milestone : repository.listMilestones(GHIssueState.ALL)) {
            log.debug("Found milestone: {}", milestone.getNumber());
            milestones.add(cacheEndpoint.findOrCreateGitHubMilestone(milestone));
        }
        gitHubRepository.getMilestones().addAll(milestones);
        log.info("Imported {} milestones", milestones.size());
    }

    private void importReleases(GHRepository repository, GitHubRepository gitHubRepository) {
        List<GitHubRelease> releases = new LinkedList<>();
        try{
            for (GHRelease release : repository.listReleases()) {
                log.debug("Found release: {}", release.getName());
                releases.add(cacheEndpoint.findOrCreateGitHubRelease(release));
            }
            gitHubRepository.getReleases().addAll(releases);
            log.info("Imported {} releases", releases.size());
        } catch(IOException e){
            throw new RuntimeException();
        }
    }

    private void importTags(GHRepository repository, GitHubRepository gitHubRepository) {
        List<GitHubTag> tags = new LinkedList<>();
        try{
            for(GHTag tag : repository.listTags()) {
                log.debug("Found tag: {}", tag.getName());
                tags.add(cacheEndpoint.findOrCreateGitHubTag(tag));
            }
            gitHubRepository.getTags().addAll(tags);
            log.info("Imported {} tags", tags.size());
        } catch(IOException e){
            throw new RuntimeException();
        }
    }

    private void importIssues(GHRepository repository, GitHubRepository gitHubRepository) {
        List<GitHubIssue> issues = new LinkedList<>();
        for (GHIssue issue : repository.queryIssues().state(GHIssueState.ALL).list()) {
            log.debug("Found issue: {}", issue.getNumber());
            issues.add(cacheEndpoint.findOrCreateGitHubIssue(issue));
            if (issue.isPullRequest()) {
                log.debug("Issue {} already imported as Pull Request", issue.getNumber());
            }
        }
        gitHubRepository.getIssues().addAll(issues);
        log.info("Imported {} issues", issues.size());
    }
}

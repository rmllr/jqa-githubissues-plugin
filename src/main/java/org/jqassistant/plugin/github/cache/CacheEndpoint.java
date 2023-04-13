package org.jqassistant.plugin.github.cache;

import com.buschmais.jqassistant.core.store.api.Store;
import lombok.extern.slf4j.Slf4j;
import org.jqassistant.plugin.github.model.*;
import org.kohsuke.github.*;

import java.io.IOException;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

/**
 * <p>
 * This class was written to handle nodes in the resulting graph that get referenced more than one
 * time. These nodes get cached in the {@link DescriptorCache}.
 * </p>
 * <p>
 * All of the methods in this class work similar:
 * </p>
 * They check if a certain descriptor instance exists. If it does
 * exist, they return the instance. Otherwise they create a new one and save it in the
 * {@link Store} and in the {@link DescriptorCache}.
 */
@Slf4j
public class CacheEndpoint {

    private final Store store;
    private final DescriptorCache descriptorCache;

    public CacheEndpoint(Store store) {

        this.store = store;
        descriptorCache = new DescriptorCache();
    }

    /**
     * Check for {@link GitHubRepository}.
     *
     * @param ghRepository The GitHub repository.
     * @return The retrieved or newly created descriptor instance.
     */
    public GitHubRepository findOrCreateGitHubRepository(GHRepository ghRepository) {

        log.debug("Creating new repository {} for owner {}: ", ghRepository.getName(), ghRepository.getOwnerName());
        GitHubRepository repository = store.create(GitHubRepository.class);
        repository.setOwner(ghRepository.getOwnerName());
        repository.setName(ghRepository.getName());
        descriptorCache.setRepository(repository);

        return repository;
    }

    public GitHubPullRequest findOrCreatePullRequest(GHPullRequest ghPullRequest) {
        // A pull request is a special kind of issue
        return this.descriptorCache.getPullRequest(ghPullRequest.getNumber())
            .orElseGet(() -> createGitHubPullRequest(ghPullRequest));
    }

    private GitHubPullRequest createGitHubPullRequest(GHPullRequest ghPullRequest) {
        log.debug("Creating new pull request: {}", ghPullRequest);
        GitHubPullRequest pullRequest = store.create(GitHubPullRequest.class);
        if (ghPullRequest.getMergedAt() != null) {
            pullRequest.setMergedAt(ghPullRequest.getMergedAt().toInstant().atZone(ZoneOffset.UTC));
        }
        try {
            pullRequest.setBase(findOrCreateGitHubCommit(ghPullRequest.getRepository(), ghPullRequest.getBase().getSha()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            // avoid retrieving commits one by one
            pullRequest.setHead(findOrCreateGitHubCommit(ghPullRequest.getRepository(), ghPullRequest.getHead().getSha()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        populateIssueInformation(pullRequest, ghPullRequest);

        descriptorCache.put(pullRequest);

        return pullRequest;
    }

    /**
     * Check for {@link GitHubIssue}.
     *
     * @param ghIssue           The GitHub issue information.
     * @return The retrieved or newly created descriptor instance.
     */
    public GitHubIssue findOrCreateGitHubIssue(GHIssue ghIssue) {
        Optional<GitHubIssue> optionalIssue = this.descriptorCache.getIssue(ghIssue.getNumber());

        return optionalIssue.orElseGet(() -> createGitHubIssue(ghIssue));
    }

    private GitHubIssue createGitHubIssue(GHIssue ghIssue) {
        log.debug("Creating new issue: " + ghIssue);
        GitHubIssue issue;
        issue = store.create(GitHubIssue.class);
        populateIssueInformation(issue, ghIssue);
        descriptorCache.put(issue);

        return issue;
    }

    private void populateIssueInformation(GitHubIssue issue, GHIssue ghIssue) {
        issue.setNumber(ghIssue.getNumber());
        issue.setLocked(ghIssue.isLocked());
        issue.setState(ghIssue.getState().name());
        issue.setTitle(ghIssue.getTitle());
        issue.setBody(ghIssue.getBody());
        try {
            issue.setCreatedAt(ghIssue.getCreatedAt().toInstant().atZone(ZoneOffset.UTC));
        } catch (IOException e) {
            // todo
        }
        try {
            issue.setCreatedBy(findOrCreateGitHubUser(ghIssue.getUser()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            issue.setUpdatedAt(ghIssue.getUpdatedAt().toInstant().atZone(ZoneOffset.UTC));
        } catch (IOException e) {
            // todo
        }
        if (ghIssue.getMilestone() != null) {
            issue.setMilestone(findOrCreateGitHubMilestone(ghIssue.getMilestone()));
        }
        if (ghIssue.getClosedAt() != null) {
            issue.setClosedAt(ghIssue.getClosedAt().toInstant().atZone(ZoneOffset.UTC));
        }
        try {
            if (ghIssue.getClosedBy() != null) {
                issue.setClosedBy(findOrCreateGitHubUser(ghIssue.getClosedBy()));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (GHUser assignee : ghIssue.getAssignees()) {
            try {
                issue.getAssignees().add(findOrCreateGitHubUser(assignee));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        for (GHLabel label : ghIssue.getLabels()) {
            issue.getLabels().add(findOrCreateGitHubLabel(label));
        }
    }

    /**
     * Check for {@link GitHubUser}.
     *
     * @param gitUser The GitHub user information.
     * @return The retrieved or newly created descriptor instance.
     */
    public GitHubUser findOrCreateGitHubUser(GitUser gitUser) {
        return createGitHubUser(gitUser.getEmail(), gitUser.getUsername(), gitUser.getName());
    }

    public GitHubUser findOrCreateGitHubUser(GHUser ghUser) throws IOException {
        String email = ghUser.getEmail();
        String name = ghUser.getName();
        String login = ghUser.getLogin();

        return createGitHubUser(email, login, name);
    }

    private GitHubUser createGitHubUser(String email, String userName, String name) {
        if (userName == null && email == null) {
            log.warn("Neither username nor email set for user. Skipping.");
            return null;
        }
        // username or email may not be present
        Optional<GitHubUser> optionalUser = Optional.empty();
        if (userName != null) {
            optionalUser = descriptorCache.getUser(userName);
        }
        // may be present via email
        if (!optionalUser.isPresent() && email != null) {
            optionalUser = descriptorCache.getUser(email);
        }
        if (optionalUser.isPresent()) {
            if (email != null && optionalUser.get().getEmail() == null) {
                optionalUser.get().setEmail(email);
            }
            if (userName != null && optionalUser.get().getUsername() == null) {
                optionalUser.get().setUsername(userName);
            }
            if (name != null && optionalUser.get().getName() == null) {
                optionalUser.get().setName(name);
            }
            return optionalUser.get();
        } else {
            log.debug("Creating new user: " + email);
            GitHubUser user = store.create(GitHubUser.class);
            user.setEmail(email);
            user.setUsername(userName);
            user.setName(name);

            descriptorCache.put(user);

            return user;
        }
    }

    /**
     * Check for {@link GitHubLabel}.
     *
     * @param ghLabel The GitHub label information.
     * @return The retrieved or newly created descriptor instance.
     */
    public GitHubLabel findOrCreateGitHubLabel(GHLabel ghLabel) {
        Optional<GitHubLabel> optionalLabel = descriptorCache.getLabel(ghLabel.getName());

        return optionalLabel.orElseGet(() -> createGitHubLabel(ghLabel));
    }

    private GitHubLabel createGitHubLabel(GHLabel ghLabel) {
        log.debug("Creating new label: " + ghLabel);
        GitHubLabel label = store.create(GitHubLabel.class);
        label.setName(ghLabel.getName());
        label.setDescription(ghLabel.getDescription());
        label.setColor(ghLabel.getColor());

        descriptorCache.put(label);

        return label;
    }

    /**
     * Check for {@link GitHubMilestone}.
     *
     * @param gHMilestone       The GitHub milestone information.
     * @return The retrieved or newly created descriptor instance.
     */
    public GitHubMilestone findOrCreateGitHubMilestone(GHMilestone gHMilestone) {
        Optional<GitHubMilestone> milestone = descriptorCache.getMilestone(gHMilestone.getNumber());

        return milestone.orElseGet(() -> createGitHubMilestone(gHMilestone));
    }

    public GitHubRelease findOrCreateGitHubRelease(GHRelease ghRelease) {
        Optional<GitHubRelease> release = descriptorCache.getRelease(ghRelease.getName());
        return release.orElseGet(() -> createGitHubRelease(ghRelease));
    }

    public GitHubTag findOrCreateGitHubTag(GHTag ghTag) {
        Optional<GitHubTag> tag = descriptorCache.getTag(ghTag.getName());
        return tag.orElseGet(() -> createGitHubTag(ghTag));
    }

    private GitHubTag createGitHubTag(GHTag ghTag){
        log.debug("Creating new tag: " + ghTag);

        GitHubTag tag = store.create(GitHubTag.class);
        tag.setName(ghTag.getName());
        tag.setCommitSha(ghTag.getCommit().getSHA1());
        descriptorCache.put(tag);
        return tag;
    }

    private GitHubRelease createGitHubRelease(GHRelease ghRelease){
        log.debug("Creating new release: " + ghRelease);

        GitHubRelease release = store.create(GitHubRelease.class);
        release.setName(ghRelease.getName());
        release.setBody(ghRelease.getBody());
        release.setTagName(ghRelease.getTagName());
        descriptorCache.put(release);
        return release;
    }

    private GitHubMilestone createGitHubMilestone(GHMilestone ghMilestone) {
        log.debug("Creating new milestone: " + ghMilestone);

        GitHubMilestone milestone = store.create(GitHubMilestone.class);
        milestone.setNumber(ghMilestone.getNumber());
        milestone.setTitle(ghMilestone.getTitle());
        milestone.setDescription(ghMilestone.getDescription());
        milestone.setState(ghMilestone.getState().name());

        try {
            milestone.setCreatedAt(ghMilestone.getCreatedAt().toInstant().atZone(ZoneOffset.UTC));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            milestone.setUpdatedAt(ghMilestone.getUpdatedAt().toInstant().atZone(ZoneOffset.UTC));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (ghMilestone.getDueOn() != null) {
            milestone.setDueOn(ghMilestone.getDueOn().toInstant().atZone(ZoneOffset.UTC));
        }

        try {
            if (ghMilestone.getCreator() != null) {
                milestone.setCreatedBy(findOrCreateGitHubUser(ghMilestone.getCreator()));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        descriptorCache.put(milestone);

        return milestone;
    }

    public GitHubCommit findOrCreateGitHubCommit(GHRepository ghRepository, String sha1) throws IOException {
        Optional<GitHubCommit> optionalCommit = descriptorCache.getCommit(sha1);
        if (optionalCommit.isPresent()) {
            return optionalCommit.get();
        } else {
            return createGitHubCommit(ghRepository.getCommit(sha1));
        }
    }
    public GitHubCommit findOrCreateGitHubCommit(GHCommit ghCommit) {
        Optional<GitHubCommit> optionalCommit = descriptorCache.getCommit(ghCommit.getSHA1());

        return optionalCommit.orElseGet(() -> createGitHubCommit(ghCommit));
    }

    private GitHubCommit createGitHubCommit(GHCommit ghCommit) {
        log.debug("Creating new commit: " + ghCommit.getSHA1());
        GitHubCommit commit = store.create(GitHubCommit.class);
        commit.setSha(ghCommit.getSHA1());
        try {
            ghCommit.getCommitDate().toInstant().atZone(ZoneOffset.UTC);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            if (ghCommit.getCommitShortInfo().getAuthor() != null) {
                commit.setAuthor(findOrCreateGitHubUser(ghCommit.getCommitShortInfo().getAuthor()));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            commit.setCommitter(findOrCreateGitHubUser(ghCommit.getCommitShortInfo().getCommitter()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            commit.setCommitDate(ghCommit.getCommitDate().toInstant().atZone(ZoneOffset.UTC));
            if (ghCommit.getAuthoredDate() != null) {
                commit.setAuthoredDate(ghCommit.getAuthoredDate().toInstant().atZone(ZoneOffset.UTC));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (String parentSHA1 : ghCommit.getParentSHA1s()) {
            try {
                commit.getParents().add(findOrCreateGitHubCommit(ghCommit.getOwner(), parentSHA1));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        descriptorCache.put(commit);
        return commit;
    }

    public GitHubBranch findOrCreateGitHubBranch(GHBranch ghBranch, List<GHCommit> commits) {
        log.debug("Creating new branch: {}", ghBranch.getName());
        GitHubBranch branch = store.create(GitHubBranch.class);
        branch.setName(ghBranch.getName());

        // commits are ordered newest to oldest, thus first importing oldest to avoid retrieving parent from GitHub
        // branched parents are also included in the list, so no risk that commits are retrieved from GitHub API separately
        for (int i = commits.size() - 1; i >= 0; i--) {
            GHCommit ghCommit = commits.get(i);

            findOrCreateGitHubCommit(ghCommit);
        }

        try {
            branch.setHead(findOrCreateGitHubCommit(ghBranch.getOwner(), ghBranch.getSHA1()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        descriptorCache.setBranch(branch);
        return branch;
    }
}

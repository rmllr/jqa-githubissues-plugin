package org.jqassistant.plugin.github.cache;


import lombok.Getter;
import lombok.Setter;
import org.jqassistant.plugin.github.model.*;

import java.util.Optional;
import java.util.TreeMap;

/**
 * This class caches descriptor instances which have already been created.
 * <p>
 * For more information see {@link CacheEndpoint} which is the public accessible interface for this cache.
 */
class DescriptorCache {

    @Setter
    @Getter
    private GitHubRepository repository;

    @Setter
    @Getter
    private GitHubBranch branch;

    private final TreeMap<String, GitHubCommit> commits = new TreeMap<>();
    private final TreeMap<Integer, GitHubIssue> issues = new TreeMap<>();
    private final TreeMap<String, GitHubUser> users = new TreeMap<>();
    private final TreeMap<String, GitHubLabel> labels = new TreeMap<>();
    private final TreeMap<Integer, GitHubMilestone> milestones = new TreeMap<>();

    Optional<GitHubMilestone> getMilestone(Integer milestoneID) {
        return Optional.ofNullable(milestones.get(milestoneID));
    }

    Optional<GitHubUser> getUser(String userID) {
        return Optional.ofNullable(users.get(userID));
    }

    Optional<GitHubLabel> getLabel(String labelID) {
        return Optional.ofNullable(labels.get(labelID));
    }

    Optional<GitHubCommit> getCommit(String commitID) {
        return Optional.ofNullable(commits.get(commitID));
    }

    Optional<GitHubIssue> getIssue(Integer issueID) {
        return Optional.ofNullable(issues.get(issueID));
    }

    Optional<GitHubPullRequest> getPullRequest(Integer pullRequestId) {
        return getIssue(pullRequestId).filter(i -> i instanceof GitHubPullRequest).map(i -> (GitHubPullRequest) i);
    }

    void put(GitHubUser user) {
        if (user.getUsername() != null) {
            this.users.put(user.getUsername(), user);
        } else if (user.getEmail() != null) {
            this.users.put(user.getEmail(), user);
        }
    }

    void put(GitHubLabel label) {
        this.labels.putIfAbsent(label.getName(), label);
    }

    void put(GitHubMilestone milestone) {
        milestones.putIfAbsent(milestone.getNumber(), milestone);
    }

    void put(GitHubCommit commit) {
        this.commits.putIfAbsent(commit.getSha(), commit);
    }

    void put(GitHubIssue issue) {
        this.issues.putIfAbsent(issue.getNumber(), issue);
    }

}

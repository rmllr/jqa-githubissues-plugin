package org.jqassistant.plugin.github.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;
import com.buschmais.xo.neo4j.api.annotation.Relation;
import com.fasterxml.jackson.databind.annotation.JsonAppend;

import java.time.ZonedDateTime;

@Label("PullRequest")
public interface GitHubPullRequest extends GitHubIssue {

    @Property("mergedAt")
    ZonedDateTime getMergedAt();
    void setMergedAt(ZonedDateTime mergedAt);

    @Relation("HAS_BASE")
    GitHubCommit getBase();
    void setBase(GitHubCommit base);

    @Relation("HAS_HEAD")
    GitHubCommit getHead();
    void setHead(GitHubCommit head);
}

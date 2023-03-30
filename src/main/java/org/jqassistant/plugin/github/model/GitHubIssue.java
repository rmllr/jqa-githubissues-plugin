package org.jqassistant.plugin.github.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;
import com.buschmais.xo.neo4j.api.annotation.Relation;

import java.time.ZonedDateTime;
import java.util.List;

@Label("Issue")
public interface GitHubIssue extends GitHub {

    @Property("title")
    String getTitle();
    void setTitle(String text);

    @Property("body")
    String getBody();
    void setBody(String body);

    @Property("state")
    String getState();
    void setState(String state);

    @Property("number")
    int getNumber();
    void setNumber(int number);

    @Property("locked")
    boolean isLocked();
    void setLocked(boolean locked);

    @Property("createdAt")
    ZonedDateTime getCreatedAt();
    void setCreatedAt(ZonedDateTime createdAt);

    @Property("updatedAt")
    ZonedDateTime getUpdatedAt();
    void setUpdatedAt(ZonedDateTime updatedAt);

    @Property("closedAt")
    ZonedDateTime getClosedAt();
    void setClosedAt(ZonedDateTime closedAt);

    @Relation("CLOSED_BY")
    GitHubUser getClosedBy();
    void setClosedBy(GitHubUser closedBy);

    @Relation("HAS_LABEL")
    List<GitHubLabel> getLabels();

    @Relation("HAS_ASSIGNEE")
    List<GitHubUser> getAssignees();

    @Relation("CREATED_BY")
    GitHubUser getCreatedBy();
    void setCreatedBy(GitHubUser user);

    @Relation("IS_PART_OF")
    GitHubMilestone getMilestone();
    void setMilestone(GitHubMilestone milestone);
}

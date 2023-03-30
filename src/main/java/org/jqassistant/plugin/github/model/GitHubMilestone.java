package org.jqassistant.plugin.github.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;
import com.buschmais.xo.neo4j.api.annotation.Relation;

import java.time.ZonedDateTime;

@Label("Milestone")
public interface GitHubMilestone extends GitHub {

    @Property("title")
    String getTitle();
    void setTitle(String text);

    @Property("description")
    String getDescription();
    void setDescription(String description);

    @Property("state")
    String getState();
    void setState(String state);

    @Property("number")
    int getNumber();
    void setNumber(int number);

    @Property("createdAt")
    ZonedDateTime getCreatedAt();
    void setCreatedAt(ZonedDateTime createdAt);

    @Property("updatedAt")
    ZonedDateTime getUpdatedAt();
    void setUpdatedAt(ZonedDateTime updatedAt);

    @Property("dueOn")
    ZonedDateTime getDueOn();
    void setDueOn(ZonedDateTime dueOn);

    @Relation("CREATED_BY")
    GitHubUser getCreatedBy();
    void setCreatedBy(GitHubUser user);
}

package org.jqassistant.plugin.github.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label("Branch")
public interface GitHubBranch extends GitHub {

    @Property("name")
    String getName();
    void setName(String name);

    @Relation("HAS_HEAD")
    GitHubCommit getHead();

    void setHead(GitHubCommit head);
}

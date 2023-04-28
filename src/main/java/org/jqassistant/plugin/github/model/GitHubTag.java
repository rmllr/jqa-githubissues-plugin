package org.jqassistant.plugin.github.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;
import com.buschmais.xo.neo4j.api.annotation.Relation;
import org.kohsuke.github.GHCommit;

@Label("Tag")
public interface GitHubTag extends GitHub {

    @Property("name")
    String getName();
    void setName(String text);

    @Relation("REFERENCES_COMMIT")
    GitHubCommit getCommit();
    void setCommit(GitHubCommit commit);
}

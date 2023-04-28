package org.jqassistant.plugin.github.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label("Release")
public interface GitHubRelease extends GitHub {
    @Property("body")
    String getBody();
    void setBody(String body);
    @Property("name")
    String getName();
    void setName(String text);

    @Relation("REFERENCES_TAG")
    GitHubTag getTag();
    void setTag(GitHubTag tag);
}

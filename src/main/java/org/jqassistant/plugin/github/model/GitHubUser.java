package org.jqassistant.plugin.github.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;

@Label("User")
public interface GitHubUser extends GitHub {

    @Property("email")
    String getEmail();
    void setEmail(String email);

    @Property("username")
    String getUsername();
    void setUsername(String username);

    @Property("name")
    String getName();
    void setName(String name);

}

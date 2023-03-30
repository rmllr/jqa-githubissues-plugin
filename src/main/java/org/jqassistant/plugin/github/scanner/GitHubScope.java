package org.jqassistant.plugin.github.scanner;

import com.buschmais.jqassistant.core.scanner.api.Scope;

public enum GitHubScope implements Scope {

    REPOSITORY;

    @Override
    public String getPrefix() {
        return "github";
    }

    @Override
    public String getName() {
        return name();
    }
}

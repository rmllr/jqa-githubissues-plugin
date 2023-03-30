package org.jqassistant.plugin.github.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;
import com.buschmais.xo.neo4j.api.annotation.Relation;

import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.List;

@Label("Commit")
public interface GitHubCommit extends GitHub {

    @Property("sha")
    String getSha();
    void setSha(String sha);

    @Property("authoredDate")
    ZonedDateTime getAuthoredDate();
    void setAuthoredDate(ZonedDateTime authoredDate);

    @Property("commitDate")
    ZonedDateTime getCommitDate();
    void setCommitDate(ZonedDateTime commitDate);

    @Relation("COMMITED_BY")
    GitHubUser getCommitter();
    void setCommitter(GitHubUser committer);

    @Relation("AUTHORED_BY")
    GitHubUser getAuthor();
    void setAuthor(GitHubUser author);

    @Relation("HAS_PARENT")
    List<GitHubCommit> getParents();
}

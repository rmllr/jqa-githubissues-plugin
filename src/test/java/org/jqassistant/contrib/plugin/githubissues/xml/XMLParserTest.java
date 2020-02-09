package org.jqassistant.contrib.plugin.githubissues.xml;

import org.jdom2.JDOMException;
import org.jqassistant.contrib.plugin.githubissues.jdom.XMLGitHubRepository;
import org.jqassistant.contrib.plugin.githubissues.jdom.XMLParser;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class XMLParserTest {

    private static final String VALID_XML = "<github-repositories>\n" +
            "\n" +
            "    <github-repository>\n" +
            "        <user>github-user</user>\n" +
            "        <name>github-repository</name>\n" +
            "\n" +
            "        <credentials>\n" +
            "            <user>other-github-user</user>\n" +
            "            <password>secret</password>\n" +
            "        </credentials>\n" +
            "    </github-repository>" +
            "</github-repositories>";

    @Test
    public void readValidPluginConfiguration() throws JDOMException, IOException {

        List<XMLGitHubRepository> gitHubRepositoryList =
                XMLParser.parseConfiguration(new ByteArrayInputStream(VALID_XML.getBytes()));

        assertEquals(1, gitHubRepositoryList.size());
        assertEquals("github-user", gitHubRepositoryList.get(0).getUser());
        assertEquals("github-repository", gitHubRepositoryList.get(0).getName());

        assertEquals("other-github-user", gitHubRepositoryList.get(0).getCredentials().getUser());
        assertEquals("secret", gitHubRepositoryList.get(0).getCredentials().getPassword());
    }
}

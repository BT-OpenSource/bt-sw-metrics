package com.bt.swmetrics.vcs

import com.bt.swmetrics.Configurator
import com.bt.swmetrics.vcs.git.GitDiffParser
import com.bt.swmetrics.vcs.git.GitListParser
import com.bt.swmetrics.vcs.git.GitLogParser
import com.bt.swmetrics.vcs.svn.SvnDiffParser
import com.bt.swmetrics.vcs.svn.SvnListParser
import com.bt.swmetrics.vcs.svn.SvnLogParser

class VcsParserFactory {
    private static SvnLogParser svnLogParser = new SvnLogParser()
    private static GitLogParser gitLogParser = new GitLogParser()
    private static SvnListParser svnListParser = new SvnListParser()
    private static GitListParser gitListParser = new GitListParser()
    private static SvnDiffParser svnDiffParser = new SvnDiffParser()
    private static GitDiffParser gitDiffParser = new GitDiffParser()

    static LogParser getLogParserInstance(Configurator configurator) {
        findAndValidateVcsType(configurator) == 'svn' ? svnLogParser : gitLogParser
    }

    static ListParser getListParserInstance(Configurator configurator) {
        findAndValidateVcsType(configurator) == 'svn' ? svnListParser : gitListParser
    }

    static DiffParser getDiffParserInstance(Configurator configurator) {
        findAndValidateVcsType(configurator) == 'svn' ? svnDiffParser : gitDiffParser
    }

    private static String findAndValidateVcsType(Configurator configurator) {
        if (!configurator.vcsType) {
            throw new IllegalArgumentException("VCS type must be set")
        }

        if (!['svn', 'git'].contains(configurator.vcsType)) {
            throw new IllegalArgumentException("Unknown VCS type: $configurator.vcsType")
        }

        configurator.vcsType
    }
}

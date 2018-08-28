package com.bt.swmetrics

class Configurator {
    static final String OPT_HELP = 'help'
    static final String OPT_DEBUG = 'debug'
    static final String OPT_EXCLUDE = 'exclude'
    static final String OPT_INCLUDE = 'include'
    static final String OPT_CSV = 'csv'
    static final String OPT_SHOW_ANNOTATED = 'show-annotated'
    static final String OPT_TREEMAP = 'treemap'
    static final String OPT_PATH_COLUMN = 'path-column'
    static final String OPT_SIZE_COLUMN = 'size-column'
    static final String OPT_COLOUR_COLUMN = 'colour-column'
    static final String OPT_EXTRA_COLUMN = 'extra-column'
    static final String OPT_TOP_COLOUR = 'top-colour'
    static final String OPT_BOTTOM_COLOUR = 'bottom-colour'
    static final String OPT_TOP_THRESHOLD = 'top-threshold'
    static final String OPT_BOTTOM_THRESHOLD = 'bottom-threshold'
    static final String OPT_PARTITION_SIZE = 'partition-size'
    static final String OPT_TITLE = 'title'
    static final String OPT_RESOURCE_PATH = 'resource-path'
    static final String OPT_VCS_LOG_FILE = 'vcs-log'
    static final String OPT_VCS_LIST_FILE = 'vcs-list'
    static final String OPT_VCS_DIFF_FILE = 'vcs-diff'
    static final String OPT_IGNORE_PREFIX = 'ignore-prefix'
    static final String OPT_JOIN_CSV_FILES = 'join-csv-files'
    static final String OPT_JOIN_TYPE = 'join-type'
    static final String OPT_TAB_SIZE = 'tab-size'
    static final String OPT_AUTHOR_STATS = 'author-stats'
    static final String OPT_AUTHOR_PATHS = 'author-paths'
    static final String OPT_VCS_TYPE = 'vcs-type'


    static final BigDecimal AUTO_THRESHOLD = -(Double.MAX_VALUE as BigDecimal)

    static final int DEFAULT_TOP_COLOUR = 0xff0000
    static final int DEFAULT_BOTTOM_COLOUR = 0x00ff00
    static final int DEFAULT_PARTITION_SIZE = 500
    static final BigDecimal DEFAULT_TOP_THRESHOLD = AUTO_THRESHOLD
    static final BigDecimal DEFAULT_BOTTOM_THRESHOLD = AUTO_THRESHOLD
    static final String DEFAULT_TITLE = 'Tree-Map Visualisation'
    static final String JOIN_TYPE_INNER = 'inner'
    static final String JOIN_TYPE_OUTER = 'outer'
    static final int DEFAULT_TAB_SIZE = 4

    private final String[] allArgs
    private CliBuilder cliBuilder
    private final OptionAccessor options

    Configurator(String[] args) {
        allArgs = args
        initialiseCliBuilder()
        options = cliBuilder.parse(allArgs)
        if (options[OPT_DEBUG]) {
            System.setProperty('org.slf4j.simpleLogger.defaultLogLevel', 'debug')
        }
    }

    private void initialiseCliBuilder() {
        cliBuilder = new CliBuilder(usage: 'java -jar swmetrics.jar [options] file-or-directory ...')
        cliBuilder.with {
            h(longOpt: OPT_HELP, 'Show this message and exit')
            d(longOpt: OPT_DEBUG, 'Turn on debug logging')
            c(longOpt: OPT_CSV, 'Output results in CSV format')
            t(longOpt: OPT_TREEMAP, args: 1, argName: '"jit" or "d3"', 'Generate HTML/JavaScript tree-map visualisation')
            a(longOpt: OPT_SHOW_ANNOTATED, 'Show source file annotated with indent levels')
            x(longOpt: OPT_EXCLUDE, args: 1, argName: 'pattern', 'Exclude any paths matching pattern (may be repeated for multiple patterns)')
            i(longOpt: OPT_INCLUDE, args: 1, argName: 'pattern', 'Include only paths matching pattern (may be repeated for multiple patterns)')
            P(longOpt: OPT_PATH_COLUMN, args: 1, argName: 'name or index', 'Select this column as the "path" data for visualisation (default 1)')
            S(longOpt: OPT_SIZE_COLUMN, args: 1, argName: 'name or index', 'Select this column as the "size" metric for visualisation (default 2)')
            C(longOpt: OPT_COLOUR_COLUMN, args: 1, argName: 'name or index', 'Select this column as the "colour" metric for visualisation (default 3)')
            A(longOpt: OPT_EXTRA_COLUMN, args: 1, argName: 'column name', 'Select this column as additional data for visualisation (may be repeated)')
            _(longOpt: OPT_TOP_COLOUR, args: 1, argName: 'rgb-colour', 'Use this colour at the top end of the scale (default #ff0000)')
            _(longOpt: OPT_BOTTOM_COLOUR, args: 1, argName: 'rgb-colour', 'Use this colour at the bottom end of the scale (default #00ff00)')
            T(longOpt: OPT_TOP_THRESHOLD, args: 1, argName: 'number', 'Set the top of the range threshold (default mean + 3 std-devs)')
            B(longOpt: OPT_BOTTOM_THRESHOLD, args: 1, argName: 'number', 'Set the bottom end of the range threshold (default mean - 3 std-devs)')
            _(longOpt: OPT_PARTITION_SIZE, args: 1, argName: 'number', 'Set the number of tiles in a tree-map before it is partitioned (default 500)')
            _(longOpt: OPT_TITLE, args: 1, argName: 'text', 'Set the title text for the visualisation')
            R(longOpt: OPT_RESOURCE_PATH, args: 1, argName: 'path-prefix', 'Link to resources under this path, instead of embedding')
            n(longOpt: OPT_IGNORE_PREFIX, args: 1, argName: 'path-prefix', 'Ignore this prefix in comparing paths')
            j(longOpt: OPT_JOIN_CSV_FILES, args: 2, valueSeparator: '=', argName: 'left-col=right-col', 'Join two CSV files on the specified column names')
            _(longOpt: OPT_JOIN_TYPE, args: 1, argName: 'inner or outer', 'Specify the join type for joining CSV files')
            _(longOpt: OPT_TAB_SIZE, args: 1, argName: 'number', "Specify the tab size for expansion (default $DEFAULT_TAB_SIZE)")
            v(longOpt: OPT_VCS_TYPE, args: 1, argName: '"git" or "svn"', "Specify the VCS type for --vcs-log, vcs-list or vcs-diff")
            _(longOpt: OPT_VCS_LOG_FILE, args: 1, argName: 'file-name', 'Process output from VCS log command')
            _(longOpt: OPT_VCS_LIST_FILE, args: 1, argName: 'file-name', 'Process output from VCS list command')
            _(longOpt: OPT_VCS_DIFF_FILE, args: 1, argName: 'file-name', 'Process output from VCS diff command')
            _(longOpt: OPT_AUTHOR_STATS, "With --vcs-log, generate statistics about authors, not paths")
            _(longOpt: OPT_AUTHOR_PATHS, "With --vcs-log, generate data about authors and paths they have touched")
        }
    }

    def showHelp() {
        cliBuilder.usage()
    }

    List<String> getArguments() {
        options.arguments()
    }

    boolean isHelpRequested() {
        options[OPT_HELP]
    }

    boolean isDebug() {
        options[OPT_DEBUG]
    }

    boolean isCsvOutput() {
        options[OPT_CSV]
    }

    boolean isShowAnnotated() {
        options[OPT_SHOW_ANNOTATED]
    }

    List<String> getExcludedPatterns() {
        options.xs ?: []
    }

    List<String> getIncludedPatterns() {
        options.is ?: []
    }

    String getTreeMapVisualisation() {
        options.t ?: ''
    }

    String getPathColumn() {
        options.P ?: '1'
    }

    String getSizeColumn() {
        options.S ?: '2'
    }

    String getColourColumn() {
        options.C ?: '3'
    }

    List<String> getExtraColumns() {
        if (options.As) {
            options.As.collectMany { it.split(',') as List }
        } else {
            []
        }
    }

    int getBottomColour() {
        convertColourStringIfPresent(OPT_BOTTOM_COLOUR, DEFAULT_BOTTOM_COLOUR)
    }

    int getTopColour() {
        convertColourStringIfPresent(OPT_TOP_COLOUR, DEFAULT_TOP_COLOUR)
    }

     int convertColourStringIfPresent(String optionName, int defaultColour) {
        if (options[optionName]) {
            Integer.parseInt(options[optionName] - '#' - '0x', 16)
        } else {
            defaultColour
        }
    }

    BigDecimal getTopThreshold() {
        if (options.T) {
            options.T == 'auto' ? AUTO_THRESHOLD : options.T as BigDecimal
        } else {
            DEFAULT_BOTTOM_THRESHOLD
        }
    }

    BigDecimal getBottomThreshold() {
        if (options.B) {
            options.B == 'auto' ? AUTO_THRESHOLD : options.B as BigDecimal
        } else {
            DEFAULT_TOP_THRESHOLD
        }
    }

    int getPartitionSize() {
        options[OPT_PARTITION_SIZE] ? options[OPT_PARTITION_SIZE] as Integer : DEFAULT_PARTITION_SIZE
    }

    String getTitle() {
        options[OPT_TITLE] ?: DEFAULT_TITLE
    }

    String getResourcePath() {
        options[OPT_RESOURCE_PATH] ?: ''
    }

    String getVcsLogFile() {
        options[OPT_VCS_LOG_FILE] ?: ''
    }

    String getVcsListFile() {
        options[OPT_VCS_LIST_FILE] ?: ''
    }

    String getVcsDiffFile() {
        options[OPT_VCS_DIFF_FILE] ?: ''
    }

    List<String> getIgnorePrefixes() {
        options.ns ?: []

    }

    List<String> getCsvJoinFields() {
        options.js ?: []
    }

    String getJoinType() {
        options[OPT_JOIN_TYPE] ?: JOIN_TYPE_INNER
    }

    int getTabSize() {
        options[OPT_TAB_SIZE] ? options[OPT_TAB_SIZE] as Integer : DEFAULT_TAB_SIZE
    }

    boolean isAuthorStats() {
        options[OPT_AUTHOR_STATS]
    }

    boolean isAuthorPaths() {
        options[OPT_AUTHOR_PATHS]
    }

    String getVcsType() {
        options.v ?: null
    }
}

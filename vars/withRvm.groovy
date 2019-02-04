/**
*   It seems Jenkins uses non-interactive remote execution with ssh 
*   and does not respect neither profile nor login shell config files.
*   As a result I didn't find a way to inject RVM to Jenkins Pipeline sh command
*/
withRvm(String version, String gemset, Closure cl) {
    // First we have to amend the `PATH`.
    final RVM_HOME = '$HOME/.rvm'
    paths = [
        "$RVM_HOME/gems/$version@$gemset/bin",
        "$RVM_HOME/gems/$version@global/bin",
        "$RVM_HOME/rubies/$version/bin",
        "$RVM_HOME/bin",
        "${env.PATH}"
    ]
    def path = paths.join(':')
    // First, let's make sure Ruby version is present.
    withEnv(["PATH=${env.PATH}:$RVM_HOME", "RVM_HOME=$RVM_HOME"]) {
        // Having `rvm` command available, `rvm use` can be used directly:
        sh "set +x; source $RVM_HOME/scripts/rvm; rvm use --create --install --binary $version@$gemset"
    }
    // Because we've just made sure Ruby is installed and Gemset is present, Ruby env vars can be exported just as `rvm use` $
    withEnv([
        "PATH=$path",
        "GEM_HOME=$RVM_HOME/gems/$version@$gemset",
        "GEM_PATH=$RVM_HOME/gems/$version@$gemset:$RVM_HOME/gems/$version@global",
        "MY_RUBY_HOME=$RVM_HOME/rubies/$version",
        "IRBRC=$RVM_HOME/rubies/$version/.irbrc",
        "RUBY_VERSION=$version"
    ]) {
        // `rvm` can't tell if `rvm use` was run or the env vars were set manually.
        sh 'rvm info'
        sh 'ruby --version'
        cl()
    }
}


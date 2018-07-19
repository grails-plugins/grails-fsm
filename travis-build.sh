#!/usr/bin/env bash
set -e

export EXIT_STATUS=0

echo "TRAVIS_TAG          : $TRAVIS_TAG"
echo "TRAVIS_BRANCH       : $TRAVIS_BRANCH"
echo "TRAVIS_PULL_REQUEST : $TRAVIS_PULL_REQUEST"
echo "Publishing archives for branch $TRAVIS_BRANCH"
rm -rf build

./gradlew --stop

./gradlew :fsm:clean || EXIT_STATUS=$?
./gradlew :fsm:check || EXIT_STATUS=$?


if [[ ${EXIT_STATUS} -ne 0 ]]; then
    echo "Check failed"
    exit ${EXIT_STATUS}
fi

./gradlew :integration-testapp:clean || EXIT_STATUS=$?
./gradlew :integration-testapp:check || EXIT_STATUS=$?

if [[ ${EXIT_STATUS} -ne 0 ]]; then
    echo "Integration tests failed"
    exit ${EXIT_STATUS}
fi

if [[ -n ${TRAVIS_TAG} ]] || [[ ${TRAVIS_BRANCH} == 'master' && ${TRAVIS_PULL_REQUEST} == 'false' ]]; then
    echo "Publishing archives for branch $TRAVIS_BRANCH"

    if [[ -n ${TRAVIS_TAG} ]]; then
        echo "Pushing build to Bintray"
        ./gradlew :fsm:bintrayUpload || EXIT_STATUS=$?
    else
        echo "Publishing snapshot..."
        ./gradlew :fsm:publish || EXIT_STATUS=$?
    fi

    ./gradlew :fsm:docs || EXIT_STATUS=$?

    git config --global user.name "$GIT_NAME"
    git config --global user.email "$GIT_EMAIL"
    git config --global credential.helper "store --file=~/.git-credentials"
    echo "https://$GH_TOKEN:@github.com" > ~/.git-credentials

    git clone https://${GH_TOKEN}@github.com/${TRAVIS_REPO_SLUG}.git -b gh-pages gh-pages --single-branch > /dev/null
    cd gh-pages

    # If this is the master branch then update the snapshot
    if [[ ${TRAVIS_BRANCH} == 'master' ]]; then
        rm -rf snapshot/
        mv ../plugin/build/docs/index.html index.html
        git add index.html
        mkdir -p snapshot
        cp -r ../plugin/build/docs/manual/. ./snapshot/
        git add snapshot/*
    fi

    # If there is a tag present then this becomes the latest
    if [[ -n $TRAVIS_TAG ]]; then
        rm -rf latest/
        mkdir -p latest
        cp -r ../plugin/build/docs/manual/. ./latest/
        git add latest/*

        version="$TRAVIS_TAG" # eg: v3.0.1
        version=${version:1} # 3.0.1
        majorVersion=${version:0:4} # 3.0.
        majorVersion="${majorVersion}x" # 3.0.x

        rm -rf "$version"
        mkdir -p "$version"
        cp -r ../plugin/build/docs/manual/. "./$version/"
        git add "$version/*"

        rm -rf "$majorVersion"
        mkdir -p "$majorVersion"
        cp -r ../plugin/build/docs/manual/. "./$majorVersion/"
        git add "$majorVersion/*"
    fi

    git commit -a -m "Updating docs for Travis build: https://travis-ci.org/$TRAVIS_REPO_SLUG/builds/$TRAVIS_BUILD_ID"
    git push origin HEAD
    cd ..
    rm -rf gh-pages
fi

exit ${EXIT_STATUS}

EXIT_STATUS=0

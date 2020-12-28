# Release

### Create a release
```bash
mvn org.lorislab.maven:semver-release-maven-plugin:0.7.0:release-create -DskipPush=true
git push --tags
git push
```